

# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Bradley Hensel <henselb@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)a

AUTHOR = "Bradley Hensel <henselb@ainfosec.com>"

SUMMARY = "Stuff for demonstrating wifi on cubietruck.. this package should not be included in commercial releases"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/LICENSE;md5=4d92cd373abda3937c2bc47fbc49d690"

SRC_URI = " \
file://domU.img \
file://guest.conf \
file://launcher.sh \
"

SRC_URI[md5sum] = "52c53999feb6315894c6ded657744b6d"
SRC_URI[sha256sum] = "ea571dafce67b81ce6cf697c220c1ae36629cef39a04ac435e0514291af2b74e"

do_rm_work() {
	true
}

HR = "/home/root"

do_install() {
	install -d ${D}/${HR}
	install -m 0755 ${WORKDIR}/domU.img ${D}/${HR}
	install -m 0755 ${WORKDIR}/guest.conf ${D}/${HR}
	install -m 0755 ${WORKDIR}/launcher.sh ${D}/${HR}
}

FILES_${PN} = " \
${HR} \
${HR}/domU.img \
${HR}/guest.conf \
${HR}/launcher.sh \
"