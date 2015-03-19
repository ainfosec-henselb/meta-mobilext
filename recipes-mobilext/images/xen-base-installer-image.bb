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

#... and ensure the installer and images are built before we try to use it.
do_bootimg[depends] += "\
    virtual/installer-root-image:do_rootfs \
    virtual/kernel:do_deploy \
"

#Specify the name of the core installer rootfs used to perform the install
ROOTFS = "${DEPLOY_DIR_IMAGE}/installer-${MACHINE}.ext4"

#Inherit the base class for building live images.
inherit image-live

#
# For now, don't create an installer rootfs, as the bootimg class pulls in an
# existing rootfs.
#
do_rootfs() {
    exit 0
}
addtask rootfs before build

