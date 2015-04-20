# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Universal Bootloader for Allwinner "Sun XI" processors.
# Modified to correctly boot in hypervisor mode.

# Recipe released under the MIT license (see COPYING.MIT for the terms)

#
# TODO: Try to replace me with upstream u-boot!
#

inherit u-boot

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://Licenses/gpl-2.0.txt;md5=b234ee4d69f5fce4486a80fdaf4a4263"

PROVIDES += "virtual/bootloader"

#Depend on the ability to create u-boot images.

SRC_URI = "git://github.com/MobileXT/u-boot-sunxi-virtualization.git;branch=sunxi-next;protocol=git"

#Set up the build environment...
S = "${WORKDIR}/git"

#Set up the package version...
PE = "1"
PV = "git${SRCPV}"
SRCREV = "${AUTOREV}"
PACKAGE_ARCH = "${MACHINE_ARCH}"

# And name the binary that we should generate.
export SPL_BINARY = "u-boot-sunxi-with-spl.bin"

#Set up the pieces needed for debugging u-boot.
#(These are mostly used for the host's reference.)
DEBUG_ELF        = "u-boot"
FILES_${PN}-dbg += " \
    /boot/${DEBUG_ELF} \
    /boot/.debug \
"

#Skip the sanity checks that don't make sense for a binary running outside
#of an OS environment.
INSANE_SKIP_${PN} = "textrel, package_qa_hash_style"

#
# Copy the ELF file used to generate the core u-boot binary into our debug package.
# We'll use this to debug u-boot with GDB.
#
do_install_append() {
    install ${S}/${DEBUG_ELF} ${D}/boot/${DEBUG_ELF}
}


#Create a symlink with a standard naming convention, for consumption by other images.
do_deploy_append() {
    if [ x"${SPL_BINARY}" != x ]; then
        install -d ${DEPLOY_DIR_IMAGE}
        ln -sf ${DEPLOY_DIR_IMAGE}/${SPL_BINARY} ${DEPLOY_DIR_IMAGE}/u-boot-with-spl.bin
        ln -sf ${DEPLOY_DIR_IMAGE}/${SPL_BINARY} ${DEPLOY_DIR_IMAGE}/bootloader-${MACHINE}.bin
    fi
}
