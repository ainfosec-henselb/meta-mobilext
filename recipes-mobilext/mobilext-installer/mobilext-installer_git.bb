# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "A simple engine to install Xen platforms."
DESCRIPTION = " \
    The MobileXT installer engine is a simple text-ui installer \
    for Xen-based systems. It can be used to install MobileXT, or \
    several varieties of the Xen base platform. \
"
AUTHOR = "Kyle J. Temkin <temkink@ainfosec.com>"
HOMEPAGE = "http://www.github.com/mobilext/installer"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=3f40d7994397109285ec7b81fdeb3b58"

#Create the installer directly from git.
SRC_URI = "git://github.com/mobilext/installer.git"
SRCREV = "${AUTOREV}"
PV = "0.1+git${SRCPV}"

#Depend on each of the tools used in the installation.
#Most of these are disk management tools. Dialog provides the installer UI;
#GPM (General Purpose Mouse) allows mice or touchscreen control over the
#dialog buttons.
RDEPENDS_${PN} = "\
  bash \
  parted \
  mtools \
  dosfstools \
  e2fsprogs \
  util-linux \
  dialog \
  gpm \
"

#Allow this package to automatically register as a systemd service,
#on systems with systemd.
inherit systemd
SYSTEMD_SERVICE_${PN} = "mobilext-installer.service"

#By default, automatically start the MobileXT installer on system startup.
MOBILEXT_INSTALLER_AUTOSTART ??= "true"
SYSTEMD_AUTO_ENABLE = "${@base_conditional("MOBILEXT_INSTALLER_AUTOSTART", "true", "enable", "", d)}"

#The target directory, relative to the package root.
TARGET_DIRECTORY = "/install"

#Use the git sources directly.
S = "${WORKDIR}/git"

#This package provides an /install directory in the target (typically live) image.
FILES_${PN} = "/install/*"

#
# Since the installer is all bash scripts, installation is surprisingly easy. 
#
do_install() {
    #Build the installation path...
    DEST="${D}${TARGET_DIRECTORY}"

    #... create the parnet directory for the target...
    install -d $(dirname "${DEST}")

    #... copy all of the installer scripts to the target directory, preserving rights.
    #FIXME: Modify me to avoid copying in the .git directory!
    cp -pr "${S}" "${DEST}"

    #... and finally, install the systemd service that automatically starts
    #the installer on livecd boot.
    install -d "${D}${systemd_unitdir}/system"
    install -m 0644 "${S}/config/mobilext-installer.service" "${D}${systemd_unitdir}/system"
}
