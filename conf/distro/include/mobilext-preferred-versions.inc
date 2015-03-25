UDEV_GE_141 = "1"

#Use the Yocto project linux-dev (which uses the mainline kernel).
PREFERRED_PROVIDER_virtual/kernel = "linux-yocto-xen"

#Try to use the lower-numbered (but apparently updated and stable)
#version of GPM.
PREFERRED_VERSION_gpm = "1.20.7"


