#Ensure that syslinux is considered a provider for the bootloader package.
PROVIDES += "virtual/bootloader"

#
#Deploy the bootloader (MBR) image to our images directory.
#
do_deploy() {
    BOOTLOADER=${STAGING_DATADIR}/syslinux/mbr.bin
    install ${BOOTLOADER} ${DEPLOY_DIR_IMAGE}/syslinux-mbr-${MACHINE}.bin
    install ${BOOTLOADER} ${DEPLOY_DIR_IMAGE}/bootloader-${MACHINE}.bin 
}
addtask deploy before build after populate_sysroot
