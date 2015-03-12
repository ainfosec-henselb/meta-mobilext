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
  u-boot-mkimage-native \
  virtual/kernel \
  xen \
"

#Specify the files that will be used in the creation of this recipe,
#(but which are not provided by another package.)
FILESPATH = "${THISDIR}/xen-base-boot-arm-image" 
SRC_URI   = "file://boot_machine_invariant.cmd"

#Specify a nicer-looking name for the image.
export IMAGE_BASENAME = "xen-base-boot"

inherit bootimg

#Ensure that the relevant pieces are deployed before they're
#needed by the boot-image-creation. Note that we do not append,
#as we want to override the base class's dependence on syslinux.
do_bootimg[depends] = " \
  dosfstools-native:do_populate_sysroot \
  mtools-native:do_populate_sysroot \
  virtual/kernel:do_deploy \
  virtual/bootloader:do_deploy \
  ${PN}:do_unpack \
  xen:do_deploy \
"

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
# Generate the boot configuration script for u-boot. 
#
generate_boot_configuration() {
	DEST=$1
	install -d ${DEST}

  #Create the raw boot script...
  install -d ${S}/boot 
  BOOT_CFG=${S}/boot/boot.cmd

  #... populate it with all of the machine-dependent values...
  echo "#Determine the addresses into which we'll load the pieces of our system."          >  ${BOOT_CFG}
  echo "#These are currently placeholder values-- they should be optimized for the system" >> ${BOOT_CFG}
  echo "#we're going to be putting them into!"                                             >> ${BOOT_CFG}
  echo "setenv xen_addr_r          \"${UBOOT_XEN_ADDR}\""                                  >> ${BOOT_CFG}
  echo "setenv kernel_addr_r       \"${UBOOT_DOM0_ADDR}\""                                 >> ${BOOT_CFG}
  echo "setenv dtb_addr_r          \"${UBOOT_DTB_ADDR}\""                                  >> ${BOOT_CFG}
  echo                                                                                     >> ${BOOT_CFG}

  echo "#Set up the device that we'll be using to provide the main"                        >> ${BOOT_CFG}
  echo "#Xen and boot files."                                                              >> ${BOOT_CFG}
  echo "setenv boot_device         \"${UBOOT_BOOT_DEVICE}\""                               >> ${BOOT_CFG}
  echo "setenv boot_partition      \"${UBOOT_BOOT_PARTITION}\""                            >> ${BOOT_CFG}
  echo                                                                                     >> ${BOOT_CFG}

  echo "#Configure Xen and dom0."                                                          >> ${BOOT_CFG}
  echo "setenv dom0_memory         \"${XEN_DOM0_MEMORY}\""                                 >> ${BOOT_CFG}
  echo "setenv dom0_root           \"${UBOOT_DOM0_ROOT}\""                                 >> ${BOOT_CFG}
  echo "setenv xen_serial_port     \"${XEN_SERIAL_PORT}\""                                 >> ${BOOT_CFG}
  echo "setenv dom0_extra_bootargs \"${DOM0_EXTRA_BOOTARGS}\""                             >> ${BOOT_CFG}
  echo "setenv xen_extra_bootargs  \"${XEN_EXTRA_BOOTARGS}\""                              >> ${BOOT_CFG}

  echo                                                                                     >> ${BOOT_CFG}
  echo                                                                                     >> ${BOOT_CFG}
  echo "# --- Begin machine-invariant instructions. ---"                                   >> ${BOOT_CFG} 
  echo                                                                                     >> ${BOOT_CFG}
  echo                                                                                     >> ${BOOT_CFG}

  #... populate it with the machine-independent bootloader instructions...
  cat ${WORKDIR}/boot_machine_invariant.cmd >> ${BOOT_CFG}

  #... and compile it into a u-boot script.
  uboot-mkimage -A ${UBOOT_ARCH} -T script -d "${BOOT_CFG}" "${DEST}/boot.scr"
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
