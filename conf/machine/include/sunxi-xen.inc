PREFERRED_PROVIDER_virtual/xserver = "xserver-xorg"
XSERVER = "xserver-xorg \
           xf86-video-fbturbo \
           xf86-input-evdev \
           xf86-input-mouse \
           xf86-input-keyboard"

PREFERRED_PROVIDER_virtual/kernel = "linux-yocto-xen"
PREFERRED_VERSION_linux-libc-headers = "3.4.61"

#The sunxi-u-boot can boot both uImages and zImages, so we'll
#use the more standard format. Note Xen loads zImages, so
#KERNEL_IMAGETYPE should always be zImage for a dom0 platform.
XEN_IMAGETYPE = "zImage"
KERNEL_IMAGETYPE = "zImage"

IMAGE_CLASSES += "sdcard_image-xen-sunxi"
IMAGE_FSTYPES += "ext3 tar.gz sunxi-xen-sdimg"

MACHINE_EXTRA_RRECOMMENDS = "kernel-modules"
