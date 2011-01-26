/* vi: set sw=4 ts=4: */
/*
 * crypt() for uClibc
 * Copyright (C) 2000-2006 by Erik Andersen <andersen@uclibc.org>
 * Licensed under the LGPL v2.1, see the file COPYING.LIB in this tarball.
 */

#define __FORCE_GLIBC
#include "crypt.h"
#include <unistd.h>
#include "libcrypt.h"

char *crypt(const char *key, const char *salt)
{
	/* First, check if we are supposed to be using the MD5 replacement
	 * instead of DES...  */
	if (salt[0]=='$' && salt[1]=='1' && salt[2]=='$')
		return __md5_crypt((unsigned char*)key, (unsigned char*)salt);
	else
		return __des_crypt((unsigned char*)key, (unsigned char*)salt);
}
