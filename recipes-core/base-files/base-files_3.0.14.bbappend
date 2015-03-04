
#
# Add in the MobileXT branding.
#
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"


#If no hostname has been set, the machine name.
#This should be set either in the image, or in the machine configuration.
TARGET_HOSTNAME ?= "${MACHINE}"

#
# Customize the MobileXT identification information on installation.
#
BASEFILESISSUEINSTALL = "do_install_mobilext_identity"
do_install_mobilext_identity () {
    echo ${TARGET_HOSTNAME} > ${D}${sysconfdir}/hostname

    install -m 644 ${WORKDIR}/issue*  ${D}${sysconfdir}
        if [ -n "${DISTRO_NAME}" ]; then
        echo -n "${DISTRO_NAME} " >> ${D}${sysconfdir}/issue
        echo -n "${DISTRO_NAME} " >> ${D}${sysconfdir}/issue.net
        if [ -n "${DISTRO_VERSION}" ]; then
            echo -n "${DISTRO_VERSION} " >> ${D}${sysconfdir}/issue
            echo -e "${DISTRO_VERSION} \n" >> ${D}${sysconfdir}/issue.net
        fi
        echo "- Kernel \r" >> ${D}${sysconfdir}/issue
        echo >> ${D}${sysconfdir}/issue
    fi
}
