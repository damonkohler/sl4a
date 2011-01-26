##
##
## Build the library
##
##

LOCAL_PATH:= $(call my-dir)

common_src_files := sqlite3.c

# the device library
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(common_src_files)

# NOTE the following flags,
#   SQLITE_TEMP_STORE=3 causes all TEMP files to go into RAM. and thats the behavior we want
#   SQLITE_ENABLE_FTS3   enables usage of FTS3 - NOT FTS1 or 2.
#   SQLITE_DEFAULT_AUTOVACUUM=1  causes the databases to be subject to auto-vacuum
LOCAL_CFLAGS += -DHAVE_USLEEP=1 -DSQLITE_DEFAULT_JOURNAL_SIZE_LIMIT=1048576 -DSQLITE_THREADSAFE=1 -DNDEBUG=1 -DSQLITE_ENABLE_MEMORY_MANAGEMENT=1 -DSQLITE_DEFAULT_AUTOVACUUM=1 -DSQLITE_TEMP_STORE=3 -DSQLITE_ENABLE_FTS3 -DSQLITE_ENABLE_FTS3_BACKWARDS
LOCAL_SHARED_LIBRARIES := libdl

LOCAL_MODULE:= libsqlite
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)
#LOCAL_C_INCLUDES += $(call include-path-for, system-core)/cutils
#LOCAL_SHARED_LIBRARIES += liblog \
#            libicuuc \
#            libicui18n \
#            libutils

include $(BUILD_SHARED_LIBRARY)
