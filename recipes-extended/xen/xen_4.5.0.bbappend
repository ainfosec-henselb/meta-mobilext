# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)


# Borrow the kernel recipe's ability to map the current architecture to a u-boot architecture.
inherit kernel-arch deploy

FILES_${PN}-hypervisor += "/boot/xen.uimg"


#
# If we've been asked to build a uImage for the primary kernel, build a uImage for Xen. 
# (We'll create a zImage for the dom0 kernel anyway-- as Xen is capable of loading them.)
#
do_uboot_mkimage() {

	if test "x${KERNEL_IMAGETYPE}" = "xuImage" ; then 
		if (test "x${KEEPUIMAGE}" != "xyes") || ! [ -e xen/xen.uimg ]; then

			ENTRYPOINT=${UBOOT_ENTRYPOINT}
      ##TODO: Support entry symbol behavior for Xen images?
      #(See the kernel implementation of this function.)

      #Compress the kernel image...
      cp xen/xen xen.bin  
      gzip -9 xen.bin

      #... and convert it to a u-boot image.
      uboot-mkimage -A ${UBOOT_ARCH} -O linux -T kernel -C gzip -a ${UBOOT_LOADADDRESS} -e $ENTRYPOINT -n "${DISTRO_NAME}/${PV}/${MACHINE}" -d xen.bin.gz xen/xen.uimg
      rm -f xen.bin.gz

		fi
	fi
}

addtask uboot_mkimage before do_install after do_compile

#
# Copy the new xen.uimg into /boot/ on a normal target.
# This can then be loaded by u-boot as the primary kernel, or
# copied by a seperate boot script.
# 
do_install_append() {
   pwd
   install -m 0644 xen/xen.uimg ${D}/boot/xen.uimg
}

#
# Add our new uImage to the deploy folder.
#
sysroot_stage_all_append() {
    install -d ${DEPLOY_DIR_IMAGE}
    
    #If we have a xen uImage, then install that.
    if [ -f ${D}/boot/xen.uimg ]; then
        install -m 0644 ${D}/boot/xen.gz ${DEPLOY_DIR_IMAGE}/xen-${MACHINE}.uimg
    fi
}

