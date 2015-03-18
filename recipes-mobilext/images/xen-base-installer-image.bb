# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "A live image, intended for installing a basic Xen environment."
DESCRIPTION = " \
    An ISO-formatted CD/USB image that allows the user to install a basic \
    Xen environment. This image will create a single ISO that contains \
    everything needed to install Xen. \
    \
    This is appropriate for systems with internal storage, and for systems \
    whose external storage is too large for efficient image creation. For small \
    embedded platforms, consider using xen-base-image to create a drive image \
    directly. \
"
AUTHOR = "Kyle J. Temkin <temkink@ainfosec.com>"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=3f40d7994397109285ec7b81fdeb3b58"

SYSLINUX_LABEL="boot"

#Ensure that we have the partitioning tools used to create disk images.
IMAGE_DEPENDS += " \
  parted-native \
  mtools-native \
  dosfstools-native \
  e2fsprogs-native \
"

#Select the image to be used to pilot the install.
INSTALLER_IMAGE = "virtual/installer-root-image"
ROOTFS          = "${DEPLOY_DIR_IMAGE}/installer-${MACHINE}.ext4"

#... and ensure the installer is built before we try to use it.
do_bootimg[depends] += "${INSTALLER_IMAGE}:do_rootfs"

#The path to the directory on the target image where the installation assets
#(e.g. the rootfs to be placed on the target)
INSTALL_ASSETS_PATH="${DEST}/install"

#Specify the names of the partition files that should be installed.
#These will be copied into an installer-accesible location.
BOOT_PARTITION_IMAGE ?= "xen-base-boot"
DOM0_PARTITION_IMAGE ?= "xen-base-dom0"

#Inherit the base class for building live images.
inherit image-live

#
# Append each of the pieces that should be included for use by the installer.
#
populate_append() {
    install -d ${INSTALL_ASSETS_PATH}

    #Include the bootloader binary...
    install -m 0644 ${DEPLOY_DIR_IMAGE}/bootloader-${MACHINE}.bin ${INSTALL_ASSETS_PATH}/bootloader.bin

    #... the boot partition.
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${BOOT_PARTITION_IMAGE}-${MACHINE}.hddimg ${INSTALL_ASSETS_PATH}/boot-partition.hddimg

    #... and the compressed dom0 rootfs.
    install -m 0644 ${DEPLOY_DIR_IMAGE}/${DOM0_PARTITION_IMAGE}-${MACHINE}.tar.xz ${INSTALL_ASSETS_PATH}/dom0-rootfs.tar.xz
}



#
# Skip building of an installer hard drive image; as we only want to install from CD/USB.
#
build_hddimg() {
    exit 0
}

#
# Build the bootloader configuration necessary to start Xen.
# This allows us to add LINUX_EXTRA_OPTIONS for serial install.
#
# TODO: See if this needs to implemented for grub; and potentially override that too.
# Or, decide that serial debug isn't necessary for installer
#

#(These would look cleaner with Heredocs, but then I'd have to use tabs instead
#of spaces, and no one wants that.)

build_syslinux_cfg() {
	echo ALLOWOPTIONS 1 > ${SYSLINUXCFG}
	echo SERIAL 0 115200 >> ${SYSLINUXCFG}
	echo DEFAULT ${SYSLINUX_LABEL} >> ${SYSLINUXCFG}
	echo TIMEOUT ${SYSLINUX_TIMEOUT} >> ${SYSLINUXCFG}
	echo PROMPT 1 >> ${SYSLINUXCFG}
	echo LABEL ${SYSLINUX_LABEL} >> ${SYSLINUXCFG}
	echo KERNEL /vmlinuz >> ${SYSLINUXCFG}
	echo APPEND initrd=/initrd LABEL=${SYSLINUX_LABEL} root=/dev/ram0 ${LINUX_EXTRA_OPTIONS} >> ${SYSLINUXCFG}
}

#
# For now, don't create an installer rootfs; this is handled by the bootimg class.
#
do_rootfs() {
    exit 0
}
addtask rootfs before build

