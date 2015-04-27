# Copyright (C) 2015 Assured Information Security, Inc.
# Author: Kyle J. Temkin <temkink@ainfosec.com>
#
# Released under the MIT license (see COPYING.MIT for the terms)

#Use the release tarball that mathces our provided kernel version.
SRC_URI = "https://www.kernel.org/pub/linux/kernel/v4.x/testing/linux-${PV}.tar.xz"

SRC_URI[md5sum] = "dbd95eac210a06ab2d1b58c9ac01b874"
SRC_URI[sha256sum] = "ad30def836c0d538c81814cfa49127f745835a921be38d9591443e0e146c1c34"

#By default, avoid this recipe, as it's a release candidate.
DEFAULT_PREFERENCE = "-1"

require linux-mobilext.inc
