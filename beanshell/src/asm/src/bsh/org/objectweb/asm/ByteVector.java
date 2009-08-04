/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (C) 2000 INRIA, France Telecom
 * Copyright (C) 2002 France Telecom
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */

package bsh.org.objectweb.asm;

/**
 * A dynamically extensible vector of bytes. This class is roughly equivalent to
 * a DataOutputStream on top of a ByteArrayOutputStream, but is more efficient.
 */

final class ByteVector {

  /**
   * The content of this vector.
   */

  byte[] data;

  /**
   * Actual number of bytes in this vector.
   */

  int length;

  /**
   * Constructs a new {@link ByteVector ByteVector} with a default initial size.
   */

  public ByteVector () {
    data = new byte[64];
  }

  /**
   * Constructs a new {@link ByteVector ByteVector} with the given initial size.
   *
   * @param initialSize the initial size of the byte vector to be constructed.
   */

  public ByteVector (final int initialSize) {
    data = new byte[initialSize];
  }

  /**
   * Puts a byte into this byte vector. The byte vector is automatically
   * enlarged if necessary.
   *
   * @param b a byte.
   * @return this byte vector.
   */

  public ByteVector put1 (final int b) {
    int length = this.length;
    if (length + 1 > data.length) {
      enlarge(1);
    }
    data[length++] = (byte)b;
    this.length = length;
    return this;
  }

  /**
   * Puts two bytes into this byte vector. The byte vector is automatically
   * enlarged if necessary.
   *
   * @param b1 a byte.
   * @param b2 another byte.
   * @return this byte vector.
   */

  public ByteVector put11 (final int b1, final int b2) {
    int length = this.length;
    if (length + 2 > data.length) {
      enlarge(2);
    }
    byte[] data = this.data;
    data[length++] = (byte)b1;
    data[length++] = (byte)b2;
    this.length = length;
    return this;
  }

  /**
   * Puts a short into this byte vector. The byte vector is automatically
   * enlarged if necessary.
   *
   * @param s a short.
   * @return this byte vector.
   */

  public ByteVector put2 (final int s) {
    int length = this.length;
    if (length + 2 > data.length) {
      enlarge(2);
    }
    byte[] data = this.data;
    data[length++] = (byte)(s >>> 8);
    data[length++] = (byte)s;
    this.length = length;
    return this;
  }

  /**
   * Puts a byte and a short into this byte vector. The byte vector is
   * automatically enlarged if necessary.
   *
   * @param b a byte.
   * @param s a short.
   * @return this byte vector.
   */

  public ByteVector put12 (final int b, final int s) {
    int length = this.length;
    if (length + 3 > data.length) {
      enlarge(3);
    }
    byte[] data = this.data;
    data[length++] = (byte)b;
    data[length++] = (byte)(s >>> 8);
    data[length++] = (byte)s;
    this.length = length;
    return this;
  }

  /**
   * Puts an int into this byte vector. The byte vector is automatically
   * enlarged if necessary.
   *
   * @param i an int.
   * @return this byte vector.
   */

  public ByteVector put4 (final int i) {
    int length = this.length;
    if (length + 4 > data.length) {
      enlarge(4);
    }
    byte[] data = this.data;
    data[length++] = (byte)(i >>> 24);
    data[length++] = (byte)(i >>> 16);
    data[length++] = (byte)(i >>> 8);
    data[length++] = (byte)i;
    this.length = length;
    return this;
  }

  /**
   * Puts a long into this byte vector. The byte vector is automatically
   * enlarged if necessary.
   *
   * @param l a long.
   * @return this byte vector.
   */

  public ByteVector put8 (final long l) {
    int length = this.length;
    if (length + 8 > data.length) {
      enlarge(8);
    }
    byte[] data = this.data;
    int i = (int)(l >>> 32);
    data[length++] = (byte)(i >>> 24);
    data[length++] = (byte)(i >>> 16);
    data[length++] = (byte)(i >>> 8);
    data[length++] = (byte)i;
    i = (int)l;
    data[length++] = (byte)(i >>> 24);
    data[length++] = (byte)(i >>> 16);
    data[length++] = (byte)(i >>> 8);
    data[length++] = (byte)i;
    this.length = length;
    return this;
  }

  /**
   * Puts a String in UTF format into this byte vector. The byte vector is
   * automatically enlarged if necessary.
   *
   * @param s a String.
   * @return this byte vector.
   */

  public ByteVector putUTF (final String s) {
    int charLength = s.length();
    int byteLength = 0;
    for (int i = 0; i < charLength; ++i) {
      char c = s.charAt(i);
      if (c >= '\001' && c <= '\177') {
        byteLength++;
      } else if (c > '\u07FF') {
        byteLength += 3;
      } else {
        byteLength += 2;
      }
    }
    if (byteLength > 65535) {
      throw new IllegalArgumentException();
    }
    int length = this.length;
    if (length + 2 + byteLength > data.length) {
      enlarge(2 + byteLength);
    }
    byte[] data = this.data;
    data[length++] = (byte)(byteLength >>> 8);
    data[length++] = (byte)(byteLength);
    for (int i = 0; i < charLength; ++i) {
      char c = s.charAt(i);
      if (c >= '\001' && c <= '\177') {
        data[length++] = (byte)c;
      } else if (c > '\u07FF') {
        data[length++] = (byte)(0xE0 | c >> 12 & 0xF);
        data[length++] = (byte)(0x80 | c >> 6 & 0x3F);
        data[length++] = (byte)(0x80 | c & 0x3F);
      } else {
        data[length++] = (byte)(0xC0 | c >> 6 & 0x1F);
        data[length++] = (byte)(0x80 | c & 0x3F);
      }
    }
    this.length = length;
    return this;
  }

  /**
   * Puts an array of bytes into this byte vector. The byte vector is
   * automatically enlarged if necessary.
   *
   * @param b an array of bytes. May be <tt>null</tt> to put <tt>len</tt> null
   *      bytes into this byte vector.
   * @param off index of the fist byte of b that must be copied.
   * @param len number of bytes of b that must be copied.
   * @return this byte vector.
   */

  public ByteVector putByteArray (
    final byte[] b,
    final int off,
    final int len)
  {
    if (length + len > data.length) {
      enlarge(len);
    }
    if (b != null) {
      System.arraycopy(b, off, data, length, len);
    }
    length += len;
    return this;
  }

  /**
   * Enlarge this byte vector so that it can receive n more bytes.
   *
   * @param size number of additional bytes that this byte vector should be
   *      able to receive.
   */

  private void enlarge (final int size) {
    byte[] newData = new byte[Math.max(2*data.length, length + size)];
    System.arraycopy(data, 0, newData, 0, length);
    data = newData;
  }
}
