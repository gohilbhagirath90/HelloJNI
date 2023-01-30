cmake_minimum_required(VERSION 3.18.1)

project("hello-jni")
find_library( # Defines the name of the path variable that stores the
        # location of the NDK library.
        log-lib

        # Specifies the name of the NDK library that
        # CMake needs to locate.
        log )
add_library(hello-jni SHARED
            hello-jni.cpp
            libusb/core.c
        libusb/events_posix.c
        libusb/descriptor.c
            libusb/hotplug.c
            libusb/io.c
            libusb/linux_usbfs.c
        libusb/strerror.c
            libusb/sync.c
            libusb/threads_posix.c
            libusb/linux_netlink.c
        )
target_include_directories(hello-jni PRIVATE
        include/libusb
        include/libusb/os)


# Include libraries needed for hello-jni lib
target_link_libraries(hello-jni
                      android
                    ${log-lib}
                    log)
