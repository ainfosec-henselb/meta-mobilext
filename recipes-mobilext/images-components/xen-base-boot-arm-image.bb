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
	kernel-image \
"
#And ensure we have all the dependencies necessary to create our image.
IMAGE_DEPENDS += " \
  parted-native \
  mtools-native \
  dosfstools-native \
  virtual/bootloader \
  virtual/kernel \
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

  #Copy the resultant xen image onto the boot partition.
  #TODO: Simplify down to xen-${XEN_IMAGETYPE}.
  if [ "x$XEN_IMAGETYPE" = "uImage" ]; then
    install -m 0644 ${STAGING_KERNEL_DIR}/xen.uimg ${DEST}/xen-uImage
  else
    install -m 0644 ${STAGING_KERNEL_DIR}/xen ${DEST}/xen-zImage
  fi

	#Populate the linux kernel image and device tree.
  #TODO: Change device tree extension to DTB.
  install -m 0644 ${STAGING_KERNEL_DIR}/${KERNEL_IMAGETYPE}  ${DEST}/linux-${KERNEL_IMAGETYPE}
  install -m 0644 ${STAGING_KERNEL_DIR}/${KERNEL_DEVICETREE} ${DEST}/deviceTree

	#Add a small file identifying the current build.
	echo "${IMAGE_NAME}-${IMAGEDATESTAMP}" > ${DEST}/image-version-info

}


do_bootimg() {

    #Populate the directory which will be used to create the hard drive image...
		populate ${HDDDIR}

    #Create the raw FAT image from the deployment directory.
		build_fat_img ${HDDDIR} ${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.hddimg
}


FIXME_unsorted() {
    
	#Copy the newly-constructed boot partition into the SD card image.
	dd if=${WORKDIR}/boot.img of=${SDIMG} conv=notrunc seek=1 bs=$(expr ${IMAGE_ROOTFS_ALIGNMENT} \* 1024) && sync && sync

	#If the rootfs is compressed, uncompress it as we create the raw SD image.
	if echo "${SDIMG_ROOTFS_TYPE}" | egrep -q "*\.xz"
	then
		xzcat ${SDIMG_ROOTFS} | dd of=${SDIMG} conv=notrunc seek=1 bs=$(expr 1024 \* ${BOOT_SPACE_ALIGNED} + ${IMAGE_ROOTFS_ALIGNMENT} \* 1024) && sync && sync
	else
		dd if=${SDIMG_ROOTFS} of=${SDIMG} conv=notrunc seek=1 bs=$(expr 1024 \* ${BOOT_SPACE_ALIGNED} + ${IMAGE_ROOTFS_ALIGNMENT} \* 1024) && sync && sync
	fi

	#Write the Secondary Program Loader (which runs before u-boot) and u-boot directly onto the SD card in the area where the A20 will look for it.
	dd if=${DEPLOY_DIR_IMAGE}/u-boot-sunxi-with-spl.bin of=${SDIMG} bs=1024 seek=8 conv=notrunc
    
}
