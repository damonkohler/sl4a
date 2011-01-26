# Copyright 2007 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This makefile builds both for host and target, and so all the
# common definitions are factored out into a separate file to
# minimize duplication between the build rules.
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := ffi
LOCAL_MODULE_FILENAME := 
LOCAL_SRC_FILES := src/arm/sysv.S \
	src/arm/ffi.c \
	src/debug.c \
	src/java_raw_api.c \
	src/prep_cif.c \
	src/raw_api.c \
	src/types.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include $(LOCAL_PATH)/linux-arm
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)

$(call __ndk_info, Building libffi)
$(call __ndk_info, PATH: $(LOCAL_PATH))
$(call __ndk_info, MODULE: $(LOCAL_MODULE))
$(call __ndk_info, FILENAME: $(LOCAL_MODULE_FILENAME))
$(call __ndk_info, SRC: $(LOCAL_SRC_FILES))
$(call __ndk_info, INCLUDES: $(LOCAL_C_INCLUDES))
$(call __ndk_info, EXPORT: $(LOCAL_EXPORT_C_INCLUDES))

include $(BUILD_SHARED_LIBRARY)
