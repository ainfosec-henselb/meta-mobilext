# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)
#
# Small boot partition for running Xen on an x86 system using
# Syslinux as the bootloader. The resultant .hddimg can be written
# directly to a partition.
#


LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=3f40d7994397109285ec7b81fdeb3b58"

#This package provides a boot partition image suitable for x86 machines.
PROVIDES += "virtual/boot-partition-image"

#This little stub effectively makes us compatible with the current machine, as
#long as the current machine supports an x86-compatbile architecture. If only
#we had COMPATIBLE_ARCH!
COMPATIBLE_MACHINE := "(${@base_contains("AVAILTUNES", "x86", d.getVar("MACHINE"), "", d)})"

#Require the linux kernel and Xen to have been built prior to creating the image.
DEPENDS += " \
  virtual/kernel \
  xen \
"

#Specify the packages to pull into our staging directory.
#We'll pull these apart to build our boot partition.
IMAGE_INSTALL += " \
  packagegroup-xen-hypervisor \
	kernel-image \
  syslinux \
"

#And specify a nicer-looking (*) name for the image.
#* Hey, I said nic_er_.
export IMAGE_BASENAME = "xen-base-boot-syslinux"

#If we're using EFI, we should use a framebuffer for any VGA output;
#as text-mode is not directly supported. Otherwise, leave things as they were.
VGA = "${@base_contains("MACHINE_FEATURES", "efi", "gfx-1024x768-32", "current", d)}"
VGA = ""

#The command line to be passed to Xen. Must not contain three dashes "---", as this
#is syslinux's multiboot separator.
XEN_OPTIONS = "console=com1,vga vga=${VGA} com1=${XEN_SERIAL_PORT} dom0_mem=${XEN_DOM0_MEMORY} ${XEN_EXTRA_BOOTARGS}"

#The command line to be passed to the dom0 kernel. Must not contain three dashes "---".
DOM0_OPTIONS = "console=hvc0 root=${DOM0_ROOT} earlyprintk=xen ${DOM0_EXTRA_BOOTARGS}"

inherit bootimg

# Add the syslinux boot "com" objects to the boot partition.
# TODO: Should this be in syslinux_populate_append? What here is HDDIMG specific?
syslinux_hddimg_populate_append() {
	install -m 0444 ${STAGING_DATADIR}/syslinux/libcom32.c32 ${HDDDIR}${SYSLINUXDIR}
	install -m 0444 ${STAGING_DATADIR}/syslinux/mboot.c32 ${HDDDIR}${SYSLINUXDIR}
}

#Add the xen image to the boot partition.
populate_append() {
	install -m 0644 ${DEPLOY_DIR_IMAGE}/xen-${MACHINE}.gz ${DEST}/xen.gz
}

#
#... and build the bootloader configuration necessary to start Xen.
#

build_syslinux_cfg() {
	echo ALLOWOPTIONS 1 > ${SYSLINUXCFG}
	echo SERIAL 0 115200 >> ${SYSLINUXCFG}
	echo DEFAULT ${SYSLINUX_LABEL} >> ${SYSLINUXCFG}
	echo TIMEOUT ${SYSLINUX_TIMEOUT} >> ${SYSLINUXCFG}
	echo PROMPT 1 >> ${SYSLINUXCFG}
	echo LABEL ${SYSLINUX_LABEL} >> ${SYSLINUXCFG}
	echo KERNEL mboot.c32 >> ${SYSLINUXCFG}
	echo APPEND xen.gz ${XEN_OPTIONS} --- vmlinuz ${DOM0_OPTIONS} >> ${SYSLINUXCFG}
}


build_grub_cfg() {
  echo "serial --unit 0 --speed=115200 --word=8 --parity=no --stop=1" > ${GRUBCFG}
  echo "default=xen" >> ${GRUBCFG}
  echo "timeout=10" >> ${GRUBCFG}
  echo >> ${GRUBCFG}
  echo "menuentry xen {" >> ${GRUBCFG}
  echo "    multiboot /xen.gz ${XEN_OPTIONS}" >> ${GRUBCFG}
  echo "    module /vmlinuz ${DOM0_OPTIONS}" >> ${GRUBCFG}
  echo "}" >> ${GRUBCFG}
}
