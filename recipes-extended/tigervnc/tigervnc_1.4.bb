#
# Recipe for TigerVNC; created by Kyle J. Temkin <temkink@ainfosec.com>
# Useful for Xen debugging and demonstration.
#

DESCRIPTION = "High performance, platform-neutral VNC implementation"
HOMEPAGE = "tigervnc.org"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://LICENCE.TXT;md5=75b02c2872421380bbd47781d2bd75d3"

DEPENDS = "fltk gnutls libgcrypt pixman virtual/libx11 libxtst libxdamage imagemagick-native"

inherit pkgconfig cmake

#Pull TigerVNC down from GitHub.
SRC_URI = "git://github.com/TigerVNC/tigervnc.git;branch=${PV}-branch"
SRCREV = "${AUTOREV}"

#Compile directly in the folder we've pulled down from git.
S = "${WORKDIR}/git"

#TODO: Get the desktop and icon files packaged in nicely!
#TODO: Split this package into server and viewer components.
