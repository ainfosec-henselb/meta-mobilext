# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)
#
# Class for creating full disk images. To use:
#   -Define the populate function, which should add images to your target
#    disk using the functions below.
#   -Optionally, set the BOOTLOADER variable to the name of the binary to
#    be used as the bootloader. By default, this is written to the MBR,
#    but bootloader start can be used to adjust the bootloader's byte offset.
#   -Optionally, override the defaults below.
#

#Decide the name for the target image.
IMAGE_NAME            ?= "xen-base" 
TARGET_IMAGE          ?= "${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.disk.img"

#The type of the partition table to create.
#Should be one of "aix", "amiga", "bsd", "dvh", "gpt", "loop", "mac", "msdos", "pc98", or "sun".
PARTITION_TABLE_TYPE  ?= "msdos"

#Specify the location where the first partition should start.
FIRST_PARTITION_START ?= "2048"
PARTITION_ALIGNMENT   ?= "2048"

#If we haven't been provided a disk image size, assume 8 GiB.
#Disk image size, in MiB.
IMAGE_SIZE            ?= "8192"

#Assume we're not writing a bootloader, unless otherwise specified.
BOOTLOADER            ?= ""
BOOTLOADER_START      ?= "0"

#Ensure that we have the partitioning tools used to create disk images.
IMAGE_DEPENDS += " \
  parted-native \
  mtools-native \
  dosfstools-native \
  e2fsprogs-native \
  qemu-native \
"

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
# Arguments: 
#   -$1: The image to be added to the partition; and
#   -$2: Optional; the type of the image to be added (e.g. "ext3"). Used to set the partition
#        type in the partition table.
#
add_partition_from_image() {
    SOURCE=$1
    IMAGE_TYPE=${2:-ext4}
  
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
    PARTITION_NUMBER=${PARTITION_NUMBER:-0}

    #Begin working with a new partition.
    PARTITION_NUMBER=$(expr ${PARTITION_NUMBER} \+ 1)

    echo "Adding partition number ${PARTITION_NUMBER}."
  
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
    export FREE_SPACE_START=${PARTITION_END}
    export PARTITION_NUMBER
}

#
# Add a boot partition from an image. Identical to add_partition_from_image(),
# but sets the boot flag on the partition afterwards.
#
add_boot_partition_from_image() {
    
    add_partition_from_image $@
  
    #... and set it bootable.
    echo "Setting ${PARTITION_NUMBER} up as bootable."
  	parted -s ${TARGET_IMAGE} set ${PARTITION_NUMBER} boot on
}


#
# Add a VM storage partition to the target image.
# This will be left empty, for the user.
# <warning> This function uses the stateful FREE_SPACE_START variable.</warning>
#
# Arguments:
#   -$1: partition type; the partition type to be created
#   -$2: partition size; the size of the partition to be created.
#        Negative numbers indicate sectors from the end of the disk; -1 uses the rest.
#
add_empty_partition() {
    NEW_PARTITION_TYPE=$1
    NEW_PARTITION_SIZE=$2

    #Add the empty partition which should fill up the remainder of the image.
    #(This will potentially lose the last KiB, but the -1 seems to be necessary for compatibility.)
  	parted -s ${TARGET_IMAGE} -- unit KiB mkpart primary fat32 ${FREE_SPACE_START} ${NEW_PARTITION_SIZE} 
  
    #Create a formatted disk image.
    #Unfortunately, we can't easily create a filesystem inside of a disk image without
    #fancy custom tools. Instead, we'll create a temporary image and then copy it over.
    PARTITION_SIZE=$(parted -s ${TARGET_IMAGE} unit B print | awk '/^ 3/ {print $4}' | sed -e 's/.$//')
  
    TEMP_IMAGE=${TMPDIR}/storage-partition.${NEW_PARTITION_TYPE}
  
    #Create an empty disk image which is the size of the target partition...
  	dd if=/dev/zero of=${TEMP_IMAGE} bs=1 count=0 seek=${PARTITION_SIZE}
  
    #... and format it appropriately.
    mkfs.${NEW_PARTITION_TYPE} -F ${TEMP_IMAGE}
  
    #Copy over the created file structures...
    dd conv=notrunc if=${TEMP_IMAGE} of=${TARGET_IMAGE} bs=1024 seek=${FREE_SPACE_START} && sync && sync
  
    #... and finally, remove the temporary image.
    rm -f ${TEMP_IMAGE}
}

#
# Populate the bootloader onto the HDD image's boot sector.
#
install_bootloader() {
    dd conv=notrunc if=${BOOTLOADER} of=${TARGET_IMAGE} seek=${BOOTLOADER_START} bs=1
}

#
# Create a QEMU disk (QED) version of the disk image; this allows us to test the image
# out using QEMU (or other virtualization software, like VirtualBox).
#
create_qemu_disk() {
    qemu-img convert -f raw -O qed ${TARGET_IMAGE} ${TARGET_IMAGE}.qed
}

#
# Create and populate the target disk image.
#
do_image() {
    
    #Create an empty disk image which is the size of the target drive.
    #We'll use this as a "canvas" on which the disk image will be created.
  	dd if=/dev/zero of=${TARGET_IMAGE} bs=1M count=0 seek=${IMAGE_SIZE}
  
  	#Create a new MS-DOS partition table on the given image.
  	parted -s ${TARGET_IMAGE} mklabel ${PARTITION_TABLE_TYPE}
  
    #Create and populate each of the partitions on the HDD image.
    populate
  
    #... and install the bootloader onto the partition.
    if [[ x"${BOOTLOADER}" != x ]]; then
        install_bootloader
    fi
  
    #If our current configuration supports producing QEMU images,
    #create a QEMU formatted disk.
    if [[ "${IMAGE_FSTYPES}" == *"qed"*  ]]; then
        create_qemu_disk
    fi
  
}
addtask do_image before do_build
