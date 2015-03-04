# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)
#
# Base Xen platform, including a simple dom0.
#

KERNEL_IMAGETYPE = "bzImage"
XEN_IMAGETYPE = "gzip"

#Use a dynamically populated devfs, rather than a device tree on x86.
USE_DEVFS = "1"

