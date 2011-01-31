/*
 *
 *  BlueZ - Bluetooth protocol stack for Linux
 *
 *  Copyright (C) 2000-2001  Qualcomm Incorporated
 *  Copyright (C) 2002-2003  Maxim Krasnyansky <maxk@qualcomm.com>
 *  Copyright (C) 2002-2010  Marcel Holtmann <marcel@holtmann.org>
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

#ifdef HAVE_CONFIG_H
#include <config.h>
#endif

#include <stdio.h>
#include <errno.h>
#include <ctype.h>
#include <stdarg.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>

#include "bluetooth.h"
#include "hci.h"

void baswap(bdaddr_t *dst, const bdaddr_t *src)
{
	register unsigned char *d = (unsigned char *) dst;
	register const unsigned char *s = (const unsigned char *) src;
	register int i;

	for (i = 0; i < 6; i++)
		d[i] = s[5-i];
}

char *batostr(const bdaddr_t *ba)
{
	char *str = bt_malloc(18);
	if (!str)
		return NULL;

	sprintf(str, "%2.2X:%2.2X:%2.2X:%2.2X:%2.2X:%2.2X",
		ba->b[0], ba->b[1], ba->b[2],
		ba->b[3], ba->b[4], ba->b[5]);

	return str;
}

bdaddr_t *strtoba(const char *str)
{
	const char *ptr = str;
	int i;

	uint8_t *ba = bt_malloc(sizeof(bdaddr_t));
	if (!ba)
		return NULL;

	for (i = 0; i < 6; i++) {
		ba[i] = (uint8_t) strtol(ptr, NULL, 16);
		if (i != 5 && !(ptr = strchr(ptr,':')))
			ptr = ":00:00:00:00:00";
		ptr++;
	}

	return (bdaddr_t *) ba;
}

int ba2str(const bdaddr_t *ba, char *str)
{
	uint8_t b[6];

	baswap((bdaddr_t *) b, ba);
	return sprintf(str, "%2.2X:%2.2X:%2.2X:%2.2X:%2.2X:%2.2X",
		b[0], b[1], b[2], b[3], b[4], b[5]);
}

int str2ba(const char *str, bdaddr_t *ba)
{
	uint8_t b[6];
	const char *ptr = str;
	int i;

	for (i = 0; i < 6; i++) {
		b[i] = (uint8_t) strtol(ptr, NULL, 16);
		if (i != 5 && !(ptr = strchr(ptr, ':')))
			ptr = ":00:00:00:00:00";
		ptr++;
	}

	baswap(ba, (bdaddr_t *) b);

	return 0;
}

int ba2oui(const bdaddr_t *ba, char *str)
{
	uint8_t b[6];

	baswap((bdaddr_t *) b, ba);

	return sprintf(str, "%2.2X-%2.2X-%2.2X", b[0], b[1], b[2]);
}

int bachk(const char *str)
{
	char tmp[18], *ptr = tmp;

	if (!str)
		return -1;

	if (strlen(str) != 17)
		return -1;

	memcpy(tmp, str, 18);

	while (*ptr) {
		*ptr = toupper(*ptr);
		if (*ptr < '0'|| (*ptr > '9' && *ptr < 'A') || *ptr > 'F')
			return -1;
		ptr++;

		*ptr = toupper(*ptr);
		if (*ptr < '0'|| (*ptr > '9' && *ptr < 'A') || *ptr > 'F')
			return -1;
		ptr++;

		*ptr = toupper(*ptr);
		if (*ptr == 0)
			break;
		if (*ptr != ':')
			return -1;
		ptr++;
	}

	return 0;
}

int baprintf(const char *format, ...)
{
	va_list ap;
	int len;

	va_start(ap, format);
	len = vprintf(format, ap);
	va_end(ap);

	return len;
}

int bafprintf(FILE *stream, const char *format, ...)
{
	va_list ap;
	int len;

	va_start(ap, format);
	len = vfprintf(stream, format, ap);
	va_end(ap);

	return len;
}

int basprintf(char *str, const char *format, ...)
{
	va_list ap;
	int len;

	va_start(ap, format);
	len = vsnprintf(str, (~0U) >> 1, format, ap);
	va_end(ap);

	return len;
}

int basnprintf(char *str, size_t size, const char *format, ...)
{
	va_list ap;
	int len;

	va_start(ap, format);
	len = vsnprintf(str, size, format, ap);
	va_end(ap);

	return len;
}

void *bt_malloc(size_t size)
{
	return malloc(size);
}

void bt_free(void *ptr)
{
	free(ptr);
}

/* Bluetooth error codes to Unix errno mapping */
int bt_error(uint16_t code)
{
	switch (code) {
	case 0:
		return 0;
	case HCI_UNKNOWN_COMMAND:
		return EBADRQC;
	case HCI_NO_CONNECTION:
		return ENOTCONN;
	case HCI_HARDWARE_FAILURE:
		return EIO;
	case HCI_PAGE_TIMEOUT:
		return EHOSTDOWN;
	case HCI_AUTHENTICATION_FAILURE:
		return EACCES;
	case HCI_PIN_OR_KEY_MISSING:
		return EINVAL;
	case HCI_MEMORY_FULL:
		return ENOMEM;
	case HCI_CONNECTION_TIMEOUT:
		return ETIMEDOUT;
	case HCI_MAX_NUMBER_OF_CONNECTIONS:
	case HCI_MAX_NUMBER_OF_SCO_CONNECTIONS:
		return EMLINK;
	case HCI_ACL_CONNECTION_EXISTS:
		return EALREADY;
	case HCI_COMMAND_DISALLOWED:
	case HCI_TRANSACTION_COLLISION:
	case HCI_ROLE_SWITCH_PENDING:
		return EBUSY;
	case HCI_REJECTED_LIMITED_RESOURCES:
	case HCI_REJECTED_PERSONAL:
	case HCI_QOS_REJECTED:
		return ECONNREFUSED;
	case HCI_HOST_TIMEOUT:
		return ETIMEDOUT;
	case HCI_UNSUPPORTED_FEATURE:
	case HCI_QOS_NOT_SUPPORTED:
	case HCI_PAIRING_NOT_SUPPORTED:
	case HCI_CLASSIFICATION_NOT_SUPPORTED:
	case HCI_UNSUPPORTED_LMP_PARAMETER_VALUE:
	case HCI_PARAMETER_OUT_OF_RANGE:
	case HCI_QOS_UNACCEPTABLE_PARAMETER:
		return EOPNOTSUPP;
	case HCI_INVALID_PARAMETERS:
	case HCI_SLOT_VIOLATION:
		return EINVAL;
	case HCI_OE_USER_ENDED_CONNECTION:
	case HCI_OE_LOW_RESOURCES:
	case HCI_OE_POWER_OFF:
		return ECONNRESET;
	case HCI_CONNECTION_TERMINATED:
		return ECONNABORTED;
	case HCI_REPEATED_ATTEMPTS:
		return ELOOP;
	case HCI_REJECTED_SECURITY:
	case HCI_PAIRING_NOT_ALLOWED:
	case HCI_INSUFFICIENT_SECURITY:
		return EACCES;
	case HCI_UNSUPPORTED_REMOTE_FEATURE:
		return EPROTONOSUPPORT;
	case HCI_SCO_OFFSET_REJECTED:
		return ECONNREFUSED;
	case HCI_UNKNOWN_LMP_PDU:
	case HCI_INVALID_LMP_PARAMETERS:
	case HCI_LMP_ERROR_TRANSACTION_COLLISION:
	case HCI_LMP_PDU_NOT_ALLOWED:
	case HCI_ENCRYPTION_MODE_NOT_ACCEPTED:
		return EPROTO;
	default:
		return ENOSYS;
	}
}

char *bt_compidtostr(int compid)
{
	switch (compid) {
	case 0:
		return "Ericsson Technology Licensing";
	case 1:
		return "Nokia Mobile Phones";
	case 2:
		return "Intel Corp.";
	case 3:
		return "IBM Corp.";
	case 4:
		return "Toshiba Corp.";
	case 5:
		return "3Com";
	case 6:
		return "Microsoft";
	case 7:
		return "Lucent";
	case 8:
		return "Motorola";
	case 9:
		return "Infineon Technologies AG";
	case 10:
		return "Cambridge Silicon Radio";
	case 11:
		return "Silicon Wave";
	case 12:
		return "Digianswer A/S";
	case 13:
		return "Texas Instruments Inc.";
	case 14:
		return "Parthus Technologies Inc.";
	case 15:
		return "Broadcom Corporation";
	case 16:
		return "Mitel Semiconductor";
	case 17:
		return "Widcomm, Inc.";
	case 18:
		return "Zeevo, Inc.";
	case 19:
		return "Atmel Corporation";
	case 20:
		return "Mitsubishi Electric Corporation";
	case 21:
		return "RTX Telecom A/S";
	case 22:
		return "KC Technology Inc.";
	case 23:
		return "Newlogic";
	case 24:
		return "Transilica, Inc.";
	case 25:
		return "Rohde & Schwartz GmbH & Co. KG";
	case 26:
		return "TTPCom Limited";
	case 27:
		return "Signia Technologies, Inc.";
	case 28:
		return "Conexant Systems Inc.";
	case 29:
		return "Qualcomm";
	case 30:
		return "Inventel";
	case 31:
		return "AVM Berlin";
	case 32:
		return "BandSpeed, Inc.";
	case 33:
		return "Mansella Ltd";
	case 34:
		return "NEC Corporation";
	case 35:
		return "WavePlus Technology Co., Ltd.";
	case 36:
		return "Alcatel";
	case 37:
		return "Philips Semiconductors";
	case 38:
		return "C Technologies";
	case 39:
		return "Open Interface";
	case 40:
		return "R F Micro Devices";
	case 41:
		return "Hitachi Ltd";
	case 42:
		return "Symbol Technologies, Inc.";
	case 43:
		return "Tenovis";
	case 44:
		return "Macronix International Co. Ltd.";
	case 45:
		return "GCT Semiconductor";
	case 46:
		return "Norwood Systems";
	case 47:
		return "MewTel Technology Inc.";
	case 48:
		return "ST Microelectronics";
	case 49:
		return "Synopsys";
	case 50:
		return "Red-M (Communications) Ltd";
	case 51:
		return "Commil Ltd";
	case 52:
		return "Computer Access Technology Corporation (CATC)";
	case 53:
		return "Eclipse (HQ Espana) S.L.";
	case 54:
		return "Renesas Technology Corp.";
	case 55:
		return "Mobilian Corporation";
	case 56:
		return "Terax";
	case 57:
		return "Integrated System Solution Corp.";
	case 58:
		return "Matsushita Electric Industrial Co., Ltd.";
	case 59:
		return "Gennum Corporation";
	case 60:
		return "Research In Motion";
	case 61:
		return "IPextreme, Inc.";
	case 62:
		return "Systems and Chips, Inc";
	case 63:
		return "Bluetooth SIG, Inc";
	case 64:
		return "Seiko Epson Corporation";
	case 65:
		return "Integrated Silicon Solution Taiwain, Inc.";
	case 66:
		return "CONWISE Technology Corporation Ltd";
	case 67:
		return "PARROT SA";
	case 68:
		return "Socket Communications";
	case 69:
		return "Atheros Communications, Inc.";
	case 70:
		return "MediaTek, Inc.";
	case 71:
		return "Bluegiga";
	case 72:
		return "Marvell Technology Group Ltd.";
	case 73:
		return "3DSP Corporation";
	case 74:
		return "Accel Semiconductor Ltd.";
	case 75:
		return "Continental Automotive Systems";
	case 76:
		return "Apple, Inc.";
	case 77:
		return "Staccato Communications, Inc.";
	case 78:
		return "Avago Technologies";
	case 79:
		return "APT Ltd.";
	case 80:
		return "SiRF Technology, Inc.";
	case 81:
		return "Tzero Technologies, Inc.";
	case 82:
		return "J&M Corporation";
	case 83:
		return "Free2move AB";
	case 84:
		return "3DiJoy Corporation";
	case 85:
		return "Plantronics, Inc.";
	case 86:
		return "Sony Ericsson Mobile Communications";
	case 87:
		return "Harman International Industries, Inc.";
	case 88:
		return "Vizio, Inc.";
	case 89:
		return "Nordic Semiconductor ASA";
	case 90:
		return "EM Microelectronic-Marin SA";
	case 91:
		return "Ralink Technology Corporation";
	case 92:
		return "Belkin International, Inc.";
	case 93:
		return "Realtek Semiconductor Corporation";
	case 94:
		return "Stonestreet One, LLC";
	case 95:
		return "Wicentric, Inc.";
	case 96:
		return "RivieraWaves S.A.S";
	case 97:
		return "RDA Microelectronics";
	case 98:
		return "Gibson Guitars";
	case 65535:
		return "internal use";
	default:
		return "not assigned";
	}
}
