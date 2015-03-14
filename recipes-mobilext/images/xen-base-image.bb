# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "Full drive partition for a Xen system, including bootloader."
DESCRIPTION = " \
    A full drive for a Xen system, including bootloader. This should be \
    writable directly to a hard drive to have a full Xen system quickly \
    and easily. \
    \
    This is appropriate for embedded systems, which typically \
    have small, fixed-size storages, but not for large-drive computers. \
    Use xen-installer-image for those systems. \
"
AUTHOR = "Kyle J. Temkin <temkink@ainfosec.com>"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=3f40d7994397109285ec7b81fdeb3b58"

#Ensure that we have the partitioning tools used to create disk images.
IMAGE_DEPENDS += " \
  parted-native \
  mtools-native \
  dosfstools-native \
  e2fsprogs-native \
  qemu-native \
"

#Ensure that the relevant pieces are deployed before we try and combine them
#into a disk image.
do_image[depends] += " \
  virtual/bootloader:do_deploy \
  virtual/boot-partition-image:do_bootimg \
  virtual/dom0-image:do_rootfs \
"

#Specify the partition types for the root and storage partitions.
#These should likely be linux formatted; so ext3/ext4 are good choices.
BOOT_IMAGE_TYPE    ?= "fat32"
DOM0_IMAGE_TYPE    ?= "ext4"
STORAGE_IMAGE_TYPE ?= "ext4"

#Specify the generic name for each of the dependencies to be 
#included in the hard drive image.
BOOT_IMAGE = "${DEPLOY_DIR_IMAGE}/boot-${MACHINE}.hddimg"
DOM0_IMAGE = "${DEPLOY_DIR_IMAGE}/dom0-${MACHINE}.${DOM0_IMAGE_TYPE}"
BOOTLOADER = "${DEPLOY_DIR_IMAGE}/bootloader-${MACHINE}.bin"

#If we haven't been provided a disk image size, assume 8 GiB.
#Disk image size, in MiB.
IMAGE_SIZE ?= "8192"

#Decide the name for the target image.
IMAGE_NAME   ?= "xen-base" 
TARGET_IMAGE  = "${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.disk.img"

#The type of the partition table to create.
#Should be one of "aix", "amiga", "bsd", "dvh", "gpt", "loop", "mac", "msdos", "pc98", or "sun".
PARTITION_TABLE_TYPE  = "msdos"

#Specify the location where the first partition should start.
FIRST_PARTITION_START = "2048"
PARTITION_ALIGNMENT   = "2048"
BOOTLOADER_START     ?= "0"

#
# Prints the effective "aligned size" of a given file to the standard output.
# Intended to allow creation of partitions sized for the images they should contain.
#
get_image_size() {

    #Ensure we have the actual file, even if we're working with a symlink.
    FILENAME=$(readlink -f $1)

    #Get the size of the file in bytes...
    BYTE_SIZE=$(du -b "${FILENAME}" | cut -f1)

    #... and round it to the next largest aligned unit.
    BYTE_SIZE_PREROUND=$(expr ${BYTE_SIZE} \+ ${PARTITION_ALIGNMENT} \- 1)
    UNIT_COUNT=$(expr ${BYTE_SIZE_PREROUND} / ${PARTITION_ALIGNMENT})

    #Convert the unit size into a size in KiB.
    KB_PER_UNIT=$(expr ${PARTITION_ALIGNMENT} / 1024)
    KB_SIZE=$(expr ${UNIT_COUNT} \* ${KB_PER_UNIT})

    #... and echo it, so it can be used in another command.
    echo ${KB_SIZE}
}


#
# Add the partition contained in the given /formatted/ file to the target image.
# <warning> This function uses and changes the stateful LAST_PARTITION_END variable.</warning>
#
add_partition_from_image() {
  SOURCE=$1
  IMAGE_TYPE=$2

  #This does nothing, but without it, BitBake isn't smart enough to
  #pull in get_image_size. TODO: Is there a better way to get BB to pull this in?
  get_image_size $SOURCE

  #Compute the size of the image to be added
  PARTITION_SIZE=$(get_image_size $SOURCE)

  #If we've already recorded the start of the "free" disk space on a prior call,
  #use our prerecorded value. Otherwise, this must be the first call, so we'll 
  #assume the free space starts at the location where we want to place the first
  #partition.
  FREE_SPACE_START=${FREE_SPACE_START:-${FIRST_PARTITION_START}}

  #Compute the start and end for the given partition.
  #Note that parte'd partition end is exclusive, so these form a [START,END) pair.
  PARTITION_START=${FREE_SPACE_START}
  PARTITION_END=$(expr ${PARTITION_START} \+ ${PARTITION_SIZE})

  #Add an empty partition to the image...
	parted -s ${TARGET_IMAGE} unit KiB mkpart primary ${IMAGE_TYPE} ${PARTITION_START} ${PARTITION_END}

  #... and populate its contents.
  #The double sync seems to be necessary on some platforms, as evidenced in other recipes.
  dd conv=notrunc if=${SOURCE} of=${TARGET_IMAGE} bs=1024 seek=${PARTITION_START} && sync && sync

  #Export the last partition's end for use by future add_partition calls.
  export FREE_SPACE_START=$(expr ${PARTITION_END})

}

#
# Add the boot partition to the start of the given disk image, and populate it.
#
add_boot_partition() {
  #Add the boot partition to our image...
  add_partition_from_image ${BOOT_IMAGE} ${BOOT_IMAGE_TYPE}

  #... and set it bootable.
	parted -s ${TARGET_IMAGE} set 1 boot on
}

#
# Add a root partition to the target image, and populate it.
#
add_dom0_partition() {
  #Add the dom0 partition from our image.
  add_partition_from_image ${DOM0_IMAGE} ${DOM0_IMAGE_TYPE}
}

#
# Add a VM storage partition to the target image.
# This will be left empty, for the user.
# <warning> This function uses the stateful FREE_SPACE_START variable.</warning>
#
add_storage_partition() {
  #Add the empty partition which should fill up the remainder of the image.
  #(This will potentially lose the last KiB, but the -1 seems to be necessary for compatibility.)
	parted -s ${TARGET_IMAGE} -- unit KiB mkpart primary fat32 ${FREE_SPACE_START} -1

  #Create a formatted disk image.
  #Unfortunately, we can't easily create a filesystem inside of a disk image without
  #fancy custom tools. Instead, we'll create a temporary image and then copy it over.
  PARTITION_SIZE=$(parted -s ${TARGET_IMAGE} unit B print | awk '/^ 3/ {print $4}' | sed -e 's/.$//')

  TEMP_IMAGE=${TMPDIR}/storage-partition.${STORAGE_IMAGE_TYPE}

  #Create an empty disk image which is the size of the target partition...
	dd if=/dev/zero of=${TEMP_IMAGE} bs=1 count=0 seek=${PARTITION_SIZE}

  #... and format it appropriately.
  mkfs.ext4 -F ${TEMP_IMAGE}

  #Copy over the created file structures...
  dd conv=notrunc if=${TEMP_IMAGE} of=${TARGET_IMAGE} bs=1024 seek=${FREE_SPACE_START} && sync && sync

  #... and finally, remove the temporary image.
  rm -f ${TEMP_IMAGE}

}

#
# Populate the bootloader onto the HDD image's boot sector.
#
install_bootloader() {
  dd conv=notrunc if=${BOOTLOADER} of=${TARGET_IMAGE} seek=${BOOTLOADER_START}
}

#
# Create a QEMU disk (QED) version of the disk image; this allows us to test the image
# out using QEMU (or other virtualization software, like VirtualBox).
#
create_qemu_disk() {
  qemu-img convert -f raw -O qed ${TARGET_IMAGE} ${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.qed
}

#
# Create and populate the target disk image.
#
do_image() {
  
  #Create an empty disk image which is the size of the target drive.
  #We'll use this as a "canvas" on which the disk image will be created.
	dd if=/dev/zero of=${TARGET_IMAGE} bs=1M count=0 seek=${IMAGE_SIZE}

	#Create a new MS-DOS partition table on the given image.
	parted -s ${TARGET_IMAGE} mklabel msdos

  #Create and populate each of the partitions on the HDD image.
  add_boot_partition 
  add_dom0_partition
  add_storage_partition

  #... and install the bootloader onto the partition.
  install_bootloader

  #If our current configuration supports producing QEMU images,
  #create a QEMU formatted disk.
  if [[ "${IMAGE_FSTYPES}" == *"qed"*  ]]; then
    create_qemu_disk
  fi

}
addtask do_image before do_build
