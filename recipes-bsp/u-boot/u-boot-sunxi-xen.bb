# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Universal Bootloader for Allwinner "SunXi" processors running the Xen hypervisor.
#
# Recipe released under the MIT license (see COPYING.MIT for the terms)

inherit u-boot

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://Licenses/gpl-2.0.txt;md5=b234ee4d69f5fce4486a80fdaf4a4263"

PROVIDES += "virtual/bootloader"

#Depend on the ability to create u-boot images.

#
# TODO: Get the virtualization changes merged into SunXI, so everything will work!
#
SRC_URI = "git://github.com/MobileXT/u-boot-sunxi-virtualization.git;branch=sunxi-next;protocol=git"

#Set up the build environment...
S = "${WORKDIR}/git"

#Set up the package version...
PE = "1"
PV = "git${SRCPV}"
SRCREV = "${AUTOREV}"
PACKAGE_ARCH = "${MACHINE_ARCH}"

# And name the binary that we should generate.
SPL_BINARY="u-boot-sunxi-with-spl.bin"

