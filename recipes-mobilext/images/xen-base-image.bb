# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "Full drive partition for a Xen system, including bootloader."
DESCRIPTION = " \
    A full drive for a Xen system, including bootloader. This should be \
    writable directly toi a hard drive to have a full Xen system quickly \
    and easily. \
    \
    This is appropriate for embedded systems, which typically \
    have smal, fixed-size storages, but not for large-drive computers. \
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
PARTITION_TABLE_TYPE = "msdos"

#Specify the location where the first partition should start.
FIRST_PARTITION_START = "2048"


#
# Add the partition contained in the given /formatted/ file to the target image.
# <warning> This function uses and changes the stateful LAST_PARTITION_END variable.</warning>
#
add_partition_from_image() {
  SOURCE=$1
  IMAGE_TYPE=$2

  #TODO: Automatically compute size!
  PARTITION_SIZE=$3

  #If we've already recorded the start of the "free" disk space on a prior call,
  #use our prerecorded value. Otherwise, this must be the first call, so we'll 
  #assume the free space starts at the location where we want to place the first
  #partition.
  FREE_SPACE_START=${FREE_SPACE_START:-${FIRST_PARTITION_START}}

  #Compute the start and end for the given partition.
  PARTITION_START=${FREE_SPACE_START}
  PARTITION_END=$(expr ${PARTITION_START} \+ ${PARTITION_SIZE} \- 1)

  #Add an empty partition to the image...
	parted -s ${TARGET_IMAGE} unit KiB mkpart primary ${IMAGE_TYPE} ${PARTITION_START} ${PARTITION_END}

  #... and populate its contents.
  #The double sync seems to be necessary on some platforms, as evidenced in other recipes.
  dd conv=notrunc if=${SOURCE} of=${TARGET_IMAGE} bs=1024 seek=${PARTITION_START} && sync && sync

  #Export the last partition's end for use by future add_partition calls.
  export FREE_SPACE_START=$(expr ${PARTITION_END} \+ 1)
  
}

#
# Add the boot partition to the start of the given disk image, and populate it.
#
add_boot_partition() {

  #FIXME: Compute the boot disk size!
  BOOT_PARTITION_SIZE=20480

  #Add the boot partition to our image...
  add_partition_from_image ${BOOT_IMAGE} ${BOOT_IMAGE_TYPE} ${BOOT_PARTITION_SIZE} 

  #... and set it bootable.
	parted -s ${TARGET_IMAGE} set 1 boot on

}

#
# Add a root partition to the target image, and populate it.
#
add_dom0_partition() {

  #FIXME: Compute the boot disk size!
  DOM0_PARTITION_SIZE=1048576

  #Add the dom0 partition from our image.
  add_partition_from_image ${DOM0_IMAGE} ${DOM0_IMAGE_TYPE} ${DOM0_PARTITION_SIZE}

}

#
# Add a VM storage partition to the target image.
# This will be left empty, for the user.
# <warning> This function uses the stateful LAST_PARTITION_END variable.</warning>
#
add_storage_partition() {
  
  #Add the empty partition which should fill up the remainder of the image.
  #(This will potentially lose the last KiB, but the -1 seems to be necessary for compatibility.)
	parted -s ${TARGET_IMAGE} -- unit KiB mkpart primary fat32 ${FREE_SPACE_START} -1

  #FIXME: Format!
  #This will involve creating a large, sparse image and then copying it over. =(
  #Definitely delete this one afterwards.
}

#
# Populate the bootloader onto the HDD image's boot sector.
#
install_bootloader() {
  #FIXME: Fail on missing BOOTLOADER_START.
  dd conv=notrunc if=${BOOTLOADER} of=${TARGET_IMAGE} seek=${BOOTLOADER_START}
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

}
addtask do_image before do_build


