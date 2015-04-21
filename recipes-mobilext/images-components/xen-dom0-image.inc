# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)
#
# Core functionality for a Xen domain zero image.


#Mark this package as a package providing a usable dom0.
PROVIDES += "virtual/dom0-image"

#And ensure that the image's rootfs has an up-to-date timestamp.
IMAGE_PREPROCESS_COMMAND = "rootfs_update_timestamp ;"

#Enable development features on development distributions.
IMAGE_FEATURES += "${@base_conditional("DISTRO_TYPE", "debug", "empty-root-password debug-tweaks", "", d)}"

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

#Set the image free space according to the local configuration--
#the desired free space may vary from machine to machine.
IMAGE_ROOTFS_EXTRA_SPACE = "${DOM0_FREE_SPACE}"

#Override the list of aceptable fstypes with the image type
#selected for dom0; assuming ext4 if none was provided.
DOM0_IMAGE_TYPE ?= "ext4"
IMAGE_FSTYPES = "${DOM0_IMAGE_TYPE}"

TARGET_HOSTNAME = "${DISTRO}-dom0"

# Systemd journal is preferred.
BAD_RECOMMENDATIONS += "busybox-syslog"

IMAGE_DEV_MANAGER   = "udev"
IMAGE_INIT_MANAGER  = "systemd"
IMAGE_INITSCRIPTS   = " "
IMAGE_LOGIN_MANAGER = "busybox shadow"

inherit override-hostname
inherit image
inherit deploy

#Create simple, toolchain-independent symlinks to the boot image that can be consumed by other images.
do_deploy() {

    for IMAGE_TYPE in ${IMAGE_FSTYPES}; do

        #Compute the path the image will have, if it's been created...
        SOURCE_IMAGE=${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.${IMAGE_TYPE}

        #... and if it has been created, create our symlink.
        if [[ -e $SOURCE_IMAGE ]]; then
            ln -sf ${SOURCE_IMAGE} ${DEPLOY_DIR_IMAGE}/dom0-${MACHINE}.${IMAGE_TYPE}
        fi

    done
}
addtask do_deploy after do_rootfs before do_build
