#
# Include for distributions with Xen support.
#

#Ensure that local QEMU builds prefer having Xen enabled.
PACKAGECONFIG_append_pn-qemu ?= "xen"

