LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := pwd_grp
LOCAL_MODULE_FILENAME := 
LOCAL_SRC_FILES := fgetgrent.c \
	fgetgrent_r.c \
	fgetpwent.c \
	fgetpwent_r.c \
	fgetspent.c \
	fgetspent_r.c \
	getgrent.c \
	getgrent_r.c \
	getgrgid.c \
	getgrgid_r.c \
	getgrnam.c \
	getgrnam_r.c \
	getgrouplist.c \
	__getgrouplist_internal.c \
	getpw.c \
	getpwent.c \
	getpwent_r.c \
	getpwnam.c \
	getpwnam_r.c \
	getpwuid.c \
	getpwuid_r.c \
	getspent.c \
	getspent_r.c \
	getspnam.c \
	getspnam_r.c \
	.indent.pro \
	initgroups.c \
	lckpwdf.c \
	__parsegrent.c \
	__parsepwent.c \
	__parsespent.c \
	__pgsreader.c \
	putgrent.c \
	putpwent.c \
	putspent.c \
	pwd_grp.c \
	pwd_grp_internal.c \
	sgetspent.c \
	sgetspent_r.c

LOCAL_CFLAGS := -DUCLIBC_HAS_SHADOW=n

include $(BUILD_SHARED_LIBRARY)
