SUMMARY = "A simple root filesystem intended for bootstrapping installations."
DESCRIPTION = " \
   An initial ram filesystem which is intended to provide a base for full-featured \
   installations that run off of a live image-- typically from a CD, SD card, or \
   USB flash drive. \
"
AUTHOR = "Kyle J. Temkin <temkink@ainfosec.com>"

LICENSE = "MIT"

#Mark this as a potential provider for the installer initramfs.
PROVIDES += "virtual/installer-initramfs-image"

#Install the basic packages necessary to bootstarp our live installer.
IMAGE_INSTALL = "\
    initramfs-live-boot \
    base-passwd \
    busybox \
    udev \
"

# Do not pollute the initrd image with rootfs features.
IMAGE_FEATURES = ""
IMAGE_LINGUAS = ""

export IMAGE_BASENAME = "mobilext-installer-initramfs"

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES}"
inherit core-image
inherit deploy

IMAGE_ROOTFS_SIZE = "8192"


#Don't pull the syslog daemon into our initramfs;
#this will be handled by systemd.
BAD_RECOMMENDATIONS += "busybox-syslog"

#
# Prepare the system for start up using systemd. Experimental. 
#
prepare_for_systemd() {
    ln -s /run ${IMAGE_ROOTFS}/var/run 
}
ROOTFS_POSTPROCESS_COMMAND += "prepare_for_systemd ; "


#
#Create simple, toolchain-independent symlinks to the image that can be consumed by other images.
#
do_deploy() {
    for IMAGE_TYPE in ${IMAGE_FSTYPES}; do

        #Compute the path the image will have, if it's been created...
        SOURCE_IMAGE=${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}.rootfs.${IMAGE_TYPE}

        #... and if it has been created, create our symlink.
        if [[ -e $SOURCE_IMAGE ]]; then
            ln -sf ${SOURCE_IMAGE} ${DEPLOY_DIR_IMAGE}/installer-initramfs-${MACHINE}.${IMAGE_TYPE}
        fi

    done
}
addtask do_deploy after do_rootfs
