# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Recipe released under the MIT license (see COPYING.MIT for the terms)

# Borrow the kernel recipe's ability to map the current architecture to a u-boot architecture.
inherit kernel-arch deploy

#If we've built an EFI image, package it in the hypervisor package.
FILES_${PN}-hypervisor = "/usr/lib64/efi/xen*.efi"

#If the package is configured to use the system's QEMU, use the QEMU package rather than
#building the QEMU provided with Xen.
EXTRA_OECONF_remove = "--with-system-qemu=/usr/bin/qemu-system-i386"
PACKAGECONFIG[system_qemu] = "--with-system-qemu=/usr/bin/qemu-system-i386,,,qemu"

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
    echo "optimize = n" >> $CONFIGFILE


    #... including the crash debugger, if we're not on ARM...
    if [ x"${ARCH}" != x"arm" ]; then
        echo "crash_debug = y" >> $CONFIGFILE
    fi

    #... and enable the architecture-specific debug options.
    # Note that this is dependent on $ARCH, a Xen-specific variable (and not
    # a bitbake one, like $PACKAGE_ARCH). These are the options available for
    # the Xen "arm" architecture, which includes aarch64.
    if [ x"${ARCH}" = x"arm" ]; then
        echo "export CONFIG_EARLY_PRINTK=${XEN_EARLYPRINTK}" >> $CONFIGFILE
    fi

  fi  
}

#Fix an error within the Xen tools' QEMU, which unfortunately sets an bad default
#if PKG_CONFIG is not populated. We'll provide a saner default.
do_compile_prepend() {
    export PKG_CONFIG="pkg-config"
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
