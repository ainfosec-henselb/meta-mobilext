#
# Newer version of FLTK than provided by OE, currently.
# Once OE moves to >=1.3.2, this can be dropped.
#

SUMMARY = "FLTK is a cross-platform C++ GUI toolkit"
HOMEPAGE = "http://www.fltk.org"
SECTION = "libs"
LICENSE = "LGPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=f6b26344a24a941a01a5b0826e80b5ca"

DEPENDS = "alsa-lib zlib jpeg libpng libxext libxft libxinerama"

PR = "r1"

SRC_URI = " \
    http://fltk.org/pub/fltk/${PV}/fltk-${PV}-source.tar.gz \
    file://fltk-no-freetype-config.patch \
    file://disable_test.patch \
"

SRC_URI[md5sum] = "9f7e707d4fb7a5a76f0f9b73ff70623d"
SRC_URI[sha256sum] = "176044df94f31bd53a5806cf5054ca78b180bf9ae27ce47649608833695ee4a4"

S = "${WORKDIR}/fltk-${PV}"

inherit lib_package autotools-brokensep binconfig pkgconfig

TARGET_CC_ARCH += "${LDFLAGS} -DXFT_MAJOR=2"

EXTRA_OECONF = "--enable-shared \
                --enable-threads \
                --enable-xdbe --enable-xft --enable-gl \
                --x-includes=${STAGING_INCDIR} --x-libraries=${STAGING_LIBDIR}"

do_configure() {
    oe_runconf
}

python populate_packages_prepend () {
    if (d.getVar('DEBIAN_NAMES', 1)):
        d.setVar('PKG_${PN}', 'libfltk${PV}')
}

LEAD_SONAME = "libfltk.so"
