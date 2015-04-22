#
# Experimental: fix and unblacklist polkit-gnome.
#

#Fix the polkit breakage...
SRC_URI += "file://fix-broken-gnome-debug-macro.patch"
FILESEXTRAPATHS_prepend := "${THISDIR}/polkit-gnome-0.102:"

#... and unblacklist it.
PNBLACKLIST[polkit-gnome] = ""
