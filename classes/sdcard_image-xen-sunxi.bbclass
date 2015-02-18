inherit image_types

#
# Create an image that can by written onto a SD card using dd.
# Originally written for rasberrypi adapt for the needs of allwinner sunxi based boards
#
# The disk layout used is:
#
#    0                      -> 8*1024                           - reserverd
#    8*1024                 -> 32*1024                          - 
#    32*1024                -> 2048*1024                        - 
#    2048*1024              -> BOOT_SPACE                       - bootloader and kernel
#
#

# This image depends on the rootfs image
IMAGE_TYPEDEP_sunxi-xen-sdimg = "${SDIMG_ROOTFS_TYPE}"

# Boot partition volume id
BOOTDD_VOLUME_ID ?= "${MACHINE}"

# Boot partition size [in KiB]
BOOT_SPACE ?= "20480"

# First partition begin at sector 2048 : 2048*1024 = 2097152
IMAGE_ROOTFS_ALIGNMENT = "2048"

# Use an uncompressed ext3 by default as rootfs
SDIMG_ROOTFS_TYPE ?= "ext3"
SDIMG_ROOTFS = "${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.${SDIMG_ROOTFS_TYPE}"

IMAGE_DEPENDS_sunxi-xen-sdimg += " \
    parted-native \
    mtools-native \
    dosfstools-native \
    xen \
    virtual/kernel \
    u-boot-sunxi-xen \
    "

# SD card image name
SDIMG = "${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.sunxi-xen-sdimg"

IMAGEDATESTAMP = "${@time.strftime('%Y.%m.%d',time.gmtime())}"

IMAGE_CMD_sunxi-xen-sdimg () {

	# Align partitions
	BOOT_SPACE_ALIGNED=$(expr ${BOOT_SPACE} + ${IMAGE_ROOTFS_ALIGNMENT} - 1)
	BOOT_SPACE_ALIGNED=$(expr ${BOOT_SPACE_ALIGNED} - ${BOOT_SPACE_ALIGNED} % ${IMAGE_ROOTFS_ALIGNMENT})
	SDIMG_SIZE=$(expr ${IMAGE_ROOTFS_ALIGNMENT} + ${BOOT_SPACE_ALIGNED} + $ROOTFS_SIZE + ${IMAGE_ROOTFS_ALIGNMENT})

	# Initialize sdcard image file
	dd if=/dev/zero of=${SDIMG} bs=1 count=0 seek=$(expr 1024 \* ${SDIMG_SIZE})

	# Create partition table
	parted -s ${SDIMG} mklabel msdos

	# Create boot partition and mark it as bootable
	parted -s ${SDIMG} unit KiB mkpart primary fat32 ${IMAGE_ROOTFS_ALIGNMENT} $(expr ${BOOT_SPACE_ALIGNED} \+ ${IMAGE_ROOTFS_ALIGNMENT})
	parted -s ${SDIMG} set 1 boot on

	# Create rootfs partition
	parted -s ${SDIMG} unit KiB mkpart primary ext2 $(expr ${BOOT_SPACE_ALIGNED} \+ ${IMAGE_ROOTFS_ALIGNMENT}) $(expr ${BOOT_SPACE_ALIGNED} \+ ${IMAGE_ROOTFS_ALIGNMENT} \+ ${ROOTFS_SIZE})
	parted ${SDIMG} print

	# Create a FAT-32 partition, which will be used for boot files.
  # Eventually, we may be able to merge this into the rootfs, if desired.
	BOOT_BLOCKS=$(LC_ALL=C parted -s ${SDIMG} unit b print | awk '/ 1 / { print substr($4, 1, length($4 -1)) / 512 /2 }')
	mkfs.vfat -n "${BOOTDD_VOLUME_ID}" -S 512 -C ${WORKDIR}/boot.img $BOOT_BLOCKS

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
