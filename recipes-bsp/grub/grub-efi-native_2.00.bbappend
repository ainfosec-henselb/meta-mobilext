#
# It looks like there's some unfortunate deprecated code in Grub.
# We'll need to turn off -Werror.
#
EXTRA_OECONF += "--disable-werror"

#
# Adjust our makeimage procedure so it includes multiboot and gzio support,
# for Xen.
#
do_mkimage() {
	# Search for the grub.cfg on the local boot media by using the
	# built in cfg file provided via this recipe
	./grub-mkimage -c ../cfg -p /EFI/BOOT -d ./grub-core/ \
	               -O ${GRUB_TARGET}-efi -o ./${GRUB_IMAGE} \
	               boot linux ext2 fat serial part_msdos part_gpt normal efi_gop iso9660 search \
                 multiboot gzio
}
