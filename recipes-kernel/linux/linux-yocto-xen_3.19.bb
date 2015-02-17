inherit kernel
require recipes-kernel/linux/linux-yocto.inc

#... and grab the newest revision.
LINUX_VERSION ?= "3.19"
LINUX_VERSION_EXTENSION ?= "-mobilext-${LINUX_KERNEL_TYPE}"

# FIXME: Replace me (and the defconfig entry below) with a single .cfg entry that contains all
# of the necessary options! For now, we're using per-machine preconfigurations, as Xen (and its
# per-machine requirements) is still a moving target.
FILESPATH="${THISDIR}/${MACHINE}"

SRC_URI = " \
    https://www.kernel.org/pub/linux/kernel/v3.x/linux-${PV}.tar.xz \
    file://defconfig \
    "

SRC_URI[md5sum] = "d3fc8316d4d4d04b65cbc2d70799e763"
SRC_URI[sha256sum] = "be42511fe5321012bb4a2009167ce56a9e5fe362b4af43e8c371b3666859806c"

S = "${WORKDIR}/linux-${PV}"

#For now, since we're hardcoding the individual devices, we'll have to manually list
#them here. =( This should go away when the Xen configurations are pinned down
#and abstracted out.
COMPATIBLE_MACHINE = "cubietruck-dom0"
