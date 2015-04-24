# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "Full drive partition for a GUI-based demonstration Xen system, including bootloader."
DESCRIPTION = " \
    A full drive image for a Xen system that includes everything needed for \
    a good tech demo, including GUI and VNC support. This image is intended to \
    be a good way to show off Xen on ARM. \
    \
    This is appropriate for embedded systems, which typically \
    have small, fixed-size storages, but not for large-drive computers. \
    Use xen-installer-image for those systems. \
"
AUTHOR = "Kyle J. Temkin <temkink@ainfosec.com>"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690"

IMAGE_NAME = "xen-demo"

#Use the demo image's dom0 as our core rootfs.
DOM0_IMAGE = "xen-demo-dom0-image"

#Build a basic Xen image.
require xen-image.inc
