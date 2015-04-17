# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)

SUMMARY = "Linux Kernel for MobileXT supported platforms"
DESCRIPTION = " \
    This package provides a customized linux kernel which provides \
    support for each of the MobileXT supported host platforms. \
"
AUTHOR = "Kyle J. Temkin <temkink@ainfosec.com>"
SECTION = "kernel"

#License for the linux kernel itself.
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"

#For now, we'll explicitly list each of the compatible machines.
#Once the xen-specific configuration fragments are broken out, we should be able
#to drop the hard requirement of having hard-listing compatible machines by adding
#per-machine configuration conditionally using an override in per-machine-additions.inc.
COMPATIBLE_MACHINE = "cubietruck|primo73|generic-x86_64-xen|generic-x86_64-efi-xen|surface-pro-3"

#Use the basic OpenEmbedded method for building kernels.
inherit kernel

#Include support for device trees, and for explicit per-device customizations.
require recipes-kernel/linux/linux-dtb.inc
require recipes-kernel/linux/per-machine-additions.inc

#Set up the linux kernel versionining information.
LINUX_VERSION ?= "${PV}"
LINUX_VERSION_EXTENSION ?= "-mobilext-${LINUX_KERNEL_TYPE}"

#Use the release tarball that mathces our provided kernel version.
SRC_URI = "https://www.kernel.org/pub/linux/kernel/v4.x/linux-${PV}.tar.xz"

SRC_URI[md5sum] = "a86916bd12798220da9eb4a1eec3616d"
SRC_URI[sha256sum] = "0f2f7d44979bc8f71c4fc5d3308c03499c26a824dd311fdf6eef4dee0d7d5991"

#TODO: Break this into configuration fragments-- one that has the pieces contained
#for a generic Xen dom0, and one that has the per-machine specific options.
SRC_URI += "file://defconfig"

#And work inside the directory where the kernel will be 
S = "${WORKDIR}/linux-${PV}"

#
# The following code is taken directly from a newer version of the kernel base class. 
# It should be removed once the usptream code is pulled down.
#
kernel_do_install() {
	#
	# First install the modules
	#
	unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS MACHINE
	if (grep -q -i -e '^CONFIG_MODULES=y$' .config); then
		oe_runmake DEPMOD=echo INSTALL_MOD_PATH="${D}" modules_install
		rm "${D}/lib/modules/${KERNEL_VERSION}/build"
		rm "${D}/lib/modules/${KERNEL_VERSION}/source"
	else
		bbnote "no modules to install"
	fi

	#
	# Install various kernel output (zImage, map file, config, module support files)
	#
	install -d ${D}/${KERNEL_IMAGEDEST}
	install -d ${D}/boot
	install -m 0644 ${KERNEL_OUTPUT} ${D}/${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE}-${KERNEL_VERSION}
	install -m 0644 System.map ${D}/boot/System.map-${KERNEL_VERSION}
	install -m 0644 .config ${D}/boot/config-${KERNEL_VERSION}
	install -m 0644 vmlinux ${D}/boot/vmlinux-${KERNEL_VERSION}
	[ -e Module.symvers ] && install -m 0644 Module.symvers ${D}/boot/Module.symvers-${KERNEL_VERSION}
	install -d ${D}${sysconfdir}/modules-load.d
	install -d ${D}${sysconfdir}/modprobe.d

	#
	# Support for external module building - create a minimal copy of the
	# kernel source tree.
	#
	kerneldir=${D}${KERNEL_SRC_PATH}
	install -d $kerneldir

	#
	# Store the kernel version in sysroots for module-base.bbclass
	#

	echo "${KERNEL_VERSION}" > $kerneldir/kernel-abiversion

	#
	# Store kernel image name to allow use during image generation
	#

	echo "${KERNEL_IMAGE_BASE_NAME}" >$kerneldir/kernel-image-name

	#
	# Copy the entire source tree. In case an external build directory is
	# used, copy the build directory over first, then copy over the source
	# dir. This ensures the original Makefiles are used and not the
	# redirecting Makefiles in the build directory.
	#
	# work and sysroots can be on different partitions, so we can't rely on
	# hardlinking, unfortunately.
	#
	find . -depth -not -name "*.cmd" -not -name "*.o" -not -name "*.so.dbg" -not -name "*.so" -not -path "./.*" -print0 | cpio --null -pdu $kerneldir
	cp .config $kerneldir
	if [ "${S}" != "${B}" ]; then
		pwd="$PWD"
		cd "${S}"
		find . -depth -not -path "./.*" -print0 | cpio --null -pdu $kerneldir
		cd "$pwd"
	fi
	install -m 0644 ${KERNEL_OUTPUT} $kerneldir/${KERNEL_IMAGETYPE}
	install -m 0644 System.map $kerneldir/System.map-${KERNEL_VERSION}

	#
	# Clean and remove files not needed for building modules.
	# Some distributions go through a lot more trouble to strip out
	# unecessary headers, for now, we just prune the obvious bits.
	#
	# We don't want to leave host-arch binaries in /sysroots, so
	# we clean the scripts dir while leaving the generated config
	# and include files.
	#
	oe_runmake -C $kerneldir CC="${KERNEL_CC}" LD="${KERNEL_LD}" clean
	make -C $kerneldir _mrproper_scripts
	find $kerneldir -path $kerneldir/lib -prune -o -path $kerneldir/tools -prune -o -path $kerneldir/scripts -prune -o -name "*.[csS]" -exec rm '{}' \;
	find $kerneldir/Documentation -name "*.txt" -exec rm '{}' \;

	# As of Linux kernel version 3.0.1, the clean target removes
	# arch/powerpc/lib/crtsavres.o which is present in
	# KBUILD_LDFLAGS_MODULE, making it required to build external modules.
	if [ ${ARCH} = "powerpc" ]; then
		cp arch/powerpc/lib/crtsavres.o $kerneldir/arch/powerpc/lib/crtsavres.o
	fi

	# Necessary for building modules like compat-wireless.
	if [ -f include/generated/bounds.h ]; then
		cp include/generated/bounds.h $kerneldir/include/generated/bounds.h
	fi
	if [ -d arch/${ARCH}/include/generated ]; then
		mkdir -p $kerneldir/arch/${ARCH}/include/generated/
		cp -fR arch/${ARCH}/include/generated/* $kerneldir/arch/${ARCH}/include/generated/
	fi

	# Remove the following binaries which cause strip or arch QA errors
	# during do_package for cross-compiled platforms
	bin_files="arch/powerpc/boot/addnote arch/powerpc/boot/hack-coff \
	           arch/powerpc/boot/mktree scripts/kconfig/zconf.tab.o \
		   scripts/kconfig/conf.o scripts/kconfig/kxgettext.o"
	for entry in $bin_files; do
		rm -f $kerneldir/$entry
	done

	# kernels <2.6.30 don't have $kerneldir/tools directory so we check if it exists before calling sed
	if [ -f $kerneldir/tools/perf/Makefile ]; then
		# Fix SLANG_INC for slang.h
		sed -i 's#-I/usr/include/slang#-I=/usr/include/slang#g' $kerneldir/tools/perf/Makefile
	fi
}
