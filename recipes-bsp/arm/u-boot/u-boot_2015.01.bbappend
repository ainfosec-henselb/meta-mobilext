#
# U-boot compatibility module; extends the OE u-boot routine to work with a
# a wider variety of supported boards.
#

#
# Extend the normal u-boot build process to include an explicit configuration
# step, which should create the relevant .config file for the specifid platform.
#
do_configure() {
    #If we're not making use of the U-boot configuration mechanism,
    #manually run the configuration step for the given machine.
    if [[ x"${UBOOT_CONFIG}" == x ]]; then
        oe_runmake ${UBOOT_MACHINE}_config
    fi;
}

#
# And, if we've used our do_configure step, then compile accordingly.
#
do_compile_prepend() {
    if [ "x${UBOOT_CONFIG}" == "x" ]; then
        oe_runmake ${UBOOT_MAKE_TARGET}
        exit 0
    fi
}

#
#Finally, create a symlink with a standard naming convention, for consumption by other images.
#
do_deploy_append() {
    if [ x"${SPL_BINARY}" != x ]; then
        install -d ${DEPLOY_DIR_IMAGE}
        ln -sf ${DEPLOY_DIR_IMAGE}/${SPL_BINARY} ${DEPLOY_DIR_IMAGE}/u-boot-with-spl.bin
        ln -sf ${DEPLOY_DIR_IMAGE}/${SPL_BINARY} ${DEPLOY_DIR_IMAGE}/bootloader-${MACHINE}.bin
    fi
}
