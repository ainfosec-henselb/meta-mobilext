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
    \
    connman \
    connman-client \
    connman-tools \
"

#
# Demonstration packages, which are useful for demonstrating the functionality of
# the Xen base platform.
#
GUEST_VM_DEMO_PACKAGES = " \
    tightvnc \
    florence \
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


#
# Configure the system so the demo will run automatically.
#
configure_image_for_demo() {

    #Enable automatic root login on all gettys.
    #Unfortunately, this is currently the recommended way to do this-- the only other option is to
    #strip out authentication altogether by an equivalent modification of PAM's configuration.
    sed -i "s/agetty --noclear/agetty --autologin root --noclear/" ${IMAGE_ROOTFS}${systemd_unitdir}/system/getty@.service

    #... and configure our shell to automatically start the xfce on the first tty.
    echo 'cd /install/' >> ${IMAGE_ROOTFS}/etc/profile
    echo '[[ -z $DISPLAY && $XDG_VTNR -eq 1 ]] && exec startxfce4' >> ${IMAGE_ROOTFS}/etc/profile

}
IMAGE_PREPROCESS_COMMAND += "configure_image_for_demo;"
