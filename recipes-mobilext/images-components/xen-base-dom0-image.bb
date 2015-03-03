# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)
#
# Base Xen platform, including a simple dom0.
#

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=3f40d7994397109285ec7b81fdeb3b58"

IMAGE_PREPROCESS_COMMAND = "rootfs_update_timestamp ;"

DISTRO_UPDATE_ALTERNATIVES ??= ""
ROOTFS_PKGMANAGE_PKGS ?= '${@base_conditional("ONLINE_PACKAGE_MANAGEMENT", "none", "", "${ROOTFS_PKGMANAGE} ${DISTRO_UPDATE_ALTERNATIVES}", d)}'

CONMANPKGS ?= "connman connman-angstrom-settings connman-plugin-loopback connman-plugin-ethernet connman-plugin-wifi"
CONMANPKGS_libc-uclibc = ""

IMAGE_INSTALL += " \
	angstrom-packagegroup-boot \
	packagegroup-basic \
	${ROOTFS_PKGMANAGE_PKGS} \
  update-alternatives-cworth \
  packagegroup-xen-tools \
	systemd-analyze \
	cpufreq-tweaks \
  kernel-modules \
	${CONMANPKGS} \
	fixmac \
"

#Do not install the dom0 kernel into /boot, as we'll provide that in a separate location.
PACKAGE_EXCLUDE="kernel-image"

# Moved into the Xen boot image:
#  packagegroup-xen-hypervisor 
#
#    If we're on ARM, include a flattened device tree.
#    IMAGE_INSTALL += "${@base_conditional("ARCH", "arm", "kernel-devicetree", "", d)}"

# Systemd journal is preferred.
BAD_RECOMMENDATIONS += "busybox-syslog"

IMAGE_DEV_MANAGER   = "udev"
IMAGE_INIT_MANAGER  = "systemd"
IMAGE_INITSCRIPTS   = " "
IMAGE_LOGIN_MANAGER = "busybox shadow"

export IMAGE_BASENAME = "xen-base"

inherit image
