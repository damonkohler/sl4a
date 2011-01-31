# Copyright (C) 2009 The Android Open Source Project
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
#
LOCAL_PATH := $(call my-dir)
PYTHONLIB := $(LOCAL_PATH)/../../python-lib

include $(CLEAR_VARS)

LOCAL_C_INCLUDES := $(PYTHONLIB)/include
LOCAL_MODULE    := bluetooth
LOCAL_MODULE_FILENAME := _bluetooth
#LOCAL_LDFLAGS := -L$(PYTHONLIB)/lib -llibpython2.6
LOCAL_LDLIBS := -L$(PYTHONLIB)/lib/ -lpython2.6
LOCAL_SRC_FILES := bluetooth/hci.c \
	bluetooth/sdp.c \
	bluetooth/bluetooth.c \
	btmodule.c \
	btsdp.c

include $(BUILD_SHARED_LIBRARY)
