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

#Ensure that this image is only used on ARM architectures.
COMPATIBLE_HOST = "(arm.*)"


#Specify the packages to pull into our staging directory.
#We'll pull these apart to build our boot partition.
IMAGE_INSTALL += " \
  packagegroup-xen-hypervisor \
  kernel-devicetree \
	kernel-image \
"
#And ensure we have all the dependencies necessary to create our image.
IMAGE_DEPENDS += " \
  parted-native \
  mtools-native \
  dosfstools-native \
  virtual/bootloader \
  virtual/kernel \
  xen \
"

#Specify a nicer-looking name for the image.
export IMAGE_BASENAME = "xen-base-boot"

inherit bootimg

#Ensure that the relevant pieces are deployed before they're
#needed by the boot-image-creation. Note that we do not append,
#as we want to override the base class's dependence on syslinux.
do_bootimg[depends] = " \
  dosfstools-native:do_populate_sysroot \
  mtools-native:do_populate_sysroot \
  cdrtools-native:do_populate_sysroot \
  ${@oe.utils.ifelse(d.getVar('COMPRESSISO'),'zisofs-tools-native:do_populate_sysroot','')} \
  virtual/kernel:do_deploy \
  virtual/bootloader:do_deploy \
  xen:do_deploy \
"


FIXME_populate_boot () {

  #Copy the resultant xen image onto the boot partition.
  #TODO: Simplify down to xen-${XEN_IMAGETYPE}.
  if [ "x$XEN_IMAGETYPE" = "uImage" ]; then
    mcopy -i ${WORKDIR}/boot.img -s ${STAGING_DIR_HOST}/kernel/xen.uimg ::xen-uImage
  else
    mcopy -i ${WORKDIR}/boot.img -s ${STAGING_DIR_HOST}/kernel/xen ::xen-zImage
  fi
  
  #Copy the dom0 image onto the boot partition...
	mcopy -i ${WORKDIR}/boot.img -s ${STAGING_DIR_HOST}/usr/src/kernel/${KERNEL_IMAGETYPE} ::linux-${KERNEL_IMAGETYPE}

  #Copy the device tree onto the boot partition...
	mcopy -i ${WORKDIR}/boot.img -s ${IMAGE_ROOTFS}/boot/${KERNEL_DEVICETREE} ::deviceTree

  #Finally, copy our boot script to the boot partition.
	mcopy -i ${WORKDIR}/boot.img -s ${DEPLOY_DIR_IMAGE}/boot.scr ::boot.scr

	#Add a small file identifying the current build.
	echo "${IMAGE_NAME}-${IMAGEDATESTAMP}" > ${WORKDIR}/image-version-info
	mcopy -i ${WORKDIR}/boot.img -v ${WORKDIR}/image-version-info ::


}

#
# Populate the provided working directory with the data to be placed into a hard drive image.
#
populate() {
	DEST=$1
	install -d ${DEST}

  #TODO: Add support for uImage kernel types.

  #Copy the resultant xen image onto the boot partition.
  install -m 0644 ${DEPLOY_DIR_IMAGE}/xen-${XEN_IMAGETYPE} ${DEST}/xen-${XEN_IMAGETYPE}

	#Populate the linux kernel image and device tree.
  #TODO: Change device tree extension to DTB.
  install -m 0644 ${STAGING_KERNEL_DIR}/${KERNEL_IMAGETYPE}  ${DEST}/linux-${KERNEL_IMAGETYPE}
  install -m 0644 ${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE}-${KERNEL_DEVICETREE} ${DEST}/deviceTree

	#Add a small file identifying the current build.
	echo "${IMAGE_NAME}-${IMAGEDATESTAMP}" > ${DEST}/image-version-info

}

#
# Generate the boot configuration script for the 
#
generate_boot_configuration() {
	DEST=$1
	install -d ${DEST}

  #FIXME: This should be generated /here/, not in the core package.
  #For now, we'll copy for compatibility, but this really should be fixed.
  install -m 0644 ${DEPLOY_DIR_IMAGE}/boot.scr ${DEST}/boot.scr
}

#
# Create the boot partition image.
#
do_bootimg() {

    #Populate the directory which will be used to create the hard drive image...
		populate ${HDDDIR}

    #... and generate the u-boot configuration for the given 
    generate_boot_configuration ${HDDDIR}

    #Create the raw FAT image from the deployment directory.
		build_fat_img ${HDDDIR} ${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.hddimg
}
