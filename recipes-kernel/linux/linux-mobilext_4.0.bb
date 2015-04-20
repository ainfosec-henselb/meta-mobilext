# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "Linux Kernel for MobileXT supported platforms"
DESCRIPTION = " \
    This package provides a customized linux kernel which provides \
    support for each of the MobileXT supported host platforms. \
"
AUTHOR = "Kyle J. Temkin <temkink@ainfosec.com>"
SECTION = "kernel"

#License for the linux kernel itself.
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"

#For now, we'll explicitly list each of the compatible machines.
#Once the xen-specific configuration fragments are broken out, we should be able
#to drop the hard requirement of having hard-listing compatible machines by adding
#per-machine configuration conditionally using an override in per-machine-additions.inc.
COMPATIBLE_MACHINE = "cubietruck|primo73|generic-x86_64-xen|generic-x86_64-efi-xen|surface-pro-3"

#Use the basic OpenEmbedded method for building kernels.
inherit kernel

#Include support for device trees, and for explicit per-device customizations.
require recipes-kernel/linux/linux-dtb.inc
require recipes-kernel/linux/per-machine-additions.inc

#Set up the linux kernel versionining information.
LINUX_VERSION ?= "${PV}"
LINUX_VERSION_EXTENSION ?= "-mobilext-${LINUX_KERNEL_TYPE}"

#Use the release tarball that mathces our provided kernel version.
SRC_URI = "https://www.kernel.org/pub/linux/kernel/v4.x/linux-${PV}.tar.xz"

SRC_URI[md5sum] = "a86916bd12798220da9eb4a1eec3616d"
SRC_URI[sha256sum] = "0f2f7d44979bc8f71c4fc5d3308c03499c26a824dd311fdf6eef4dee0d7d5991"

#TODO: Break this into configuration fragments-- one that has the pieces contained
#for a generic Xen dom0, and one that has the per-machine specific options.
SRC_URI += "file://defconfig"

#And work inside the directory where the kernel will be 
S = "${WORKDIR}/linux-${PV}"
