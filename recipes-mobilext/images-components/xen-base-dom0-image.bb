# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)
#

SUMMARY = "Root image for a minimal Xen control domain"
DESCRIPTION = " \
    This component image generates the root filesystem for a minimal \
    Xen control domain ('dom0'), based off of Linux. It can be used as \
    a basis for more complex images, or as a simple platform for testing. \
"
AUTHOR = "Kyle J. Temkin <temkink@ainfosec.com>"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=3f40d7994397109285ec7b81fdeb3b58"

require xen-dom0-image.inc
