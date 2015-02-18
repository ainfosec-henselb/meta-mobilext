# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)

#
# <machine-generated environment setup>
#

#Determine the addresses into which we'll load the pieces of our system.
#These are currently placeholder values-- they should be optimized for the system
#we're going to be putting them into!
setenv xen_addr_r          "@UBOOT_XEN_ADDR@"
setenv kernel_addr_r       "@UBOOT_DOM0_ADDR@"
setenv dtb_addr_r          "@UBOOT_DTB_ADDR@"

#Set up the device that we'll be using to 
setenv boot_device         "@UBOOT_BOOT_DEVICE@"
setenc boot_partition      "@UBOOT_BOOT_PARTITION@"

#Configure Xen and dom0.
setenv dom0_memory         "@XEN_DOM0_MEMORY@"
setenv dom0_root           "@UBOOT_DOM0_ROOT@"
setenv xen_serial_port     "@XEN_SERIAL_PORT@"
setenv dom0_extra_bootargs "@DOM0_EXTRA_BOOTARGS@"
setenv xen_extra_bootargs  "@XEN_EXTRA_BOOTARGS@"

#
# </machine-generated-environment-setup>
#

#Create a pair of environment variables which will hold our boot arguments.
#These are mostly for convenience-- they make it easier for the user to edit the command 
#line. They're used directly below.
setenv xen_bootargs  "console=dtuart dtuart=$xen_serial dom0_mem=$dom0_memory $xen_extra_bootargs" 
setenv dom0_bootargs "console=hvc0 ignore_loglevel psci=enable clk_ignore_unused root=$dom0_root earlyprintk=xen $dom0_extra_bootargs"

#Load each of the images from the USB stick.
#Note that we use $filesize below, so the order matters.
fatload $boot_device $boot_partition $xen_addr_r xen-zImage
fatload $boot_device $boot_partition $dtb_addr_r deviceTree
fatload $boot_device $boot_partition $kernel_addr_r linux-zImage
setenv kernel_size $filesize 

#Now, we'll rewrite the flat device tree as a simple way of passing
#options to Xen.

#Open the device tree, and make sure it's the largest size possible.
fdt addr $dtb_addr_r
fdt resize

#Ensure we have a chosen section, in case the device does not.
fdt chosen

#Set up the Xen boot arguments.
fdt set /chosen xen,xen-bootargs \"$xen_bootargs\"

#And set up the kernel to be loaded.
fdt set /chosen '#address-cells' <1>
fdt set /chosen '#size-cells' <1>  
fdt mknode /chosen module@0
fdt set /chosen/module@0 compatible "xen,linux-zimage" "xen,multiboot-module"
fdt set /chosen/module@0 reg <$kernel_addr_r 0x$kernel_size>                                             
fdt set /chosen/module@0 bootargs \"$dom0_bootargs\"

#Finally, boot xen.
bootz $xen_addr_r - $dtb_addr_r
