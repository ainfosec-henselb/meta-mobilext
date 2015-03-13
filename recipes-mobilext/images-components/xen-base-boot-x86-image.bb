# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "Small boot partition for running Xen on an x86 system."
DESCRIPTION = " \
 Small boot partition for running Xen on an x86 system using \
 syslinux or grub-efi as the bootloader. The resultant .hddimg can be \
 written directly to a partition. \
"
AUTHOR = "Kyle J. Temkin <temkink@ainfosec.com>"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=3f40d7994397109285ec7b81fdeb3b58"

#This package provides a boot partition image suitable for x86 machines.
PROVIDES += "virtual/boot-partition-image"

#Ensure these boot images are only ever built for x86.
COMPATIBLE_HOST = "(x86_64.*|i.86.*)" 

#Ensure that the relevant pieces are deployed before they're
#needed by the boot-image-creation.
do_bootimg[depends] += " \
  virtual/kernel:do_deploy \
  xen:do_deploy \
"
#Specify the packages to pull into our staging directory.
#We'll pull these apart to build our boot partition.
IMAGE_INSTALL += " \
  packagegroup-xen-hypervisor \
	kernel-image \
"

#Specify a nicer-looking (*) name for the image.
#* Hey, I said nic_er_.
export IMAGE_BASENAME = "xen-base-boot"

#Specify the path at which we should create Xen-EFI configuration files.
export XEN_EFI_CFG="${S}/xen.cfg"

#The command line to be passed to Xen. Must not contain three dashes "---", as this
#is syslinux's multiboot separator.
XEN_OPTIONS = "console=vga,com1 com1=${XEN_SERIAL_PORT} dom0_mem=${XEN_DOM0_MEMORY} ${XEN_EXTRA_BOOTARGS}"

#The command line to be passed to the dom0 kernel. Must not contain three dashes "---".
DOM0_OPTIONS = "console=hvc0 root=${DOM0_ROOT} earlyprintk=xen ${DOM0_EXTRA_BOOTARGS}"

inherit bootimg

# Unfortunately, the core bootimg class assumes we'll be using grub-efi, rather
# than booting the target directly. Here, we'll directly override this.
grubefi_hddimg_populate() {
  xenefi_populate 
}

build_grub_cfg() {
  build_xen_cfg
}


# Add the syslinux boot "com" objects to the boot partition.
# TODO: Should this be in syslinux_populate_append? What here is HDDIMG specific?
syslinux_hddimg_populate_append() {
	install -m 0444 ${STAGING_DATADIR}/syslinux/libcom32.c32 ${HDDDIR}${SYSLINUXDIR}
	install -m 0444 ${STAGING_DATADIR}/syslinux/mboot.c32 ${HDDDIR}${SYSLINUXDIR}
}


#Add the xen image to the boot partition. If we're using BIOS, then populate a gzip'd
#xen.img...
syslinux_populate_append() {
	install -m 0644 ${DEPLOY_DIR_IMAGE}/bzImage ${DEST}/vmlinuz
	install -m 0644 ${DEPLOY_DIR_IMAGE}/xen-${MACHINE}.gz ${DEST}/xen.gz
}

#... and if we're using UEFI, populate the xen EFI image and configuration.
xenefi_populate() {

  #Create the EFI boot directory, which is unfortunately needed by
  #many UEFI clients. 
  export EFI_DEST="${DEST}/EFI/BOOT"
  install -d ${EFI_DEST}

  #And populatge it with our boot components.
  install -m 0644 ${XEN_EFI_CFG} ${EFI_DEST}/bootx64.cfg
  install -m 0644 ${DEPLOY_DIR_IMAGE}/xen.efi ${EFI_DEST}/bootx64.efi 
	install -m 0644 ${DEPLOY_DIR_IMAGE}/bzImage ${EFI_DEST}/vmlinuz
}



#
# Build the bootloader configuration necessary to start Xen.
#

#(These would look cleaner with Heredocs, but then I'd have to use tabs instead
#of spaces, and no one wants that.)

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

build_xen_cfg() {
  echo "[global]" > ${XEN_EFI_CFG}
  echo "default=main" >> ${XEN_EFI_CFG}
  echo 
  echo "[main]" >> ${XEN_EFI_CFG}
  echo "options=${XEN_OPTIONS}" >> ${XEN_EFI_CFG}
  echo "kernel=vmlinuz ${DOM0_OPTIONS}" >> ${XEN_EFI_CFG}

  #TODO: Generalize this to a machine variable?
  #echo "video=gfx-1024x768x32" >> ${XEN_EFI_CFG}
}

#
# Override the core population logic; we'll replace it on a per-bootloader basis above.
#
populate() {
	DEST=$1
	install -d ${DEST}
}

#Create simple, toolchain-independent symlinks to the boot image that can be consumed by other images.
do_bootimg_append() {
  ln -sf ${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.hddimg ${DEPLOY_DIR_IMAGE}/${IMAGE_BASENAME}-${MACHINE}.hddimg
  ln -sf ${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.hddimg ${DEPLOY_DIR_IMAGE}/boot-${MACHINE}.hddimg
}
