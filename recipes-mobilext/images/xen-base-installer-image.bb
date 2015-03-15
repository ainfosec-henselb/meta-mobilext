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

#Ensure that we have the partitioning tools used to create disk images.
IMAGE_DEPENDS += " \
  parted-native \
  mtools-native \
  dosfstools-native \
  e2fsprogs-native \
"

#Inherit the base class for building live images.
inherit image-live

do_rootfs() {
    #TODO
    exit 0
}
addtask rootfs before build
