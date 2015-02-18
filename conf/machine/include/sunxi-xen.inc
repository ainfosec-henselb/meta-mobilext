PREFERRED_PROVIDER_virtual/kernel = "linux-yocto-xen"
PREFERRED_PROVIDER_virtual/bootloader = "uboot-sunxi-xen"
PREFERRED_VERSION_linux-libc-headers = "3.4.61"

#The sunxi-u-boot can boot both uImages and zImages, so we'll
#use the more standard format. Note Xen loads zImages, so
#KERNEL_IMAGETYPE should always be zImage for a dom0 platform.
XEN_IMAGETYPE = "zImage"
KERNEL_IMAGETYPE = "zImage"

IMAGE_CLASSES += "sdcard_image-xen-sunxi"
IMAGE_FSTYPES += "ext3 tar.gz sunxi-xen-sdimg"

MACHINE_EXTRA_RRECOMMENDS = "kernel-modules"

UBOOT_ARCH           = "arm"

#Set up the locations into which u-boot should load the kernel components.
#Ideally, these will be overridden on a per-device basis to optimize.
#TODO: Appropriate for 1GiB+ of RAM. Adjust lower to support devices with less RAM.
UBOOT_DOM0_ADDR      = "0x7f600000"
UBOOT_DTB_ADDR       = "0x7ec00000"
UBOOT_XEN_ADDR       = "0x7ea00000"

UBOOT_BOOT_DEVICE    = "mmc"
UBOOT_BOOT_PARTITION = "0"
UBOOT_DOM0_ROOT      = "/dev/mmcblk0p2"

#TODO: Possibly decrease, depending on dom0's requirements?
XEN_DOM0_MEMORY      = "128M"

DOM0_EXTRA_BOOTARGS  = "rootwait"
XEN_EXTRA_BOOTARGS   = "loglvl=all"
XEN_SERIAL_PORT      = "ttyS0"
