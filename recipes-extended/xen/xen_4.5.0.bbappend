# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Recipe released under the MIT license (see COPYING.MIT for the terms)


# Borrow the kernel recipe's ability to map the current architecture to a u-boot architecture.
inherit kernel-arch deploy

FILES_${PN}-hypervisor += "/boot/xen.uimg"
XEN_IMAGETYPE ?= "zImage"


#
# Set up configuration options according to our local setup.
#
do_configure_prepend() {

  CONFIGFILE="${S}/.config"

  #Ensure that a configuration file exists.
  touch $CONFIGFILE

  #If we have a debug distro...
  if [ x"${DISTRO_TYPE}" = x"debug" ]; then
    
    #... enable debugging...
    echo "debug = y" >> $CONFIGFILE

    #... and enable the architecture-specific debug options.
    # Note that this is dependent on $ARCH, a Xen-specific variable (and not
    # a bitbake one, like $PACKAGE_ARCH). These are the options available for
    # the Xen "arm" architecture, which includes aarch64.
    if [ x"${ARCH}" = x"arm" ]; then
        echo "export CONFIG_EARLY_PRINTK=${XEN_EARLYPRINTK}" >> $CONFIGFILE
    fi

  fi  
}


#
# If we've been asked to build a uImage for the primary kernel, build a uImage for Xen. 
# (We'll create a zImage for the dom0 kernel anyway-- as Xen is capable of loading them.)
#
# TODO: Decide if this is worth keeping around-- it adds another "surface" to be maintained,
#       in exchange for adding support for older versions of u-boot (like those used on the
#       Samsung Chromebook.) If so, should this be here, or should it be the responsibility
#       of the Chromebook BSP?
#
do_uboot_mkimage() {

	if test "x${XEN_IMAGETYPE}" = "xuImage" ; then 
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

  if test "x${XEN_IMAGETYPE}" = "xuImage" ; then 
    install -m 0644 xen/xen.uimg ${D}/boot/xen.uimg
  fi

}
