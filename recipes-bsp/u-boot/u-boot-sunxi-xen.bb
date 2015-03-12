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
#FIXME: Remove me once the mkimage is moved to virtual/boot-partition-image?
DEPENDS += "u-boot-mkimage-native"

#
# TODO: Get the virtualization changes merged into SunXI, so everything will work!
#
SRC_URI = " \
    git://github.com/MobileXT/u-boot-sunxi-virtualization.git;branch=sunxi-next;protocol=git \
    file://boot.cmd \
    "

#Set up the build environment...
FILESPATH := "${THISDIR}/u-boot-scripts"
S = "${WORKDIR}/git"

#Set up the package version...
PE = "1"
PV = "git${SRCPV}"
SRCREV = "${AUTOREV}"
PACKAGE_ARCH = "${MACHINE_ARCH}"

# And name the binary that we should generate.
SPL_BINARY="u-boot-sunxi-with-spl.bin"

#
# The variables in POPULATE_BOOT_VARIABLES will be inserted into the boot script
# during do_populate_bootscript(). This allows us to have a single generic u-boot
# script which pull machine-dependent information from the environment.
#
POPULATE_BOOT_VARIABLES = " \
    UBOOT_XEN_ADDR \
    UBOOT_DOM0_ADDR \
    UBOOT_DTB_ADDR \
    UBOOT_BOOT_DEVICE \
    UBOOT_BOOT_PARTITION \
    XEN_DOM0_MEMORY \
    UBOOT_DOM0_ROOT \
    XEN_SERIAL_PORT \
    DOM0_EXTRA_BOOTARGS \
    XEN_EXTRA_BOOTARGS \
    "

#
# Populate the boot script that will be used by u-boot by substituting
# certain @-surrounded variables in the boot-script with values taken
# from the bitbake environment. 
#
# See POPULATE_BOOT_VARIABLES for the list of variables which can be
# booted.
#
python do_populate_bootscript() {

    source_script = "%s/boot.cmd" % d.getVar("WORKDIR", d, 1)
    new_script    = "%s/boot.cmd" % d.getVar("S", d, 1)

    #Create a working copy of our boot script.
    os.system("cp \"%s\" \"%s\"" % (source_script, new_script))

    failures = []

    #Itereate over all of the exposed boot configuration variables...
    boot_variables = d.getVar("POPULATE_BOOT_VARIABLES", d, 1).split()
    for variable in boot_variables:

        # If the given variable could not be resolved, this is an error!
        # We'll add this to our list of failures and keep going, so we can
        # give the user a sane error message at the end.
        if d.getVar(variable, d, 1) is None:
            failures.append(variable)
            continue

        #... and populate their placeholders in the booot script.
        replacement = d.getVar(variable, d, 1)

        os.system("sed -i s#@%s@#%s# %s" % (variable, replacement, new_script))


    #If there were failures, notify the user and abort!
    if failures:
        bb.fatal("Your machine must define the machine-dependent varaible(s) %s before you can build images with Xen." % failures)
}

addtask populate_bootscript after do_patch before do_install

#
# As a final step after install, generate our custom boot-script image.
#
do_install_append() {
    #Convert our boot-script to its final image.
    uboot-mkimage -A ${UBOOT_ARCH} -T script -d "${S}/boot.cmd" "${D}/boot/boot.scr"
}

#
# Deploy our newly-created boot-script to our deploy directory.
#
do_deploy_append() {
    install ${D}/boot/boot.scr ${DEPLOY_DIR_IMAGE}
}
