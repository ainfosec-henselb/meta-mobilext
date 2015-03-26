#
# Modification that ensures that ncurses is built with GPM (and thus touchscreen)
# support. This allows ncurses dialogs to be used in tablet installers.
#

#If we're building curses for a /target/, and not for the native SDK,
#include GPM support. TODO: Base this off something like a touchscreen
#machine feature?
GPM_DEPENDS = "gpm"
GPM_DEPENDS_virtclass-native = ""
GPM_DEPENDS_virtclass-nativesdk = ""

DEPENDS += "${GPM_DEPENDS}"

# Helper function for do_configure to allow multiple configurations
# $1 the directory to run configure in
# $@ the arguments to pass to configure
ncurses_configure() {
	mkdir -p $1
	cd $1
	shift
	oe_runconf \
	        --disable-static \
	        --without-debug \
	        --without-ada \
          --with-gpm=libgpm.so.2 \
	        --enable-hard-tabs \
	        --enable-xmc-glitch \
	        --enable-colorfgbg \
	        --with-termpath='${sysconfdir}/termcap:${datadir}/misc/termcap${EX_TERMCAP}' \
	        --with-terminfo-dirs='${sysconfdir}/terminfo:${datadir}/terminfo${EX_TERMINFO}' \
	        --with-shared \
	        --disable-big-core \
	        --program-prefix= \
	        --with-ticlib \
	        --with-termlib=tinfo \
	        --enable-sigwinch \
	        --enable-pc-files \
	        --disable-rpath-hack \
		${EXCONFIG_ARGS} \
	        --with-manpage-format=normal \
	        "$@" || return 1
	cd ..
}
