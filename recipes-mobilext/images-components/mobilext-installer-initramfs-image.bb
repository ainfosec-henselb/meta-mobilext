SUMMARY = "A simple root filesystem intended for bootstrapping installations."
DESCRIPTION = " \
   An initial ram filesystem which is intended to provide a base for full-featured \
   installations that run off of a live image-- typically from a CD, SD card, or \
   USB flash drive. \
"
AUTHOR = "Kyle J. Temkin <temkink@ainfosec.com>"

IMAGE_INSTALL = "initramfs-live-boot busybox udev base-passwd"

# Do not pollute the initrd image with rootfs features
IMAGE_FEATURES = ""

export IMAGE_BASENAME = "core-image-minimal-initramfs"
IMAGE_LINGUAS = ""

LICENSE = "MIT"

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES}"
inherit core-image

IMAGE_ROOTFS_SIZE = "8192"

BAD_RECOMMENDATIONS += "busybox-syslog"
