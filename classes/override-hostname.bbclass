
#Adjust the image so it 
IMAGE_PREPROCESS_COMMAND = "rootfs_override_hostname ;"

#Override
rootfs_override_hostname() {
    echo ${TARGET_HOSTNAME} > ${IMAGE_ROOTFS}${sysconfdir}/hostname
}

