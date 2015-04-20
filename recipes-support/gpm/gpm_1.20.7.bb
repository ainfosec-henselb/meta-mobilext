DESCRIPTION = "GPM (General Purpose Mouse) is a mouse server \
for the console and xterm, with sample clients included \
(emacs, etc)."

SECTION = "console/utils"
LICENSE = "GPLv2+"
LIC_FILES_CHKSUM = "file://COPYING;md5=18810669f13b87348459e611d31ab760"

#Create the GPM instance right from our custom git.
SRC_URI = "\
    git://github.com/mobilext/gpm.git;branch=add-evdev-absolute \
    file://init \
"

#Use our per-version file directory for the version _minus_ our git data.
FILESPATH = "${THISDIR}/gpm-1.20.7"

#Automatically pull down newer commits from our git repository.
SRCREV = "${AUTOREV}"
PV = "1.20.7+git${SRCPV}"

#... and work out of the git tree we've pulled down.
S = "${WORKDIR}/git"


inherit autotools-brokensep update-rc.d

INITSCRIPT_NAME = "gpm"
INITSCRIPT_PARAMS = "defaults"

#export LIBS = "-lm"

# all fields are /* FIXME: gpm 1.99.13 */
# gpm-1.99.7/src/lib/libhigh.c:171:43: error: parameter 'clientdata' set but not used [-Werror=unused-but-set-parameter]
# gpm-1.99.7/src/lib/report-lib.c:28:21: error: parameter 'line' set but not used [-Werror=unused-but-set-parameter]
# gpm-1.99.7/src/lib/report-lib.c:28:33: error: parameter 'file' set but not used [-Werror=unused-but-set-parameter]
# gpm-1.99.7/src/drivers/empty/i.c:26:23: error: parameter 'fd' set but not used [-Werror=unused-but-set-parameter]
# gpm-1.99.7/src/drivers/empty/i.c:26:42: error: parameter 'flags' set but not used [-Werror=unused-but-set-parameter]
# gpm-1.99.7/src/drivers/etouch/i.c:34:43: error: parameter 'flags' set but not used [-Werror=unused-but-set-parameter]
# gpm-1.99.7/src/drivers/msc/r.c:32:12: error: variable 'dy' set but not used [-Werror=unused-but-set-variable]
# gpm-1.99.7/src/drivers/msc/r.c:32:8: error: variable 'dx' set but not used [-Werror=unused-but-set-variable]
# cc1: all warnings being treated as errors
CFLAGS += "-Wno-extra -Wno-error=unused-but-set-parameter -Wno-error=unused-but-set-variable"

# twiddler is WIP in 1.99.7 and probably not worth fixing (a lot of changes in gpm-2-dev after 1.99.7
# gpm-1.99.7/src/drivers/twid/twiddler.c:503:14: error: cast to pointer from integer of different size [-Werror=int-to-pointer-cast]
# /gpm-1.99.7/src/mice.c:221:5: error: (near initialization for 'mice[32].init') [-Werror]
CFLAGS += "-Wno-error=int-to-pointer-cast -Wno-error"

#
# Configure using old-style autotools configuration; as GPM doesn't match
# the OE hueristic.
#
do_configure() {
    NOCONFIGURE=yes ./autogen.sh
    oe_runconf
}

do_install () {
    oe_runmake 'ROOT=${D}' install
    install -m 0644 src/headers/gpm.h ${D}${includedir}
    install -d ${D}/${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/init ${D}/${sysconfdir}/init.d/gpm
    #cd ${D}${libdir} && ln -sf libgpm.so.1.19.0 libgpm.so.1
}

FILES_${PN} += "${datadir}/emacs"
