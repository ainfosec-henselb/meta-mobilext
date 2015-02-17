UDEV_GE_141 = "1"

#Use the Yocto project linux-dev (which uses the mainline kernel), rather than
#the Sunxi version. For now, use the furthest upstream with have.
PREFERRED_PROVIDER_virtual/kernel = "linux-yocto-xen"


