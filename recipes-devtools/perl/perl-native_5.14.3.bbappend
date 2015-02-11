
SYSROOT_PREPROCESS_FUNCS += "perl_sysroot_link_pod_utils"

#
# Hack:
# 
# Ensure that each of the perl documentation functions are
# available in the image's path. For now, we'll accomplish
# this with symlinks; though there may be a more OE-friendly
# way to do this.
#
perl_sysroot_link_pod_utils() {
    for i in ${SYSROOT_DESTDIR}${bindir}/pod2*; do
        UTILITY=$(basename $i)

        ln -s "perl-native/$UTILITY" ${SYSROOT_DESTDIR}${bindir}/..
    done
}
