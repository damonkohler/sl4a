LOCAL_PATH := $(call my-dir)
PYTHON_SRC_PATH := $(LOCAL_PATH)/../../python-src

include $(CLEAR_VARS)

LOCAL_C_INCLUDES := $(PYTHON_SRC_PATH) $(PYTHON_SRC_PATH)/Include
LOCAL_MODULE := python
LOCAL_SRC_FILES := Modules/python.c
LOCAL_PATH := $(PYTHON_SRC_PATH)
LOCAL_SHARED_LIBRARIES := libpython2.6

build-module = \
      $(eval LOCAL_MODULE := $(strip $1)) \
      $(eval LOCAL_MODULE_FILENAME := $(strip $1)) \
      $(eval LOCAL_SRC_FILES := $(strip $2) ) \
      $(eval LOCAL_SHARED_LIBRARIES := libpython2.6 $(strip $3) ) \
      $(eval LOCAL_CFLAGS := $(strip $4) ) \
      $(eval LOCAL_LDFLAGS := $(strip $5) ) \
      $(call __ndk_info, building $(LOCAL_MODULE_FILENAME )) \
      $(call __ndk_info, SRC: $(LOCAL_SRC_FILES)) \
      $(eval include $(BUILD_SHARED_LIBRARY) )

$(call build-module,  _struct ,  Modules/_struct.c )
$(call build-module,  _ctypes_test ,  Modules/_ctypes/_ctypes_test.c )
$(call build-module,  _weakref ,  Modules/_weakref.c )
$(call build-module,  array ,  Modules/arraymodule.c )
$(call build-module,  cmath ,  Modules/cmathmodule.c ,  m )
$(call build-module,  math ,  Modules/mathmodule.c ,  m )
$(call build-module,  strop ,  Modules/stropmodule.c )
$(call build-module,  time ,  Modules/timemodule.c ,  m )
$(call build-module,  datetime ,  Modules/datetimemodule.c Modules/timemodule.c ,  m )
$(call build-module,  itertools ,  Modules/itertoolsmodule.c )
$(call build-module,  future_builtins ,  Modules/future_builtins.c )
$(call build-module,  _random ,  Modules/_randommodule.c )
$(call build-module,  _collections ,  Modules/_collectionsmodule.c )
$(call build-module,  _bisect ,  Modules/_bisectmodule.c )
$(call build-module,  _heapq ,  Modules/_heapqmodule.c )
$(call build-module,  operator ,  Modules/operator.c )
$(call build-module,  _fileio ,  Modules/_fileio.c )
$(call build-module,  _bytesio ,  Modules/_bytesio.c )
$(call build-module,  _functools ,  Modules/_functoolsmodule.c )
$(call build-module,  _json ,  Modules/_json.c )
$(call build-module,  _testcapi ,  Modules/_testcapimodule.c )
$(call build-module,  _hotshot ,  Modules/_hotshot.c )
$(call build-module,  _lsprof ,  Modules/_lsprof.c Modules/rotatingtree.c )
$(call build-module,  unicodedata ,  Modules/unicodedata.c )
$(call build-module,  _locale ,  Modules/_localemodule.c )
$(call build-module,  fcntl ,  Modules/fcntlmodule.c )
#$(call build-module,  grp ,  Modules/grpmodule.c )
#$(call build-module,  spwd ,  Modules/spwdmodule.c )
$(call build-module,  select ,  Modules/selectmodule.c )
$(call build-module,  parser ,  Modules/parsermodule.c )
$(call build-module,  cStringIO ,  Modules/cStringIO.c )
$(call build-module,  cPickle ,  Modules/cPickle.c )
$(call build-module,  mmap ,  Modules/mmapmodule.c )
$(call build-module,  syslog ,  Modules/syslogmodule.c )
$(call build-module,  audioop ,  Modules/audioop.c )
$(call build-module,  imageop ,  Modules/imageop.c )
$(call build-module,  _csv ,  Modules/_csv.c )
$(call build-module,  _socket ,  Modules/socketmodule.c, libc, -lc, -nostdlib )
$(call build-module,  _sha ,  Modules/shamodule.c )
$(call build-module,  _md5 ,  Modules/md5module.c Modules/md5.c )
$(call build-module,  _sha256 ,  Modules/sha256module.c )
$(call build-module,  _sha512 ,  Modules/sha512module.c )
#$(call build-module,  gdbm ,  Modules/gdbmmodule.c ,  gdbm )
$(call build-module,  termios ,  Modules/termios.c )
$(call build-module,  resource ,  Modules/resource.c )
#$(call build-module,  nis ,  Modules/nismodule.c ,  nsl )
$(call build-module,  binascii ,  Modules/binascii.c )
$(call build-module,  _multibytecodec ,  Modules/cjkcodecs/multibytecodec.c )
$(call build-module,  _codecs_kr ,  Modules/cjkcodecs/_codecs_kr.c )
$(call build-module,  _codecs_jp ,  Modules/cjkcodecs/_codecs_jp.c )
$(call build-module,  _codecs_cn ,  Modules/cjkcodecs/_codecs_cn.c )
$(call build-module,  _codecs_tw ,  Modules/cjkcodecs/_codecs_tw.c )
$(call build-module,  _codecs_hk ,  Modules/cjkcodecs/_codecs_hk.c )
$(call build-module,  _codecs_iso2022 ,  Modules/cjkcodecs/_codecs_iso2022.c )
$(call build-module,  _multiprocessing ,  Modules/_multiprocessing/multiprocessing.c Modules/_multiprocessing/socket_connection.c Modules/_multiprocessing/semaphore.c,,-DHAVE_SEM_OPEN )
#$(call build-module,  ossaudiodev ,  Modules/ossaudiodev.c )

$(call import-module, bzip2)
LOCAL_C_INCLUDES += $(PYTHON_SRC_PATH) $(PYTHON_SRC_PATH)/Include
LOCAL_PATH := $(PYTHON_SRC_PATH)
LOCAL_MODULE := bz2
LOCAL_MODULE_FILENAME := bz2
LOCAL_SRC_FILES := Modules/bz2module.c
LOCAL_SHARED_LIBRARIES := libpython2.6 libbz
include $(BUILD_SHARED_LIBRARY)

$(call import-module, libcrypt)
LOCAL_C_INCLUDES += $(PYTHON_SRC_PATH) $(PYTHON_SRC_PATH)/Include
LOCAL_PATH := $(PYTHON_SRC_PATH)
LOCAL_MODULE := crypt
LOCAL_MODULE_FILENAME := crypt
LOCAL_SRC_FILES := Modules/cryptmodule.c
LOCAL_SHARED_LIBRARIES := libpython2.6 _crypt
include $(BUILD_SHARED_LIBRARY)


$(call import-module, expat)
LOCAL_C_INCLUDES += $(PYTHON_SRC_PATH) $(PYTHON_SRC_PATH)/Include
LOCAL_PATH := $(PYTHON_SRC_PATH)
LOCAL_MODULE := pyexpat
LOCAL_MODULE_FILENAME := pyexpat
LOCAL_SRC_FILES := Modules/pyexpat.c
LOCAL_SHARED_LIBRARIES := libpython2.6 libexpat
include $(BUILD_SHARED_LIBRARY)

LOCAL_MODULE := _elementtree
LOCAL_MODULE_FILENAME := _elementtree
LOCAL_SRC_FILES := Modules/_elementtree.c
include $(BUILD_SHARED_LIBRARY)

$(call import-module, libffi)
LOCAL_PATH := $(PYTHON_SRC_PATH)
LOCAL_C_INCLUDES += $(PYTHON_SRC_PATH) $(PYTHON_SRC_PATH)/Include
LOCAL_MODULE := _ctypes
LOCAL_MODULE_FILENAME := _ctypes
LOCAL_SRC_FILES := Modules/_ctypes/_ctypes.c Modules/_ctypes/callbacks.c Modules/_ctypes/callproc.c Modules/_ctypes/stgdict.c Modules/_ctypes/cfield.c Modules/_ctypes/malloc_closure.c 
LOCAL_SHARED_LIBRARIES := libpython2.6 libffi
include $(BUILD_SHARED_LIBRARY)

$(call import-module, sqlite3)
LOCAL_PATH := $(PYTHON_SRC_PATH)
LOCAL_C_INCLUDES += $(PYTHON_SRC_PATH) $(PYTHON_SRC_PATH)/Include $(PYTHON_SRC_PATH)/Modules/_sqlite/
LOCAL_MODULE := _sqlite3
LOCAL_MODULE_FILENAME := _sqlite3
LOCAL_CFLAGS += -DMODULE_NAME='"sqlite3"'
LOCAL_SRC_FILES := Modules/_sqlite/cache.c \
	Modules/_sqlite/connection.c \
	Modules/_sqlite/cursor.c \
	Modules/_sqlite/microprotocols.c \
	Modules/_sqlite/module.c \
	Modules/_sqlite/prepare_protocol.c \
	Modules/_sqlite/row.c \
	Modules/_sqlite/statement.c \
	Modules/_sqlite/util.c
LOCAL_SHARED_LIBRARIES := libpython2.6 libsqlite
include $(BUILD_SHARED_LIBRARY)

#$(call import-module, pwd_grp)
#LOCAL_PATH := $(PYTHON_SRC_PATH)
#LOCAL_C_INCLUDES += $(PYTHON_SRC_PATH) $(PYTHON_SRC_PATH)/Include
#LOCAL_MODULE := grp
#LOCAL_MODULE_FILENAME := grp
#LOCAL_SRC_FILES := Modules/grpmodule.c
#LOCAL_SHARED_LIBRARIES := libpython2.6 libpwd_grp
#include $(BUILD_SHARED_LIBRARY)

#LOCAL_MODULE := spwd
#LOCAL_MODULE_FILENAME := spwd
#LOCAL_SRC_FILES := Modules/spwdmodule.c
#include $(BUILD_SHARED_LIBRARY)
