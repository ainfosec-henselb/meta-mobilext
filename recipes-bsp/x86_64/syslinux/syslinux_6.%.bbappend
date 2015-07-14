#Ensure that syslinux is considered a provider for the bootloader package.
PROVIDES += "virtual/bootloader"

#
#On BIOS systems, deploy the bootloader (MBR) image to our images directory.
#
do_deploy() {
    BOOTLOADER=${STAGING_DATADIR}/syslinux/mbr.bin

    if [ -f $BOOTLOADER ]; then
        install -d ${DEPLOY_DIR_IMAGE}
        install ${BOOTLOADER} ${DEPLOY_DIR_IMAGE}/syslinux-mbr-${MACHINE}.bin
        install ${BOOTLOADER} ${DEPLOY_DIR_IMAGE}/bootloader-${MACHINE}.bin
    fi
}
addtask deploy before build after populate_sysroot
