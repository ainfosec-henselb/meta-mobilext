PREFERRED_PROVIDER_virtual/bootloader = "u-boot"

#Ensure that we get a version of u-boot that's new enough to properly 
#support the virtualization extensions.
PREFERRED_VERSION_u-boot = "v2015.04%"

#The sunxi-u-boot can boot both uImages and zImages, so we'll
#use the more standard format. Note Xen loads zImages, so
#KERNEL_IMAGETYPE should always be zImage for a dom0 platform.
XEN_IMAGETYPE    = "zImage"
KERNEL_IMAGETYPE = "zImage"

IMAGE_FSTYPES += "ext4"

MACHINE_EXTRA_RRECOMMENDS = "kernel-modules"

UBOOT_ARCH           = "arm"

UBOOT_BOOT_DEVICE    ?= "mmc"
UBOOT_BOOT_PARTITION ?= "0"
UBOOT_DOM0_ROOT      ?= "/dev/mmcblk0p2"

#TODO: Possibly decrease, depending on dom0's requirements?
XEN_DOM0_MEMORY      ?= "128M"

DOM0_EXTRA_BOOTARGS  ?= "rootwait"
XEN_EXTRA_BOOTARGS   ?= "loglvl=all"
XEN_SERIAL_PORT      ?= "ttyS0"
