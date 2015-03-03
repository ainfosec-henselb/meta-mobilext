
#Unfortunately, some tarballs of VGAbios ship with files that break our builds.
#We'll clean up, first.
do_compile_prepend() {
    oe_runmake clean
}
