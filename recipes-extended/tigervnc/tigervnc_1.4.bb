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

#
# Install the desktop file and its icons.
#
do_install_append() {

    #Install the desktop file...
    install -D ${S}/contrib/packages/rpm/el6/SOURCES/vncviewer.desktop \
        ${D}/usr/share/applications/vncviewer.desktop

    #Iterate over each of the provided icons...
    for ICON in ${S}/media/icons/tigervnc_*.png; do

        #... extract the icon's size...
        SIZE=$(basename ${ICON} | grep -o "[0-9]*")

        echo ICON: ${ICON} $ICON
        echo SIZE: ${SIZE} $SIZE

        #... and copy the icon to the correct location.
        echo install -D ${ICON} ${D}/usr/share/icons/hicolor/${SIZE}x${SIZE}/apps/tigervnc.png
        install -D ${ICON} ${D}/usr/share/icons/hicolor/${SIZE}x${SIZE}/apps/tigervnc.png
    done

}
