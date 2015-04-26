# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)
#

SUMMARY = "Image for a more full-featured Xen control domain, intended to demo the platform."
DESCRIPTION = " \
    This image contains a GUI-based Xen control domain, with all of the software necessary \
    to get a graphical view of each guest domain. This is not intended as a basis for a \
    secure platform-- it currently runs a full X server in the control domain. \
    \
    The image also contains a few development tools intended to be useful in scoping out \
    platform hardware and diagnosing hardware issues. This allows the demonstration image \
    to also act as a useful test of platform features. \
"
AUTHOR = "Kyle J. Temkin <temkink@ainfosec.com>"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=3f40d7994397109285ec7b81fdeb3b58"

require xen-dom0-image.inc

#Group together the packages Angstrom includes in its XFCE development image,
#minus the large SDK image. The remainder of the tools may be useful in bringing
#hardware support to new platforms.
ANGSTROM_XFCE_PACKAGES += " \
  	xinput-calibrator \
  	systemd-analyze \
  	\
  	packagegroup-xfce-base \
  	packagegroup-gnome-xserver-base \
  	packagegroup-core-x11-xserver \
  	packagegroup-gnome-fonts \
  	angstrom-gnome-icon-theme-enable gtk-engine-clearlooks gtk-theme-clearlooks angstrom-clearlooks-theme-enable \
  	\
  	angstrom-gdm-autologin-hack angstrom-gdm-xfce-hack gdm \
  	\
  	bash \
    pciutils \
  	usbutils \
  	i2c-tools \
  	parse-edid \
  	memtester \
  	alsa-utils \
  	devmem2 \
  	iw \
  	bonnie++ \
  	hdparm \
  	iozone3 \
  	iperf \
  	lmbench \
  	rt-tests \
  	evtest \
  	bc \
  	fb-test \
  	tcpdump \
  	procps \
  	util-linux \
  	coreutils \
  	ethtool \
  	bridge-utils \
  	wget \
  	screen \
  	minicom \
  	rsync \
  	vim vim-vimrc \
  	\
  	git \
  	\
  	e2fsprogs-mke2fs \
  	dosfstools \
  	parted \
  	xfsprogs \
  	btrfs-tools \
  	\
  	python-core python-modules \
"

#
# Demonstration packages, which are useful for demonstrating the functionality of
# the Xen base platform.
#
GUEST_VM_DEMO_PACKAGES = " \
    tightvnc \
"

IMAGE_INSTALL += "\
    ${ANGSTROM_XFCE_PACKAGES} \
    ${GUEST_VM_DEMO_PACKAGES} \
"

export IMAGE_BASENAME = "xen-demo-dom0"

IMAGE_PREPROCESS_COMMAND += "do_delete_gnome_session ; "

do_delete_gnome_session () {
	rm -f ${IMAGE_ROOTFS}${datadir}/xsessions/gnome.desktop
}

