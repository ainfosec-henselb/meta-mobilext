#
# U-boot compatibility module; extends the OE u-boot routine to work with 
# our bootloader naming conventions.

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
