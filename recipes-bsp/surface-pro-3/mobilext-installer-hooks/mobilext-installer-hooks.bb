# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "Installer hooks to support the Surface Pro 3."
DESCRIPTION = " \
    Installer hooks to support using the touchscreen \
    from inside the installer for the Surface Pro 3. \
"
AUTHOR = "Kyle J. Temkin <temkink@ainfosec.com>"
HOMEPAGE = "http://www.github.com/mobilext/installer"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=3f40d7994397109285ec7b81fdeb3b58"

#Mark this as a Board Support Package for the installer.
PROVIDES += "virtual/installer-board-support"

#Use this only on the Surface Pro 3.
COMPATIBLE_MACHINE="surface-pro-3"

#Use the local pre-install hook from this layer.
SRC_URI = "file://preinstall.hook"

#And include our preinstall hook in the package.
FILES_${PN} = "/install/preinstall.hook"

#To keep things clean, copy the preinstall hook into the source directory.
do_unpack() {
    cp "${WORKDIR}/preinstall.hook" "${S}/preinstall.hook"    
}

#Simple install: copy the preinstall hook to the target directory.
do_install() {
    install -D "${S}/preinstall.hook" "${D}/install/preinstall.hook"
}




