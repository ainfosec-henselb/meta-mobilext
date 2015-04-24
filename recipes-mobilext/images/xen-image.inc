# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)

#Image components for a structured Xen environment.

#If no dom0 image has been provided, use the basic platform.
DOM0_IMAGE ?= "xen-base-dom0-image"

##Ensure that the relevant pieces are deployed before we try and combine them
##into a disk image.
do_image[depends] += " \
  virtual/bootloader:do_deploy \
  virtual/boot-partition-image:do_bootimg \
  ${DOM0_IMAGE}:do_deploy \
"

#Specify the partition types for the root and storage partitions.
#These should likely be linux formatted; so ext3/ext4 are good choices.
BOOT_IMAGE_TYPE    ?= "fat32"
DOM0_IMAGE_TYPE    ?= "ext4"
STORAGE_IMAGE_TYPE ?= "ext4"

#Specify the generic name for each of the dependencies to be
#included in the hard drive image.
BOOT_IMAGE_FILE = "${DEPLOY_DIR_IMAGE}/boot-${MACHINE}.hddimg"
DOM0_IMAGE_FILE = "${DEPLOY_DIR_IMAGE}/${DOM0_IMAGE}-${MACHINE}.${DOM0_IMAGE_TYPE}"
BOOTLOADER      = "${DEPLOY_DIR_IMAGE}/bootloader-${MACHINE}.bin"


inherit disk-image

#
# Add the boot partition to the start of the given disk image, and populate it.
#
add_boot_partition() {
    add_boot_partition_from_image ${BOOT_IMAGE_FILE} ${BOOT_IMAGE_TYPE}
}

#
# Add a root partition to the target image, and populate it.
#
add_dom0_partition() {
    add_partition_from_image ${DOM0_IMAGE_FILE} ${DOM0_IMAGE_TYPE}
}

#
# Add a VM storage partition to the target image.
# This will be left empty, for the user.
#
add_storage_partition() {
    add_empty_partition ${STORAGE_IMAGE_TYPE} -1
}

#
# Populate the target disk image.
#
populate() {
    add_boot_partition 
    add_dom0_partition
    add_storage_partition
}
