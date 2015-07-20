
#For MobileXT, replace the machine compatibility filter with an architecture
#compatibility filter.
COMPATIBLE_MACHINE = "${MACHINE}"
COMPATIBLE_HOST = '(i.86|x86_64).*-linux'

#
# Install each of the kernel modules, but do not install the Shared Folders,
# which MobileXT is opting not to choose or support for portability.
#

module_do_install() {
    MODULE_DIR=${D}${base_libdir}/modules/${KERNEL_VERSION}/kernel/misc
    install -d $MODULE_DIR
    install -m 644 vboxguest.ko $MODULE_DIR
    install -m 644 vboxvideo.ko $MODULE_DIR
}

do_install_append() {
    install -d ${D}${base_sbindir}
}
