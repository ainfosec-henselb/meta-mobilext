#
# Per-machine additions for the Linux/Yocto kernel.
#

#Primo 73: Add our out-of-kernel device tree to the kernel, for compilation.
SRC_URI_append_primo73 = " file://primo73-add-device-tree.patch;patch=1;stripnum=1"
