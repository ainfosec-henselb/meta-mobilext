# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "A root filesystem for a live Xen installation environment"
DESCRIPTION = " \
    The core root filesystem (rootfs) for the MobileXT installation \
    environment, which should be able to install a simple or complex \
    Xen setup onto a target system -- including the MobileXT platform. \
\
    This is usually packaged into a full install image, which includes \
    the filesystem images to be installed. As an example, see \
    xen-base-installer-image, which wraps this rootfs in a bootable ISO. \
"
AUTHOR = "Kyle J. Temkin <temkink@ainfosec.com>"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=3f40d7994397109285ec7b81fdeb3b58"

PROVIDES += "virtual/installer-root-image"

IMAGE_PREPROCESS_COMMAND = "rootfs_update_timestamp ;"

# Select the components to be included in the installer's live environment. 
IMAGE_INSTALL += " \
	angstrom-packagegroup-boot \
	packagegroup-basic \
  kernel-modules \
	fixmac \
"

IMAGE_FSTYPES += "ext4"

#Set the target hostname to indicate that one is running inside
#the installer. This is useful in development shells.
TARGET_HOSTNAME = "mobilext-installer"

# Systemd journal is preferred.
BAD_RECOMMENDATIONS += "busybox-syslog"
IMAGE_DEV_MANAGER   = "udev"
IMAGE_INIT_MANAGER  = "systemd"
IMAGE_INITSCRIPTS   = " "
IMAGE_LOGIN_MANAGER = "busybox shadow"

export IMAGE_BASENAME = "mobilext-installer"

inherit override-hostname
inherit image

#Create simple, toolchain-independent symlinks to the boot image that can be consumed by other images.
do_rootfs_append() {
    for IMAGE_TYPE in ${IMAGE_FSTYPES}; do

        #Compute the path the image will have, if it's been created...
        SOURCE_IMAGE=${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.${IMAGE_TYPE}

        #... and if it has been created, create our symlink.
        if [[ -e $SOURCE_IMAGE ]]; then
            ln -sf ${SOURCE_IMAGE} ${DEPLOY_DIR_IMAGE}/installer-${MACHINE}.${IMAGE_TYPE}
        fi

    done
}
