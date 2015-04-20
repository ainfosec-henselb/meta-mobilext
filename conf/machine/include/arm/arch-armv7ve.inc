#
# ARMv7 processors with Virtualization Extensions
#

DEFAULTTUNE ?= "armv7ve"

TUNEVALID[armv7ve] = "Enable instructions for ARMv7 processors with virtualization extensions"
TUNECONFLICTS[armv7ve] = "armv4 armv5 armv6 armv7 armv7a"
TUNE_CCARGS .= "${@bb.utils.contains("TUNE_FEATURES", "armv7ve", " -march=armv7ve", "", d)}"
MACHINEOVERRIDES =. "${@bb.utils.contains("TUNE_FEATURES", "armv7ve", "armv7ve:", "" ,d)}"

require conf/machine/include/arm/arch-armv7a.inc

# Little Endian base configs
AVAILTUNES += "armv7ve armv7vet armv7ve-neon armv7vet-neon"
ARMPKGARCH_tune-armv7ve ?= "armv7ve"
ARMPKGARCH_tune-armv7vet ?= "armv7ve"
ARMPKGARCH_tune-armv7ve-neon?= "armv7ve"
ARMPKGARCH_tune-armv7vet-neon ?= "armv7ve"
TUNE_FEATURES_tune-armv7ve ?= "arm armv7ve vfp"
TUNE_FEATURES_tune-armv7vet ?= "${TUNE_FEATURES_tune-armv7ve} thumb"
TUNE_FEATURES_tune-armv7ve-neon ?= "${TUNE_FEATURES_tune-armv7ve} neon"
TUNE_FEATURES_tune-armv7vet-neon ?= "${TUNE_FEATURES_tune-armv7vet} neon"
PACKAGE_EXTRA_ARCHS_tune-armv7ve = "${PACKAGE_EXTRA_ARCHS_tune-armv6} armv7ve armv7ve-vfp"
PACKAGE_EXTRA_ARCHS_tune-armv7ve-neon = "${PACKAGE_EXTRA_ARCHS_tune-armv7ve} armv7ve-vfp-neon"
PACKAGE_EXTRA_ARCHS_tune-armv7vet = "${PACKAGE_EXTRA_ARCHS_tune-armv6t} armv7ve armv7ve-vfp armv7vet2-vfp"
PACKAGE_EXTRA_ARCHS_tune-armv7vet-neon = "${PACKAGE_EXTRA_ARCHS_tune-armv7vet} armv7ve-vfp-neon armv7vet2-vfp-neon"

# VFP Tunes
AVAILTUNES += "armv7vehf armv7vethf armv7vehf-neon armv7vethf-neon armv7vehf-neon-vfpv4 armv7vethf-neon-vfpv4"
ARMPKGARCH_tune-armv7vehf ?= "armv7ve"
ARMPKGARCH_tune-armv7vethf ?= "armv7ve"
ARMPKGARCH_tune-armv7vehf-neon?= "armv7ve"
ARMPKGARCH_tune-armv7vethf-neon ?= "armv7ve"
ARMPKGARCH_tune-armv7vehf-neon-vfpv4 ?= "armv7ve"
ARMPKGARCH_tune-armv7vethf-neon-vfpv4 ?= "armv7ve"
TUNE_FEATURES_tune-armv7vehf ?= "${TUNE_FEATURES_tune-armv7ve} callconvention-hard"
TUNE_FEATURES_tune-armv7vethf ?= "${TUNE_FEATURES_tune-armv7vet} callconvention-hard"
TUNE_FEATURES_tune-armv7vehf-neon ?= "${TUNE_FEATURES_tune-armv7ve-neon} callconvention-hard"
TUNE_FEATURES_tune-armv7vethf-neon ?= "${TUNE_FEATURES_tune-armv7vet-neon} callconvention-hard"
TUNE_FEATURES_tune-armv7vehf-neon-vfpv4 ?= "${TUNE_FEATURES_tune-armv7vehf-neon} vfpv4"
TUNE_FEATURES_tune-armv7vethf-neon-vfpv4 ?= "${TUNE_FEATURES_tune-armv7vethf-neon} vfpv4"
PACKAGE_EXTRA_ARCHS_tune-armv7vehf = "${PACKAGE_EXTRA_ARCHS_tune-armv6hf} armv7vehf-vfp"
PACKAGE_EXTRA_ARCHS_tune-armv7vethf = "${PACKAGE_EXTRA_ARCHS_tune-armv6thf} armv7vehf-vfp armv7vet2hf-vfp"
PACKAGE_EXTRA_ARCHS_tune-armv7vehf-neon = "${PACKAGE_EXTRA_ARCHS_tune-armv7vehf} armv7vehf-vfp-neon"
PACKAGE_EXTRA_ARCHS_tune-armv7vethf-neon = "${PACKAGE_EXTRA_ARCHS_tune-armv7vethf} armv7vehf-vfp-neon armv7vet2hf-vfp-neon"
PACKAGE_EXTRA_ARCHS_tune-armv7vehf-neon-vfpv4 = "${PACKAGE_EXTRA_ARCHS_tune-armv7vehf-neon} armv7vehf-vfp-neon-vfpv4"
PACKAGE_EXTRA_ARCHS_tune-armv7vethf-neon-vfpv4 = "${PACKAGE_EXTRA_ARCHS_tune-armv7vethf-neon} armv7vehf-vfp-neon-vfpv4 armv7vet2hf-vfp-neon-vfpv4"

# Big Endian
AVAILTUNES += "armv7veb armv7vetb armv7veb-neon armv7vetb-neon"
ARMPKGARCH_tune-armv7veb ?= "armv7ve"
ARMPKGARCH_tune-armv7vetb ?= "armv7ve"
ARMPKGARCH_tune-armv7veb-neon?= "armv7ve"
ARMPKGARCH_tune-armv7vetb-neon ?= "armv7ve"
TUNE_FEATURES_tune-armv7veb ?= "${TUNE_FEATURES_tune-armv7ve} bigendian"
TUNE_FEATURES_tune-armv7vetb ?= "${TUNE_FEATURES_tune-armv7vet} bigendian"
TUNE_FEATURES_tune-armv7veb-neon ?= "${TUNE_FEATURES_tune-armv7ve-neon} bigendian"
TUNE_FEATURES_tune-armv7vetb-neon ?= "${TUNE_FEATURES_tune-armv7vet-neon} bigendian"
PACKAGE_EXTRA_ARCHS_tune-armv7veb = "${PACKAGE_EXTRA_ARCHS_tune-armv6b} armv7veb-vfp"
PACKAGE_EXTRA_ARCHS_tune-armv7vetb = "${PACKAGE_EXTRA_ARCHS_tune-armv6tb} armv7veb-vfp armv7vet2b-vfp"
PACKAGE_EXTRA_ARCHS_tune-armv7veb-neon = "${PACKAGE_EXTRA_ARCHS_tune-armv7veb} armv7veb-vfp-neon"
PACKAGE_EXTRA_ARCHS_tune-armv7vetb-neon = "${PACKAGE_EXTRA_ARCHS_tune-armv7vetb} armv7veb-vfp-neon armv7vet2b-vfp-neon"

# Big Endian + VFP
AVAILTUNES += "armv7vehfb armv7vethfb armv7vehfb-neon armv7vethfb-neon"
ARMPKGARCH_tune-armv7vehfb ?= "armv7ve"
ARMPKGARCH_tune-armv7vethfb ?= "armv7ve"
ARMPKGARCH_tune-armv7vehfb-neon?= "armv7ve"
ARMPKGARCH_tune-armv7vethfb-neon ?= "armv7ve"
TUNE_FEATURES_tune-armv7vehfb ?= "${TUNE_FEATURES_tune-armv7vehf} bigendian"
TUNE_FEATURES_tune-armv7vethfb ?= "${TUNE_FEATURES_tune-armv7vethf} bigendian"
TUNE_FEATURES_tune-armv7vehfb-neon ?= "${TUNE_FEATURES_tune-armv7vehf-neon} bigendian"
TUNE_FEATURES_tune-armv7vethfb-neon ?= "${TUNE_FEATURES_tune-armv7vethf-neon} bigendian"
PACKAGE_EXTRA_ARCHS_tune-armv7vehfb = "${PACKAGE_EXTRA_ARCHS_tune-armv6hfb} armv7vehfb-vfp"
PACKAGE_EXTRA_ARCHS_tune-armv7vethfb = "${PACKAGE_EXTRA_ARCHS_tune-armv6thfb} armv7vehfb-vfp armv7vet2hfb-vfp"
PACKAGE_EXTRA_ARCHS_tune-armv7vehfb-neon = "${PACKAGE_EXTRA_ARCHS_tune-armv7vehfb} armv7vehfb-vfp-neon"
PACKAGE_EXTRA_ARCHS_tune-armv7vethfb-neon = "${PACKAGE_EXTRA_ARCHS_tune-armv7vethfb} armv7vehfb-vfp-neon armv7vet2hfb-vfp-neon"

