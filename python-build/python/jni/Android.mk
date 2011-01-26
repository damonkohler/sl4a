LOCAL_PATH := $(call my-dir)
PYTHON_PATH := $(LOCAL_PATH)
PYTHON_SRC_PATH := $(LOCAL_PATH)/../../python-src

include $(CLEAR_VARS)

include $(PYTHON_PATH)/python.mk
include $(PYTHON_PATH)/modules.mk

$(call import-module, libpython)
