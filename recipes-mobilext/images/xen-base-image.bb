# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "Full drive partition for a minimal Xen system, including bootloader."
DESCRIPTION = " \
    A full drive for a minimal Xen system, including bootloader. This should \
    be writable directly to a hard drive to have a full Xen system quickly \
    and easily. \
    \
    This is appropriate for embedded systems, which typically \
    have small, fixed-size storages, but not for large-drive computers. \
    Use xen-installer-image for those systems. \
"
AUTHOR = "Kyle J. Temkin <temkink@ainfosec.com>"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690"

IMAGE_NAME = "xen-base"

#Build a basic Xen image.
require xen-image.inc

#And use our minimal dom0 image.
PREFERRED_PROVIDER_virtual/dom0-image = "xen-base-dom0-image"
