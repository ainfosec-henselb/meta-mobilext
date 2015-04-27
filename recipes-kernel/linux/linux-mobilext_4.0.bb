# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)


#Use the release tarball that mathces our provided kernel version.
SRC_URI = "https://www.kernel.org/pub/linux/kernel/v4.x/linux-${PV}.tar.xz"

SRC_URI[md5sum] = "a86916bd12798220da9eb4a1eec3616d"
SRC_URI[sha256sum] = "0f2f7d44979bc8f71c4fc5d3308c03499c26a824dd311fdf6eef4dee0d7d5991"

require linux-mobilext.inc

