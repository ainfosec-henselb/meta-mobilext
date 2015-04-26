
#Require the core u-boot definition from the OE core.
require recipes-bsp/u-boot/u-boot.inc

#... and the Device Tree Compiler.
DEPENDS += "dtc-native"

# This revision corresponds to the tag "v2015.04"
# We use the revision in order to avoid having to fetch it from the repo during parse.
SRCREV = "f33cdaa4c3da4a8fd35aa2f9a3172f31cc887b35"

PV = "v2015.04+git${SRCPV}"

#
#Create a symlink with a standard naming convention, for consumption by other images.
#
do_deploy_append() {
    if [ x"${SPL_BINARY}" != x ]; then
        install -d ${DEPLOY_DIR_IMAGE}
        ln -sf ${DEPLOY_DIR_IMAGE}/${SPL_BINARY} ${DEPLOY_DIR_IMAGE}/u-boot-with-spl.bin
        ln -sf ${DEPLOY_DIR_IMAGE}/${SPL_BINARY} ${DEPLOY_DIR_IMAGE}/bootloader-${MACHINE}.bin
    fi
}
