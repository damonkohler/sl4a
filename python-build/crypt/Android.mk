LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := _crypt
LOCAL_MODULE_FILENAME := libcrypt
LOCAL_SRC_FILES := crypt-entry.c \
	md5-crypt.c \
	sha256-crypt.c \
	sha512-crypt.c \
	crypt.c \
	crypt_util.c \
	md5.c \
	sha256.c \
	sha512.c
LOCAL_CFLAGS := -D__USE_GNU -D_LIBC  -DHAVE_LIMITS_H -D__GNU_LIBRARY__
LOCAL_C_EXPORTS := $(LOCAL_PATH)

include $(BUILD_SHARED_LIBRARY)
