# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Recipe released under the MIT license (see COPYING.MIT for the terms)

# Borrow the kernel recipe's ability to map the current architecture to a u-boot architecture.
inherit kernel-arch deploy

#If we've built an EFI image, package it in the hypervisor package.
FILES_${PN}-hypervisor = "/usr/lib64/efi/xen*.efi"

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
# Copy any additional images (e.g. Xen EFI images) created to the deploy directory.
#
do_deploy() {

  #If we've generated a Xen EFI image, deploy it.
  if [ -e ${D}/usr/lib64/efi/xen.efi ]; then
    install -m 664 ${D}/usr/lib64/efi/xen.efi ${DEPLOY_DIR_IMAGE}/xen.efi 
  fi

  #Deploy any Xen zImages.
  if [ -e ${D}/boot/xen ]; then
    install -m 664 ${D}/boot/xen ${DEPLOY_DIR_IMAGE}/xen-zImage
  fi

}
addtask do_deploy after do_install

#TODO: Determine if this can be removed.
#sysroot_stage_all_append() {
#  if [ -e ${D}/usr/lib64/xen.efi ]; then
#    install -m 664 ${D}/usr/lib64/xen.efi ${DEPLOY_DIR_IMAGE}/xen.efi 
#  fi
#}
