LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	lib/xmlparse.c \
	lib/xmlrole.c \
	lib/xmltok.c

LOCAL_CFLAGS := -Wall -Wmissing-prototypes -Wstrict-prototypes -fexceptions -DHAVE_EXPAT_CONFIG_H
LOCAL_C_INCLUDES := $(LOCAL_PATH)/lib
EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)

LOCAL_MODULE := expat
LOCAL_MODULE_FILENAME :=

$(call __ndk_info, Building expat)
$(call __ndk_info, PATH: $(LOCAL_PATH))
$(call __ndk_info, MODULE: $(LOCAL_MODULE))
$(call __ndk_info, FILENAME: $(LOCAL_MODULE_FILENAME))
$(call __ndk_info, SRC: $(LOCAL_SRC_FILES))
$(call __ndk_info, INCLUDES: $(LOCAL_C_INCLUDES))
$(call __ndk_info, EXPORT: $(LOCAL_EXPORT_C_INCLUDES))

include $(BUILD_SHARED_LIBRARY)
