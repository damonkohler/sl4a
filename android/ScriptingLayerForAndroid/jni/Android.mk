LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := com_googlecode_android_scripting_Exec
LOCAL_SRC_FILES := com_googlecode_android_scripting_Exec.cpp
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := run_pie
LOCAL_SRC_FILES := run_pie.c
include $(BUILD_EXECUTABLE)

