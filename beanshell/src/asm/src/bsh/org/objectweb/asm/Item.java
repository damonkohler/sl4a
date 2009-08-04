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
 * A constant pool item. Constant pool items can be created with the 'newXXX'
 * methods in the {@link ClassWriter} class.
 */

final class Item {

  /**
   * Index of this item in the constant pool.
   */

  short index;

  /**
   * Type of this constant pool item. A single class is used to represent all
   * constant pool item types, in order to minimize the bytecode size of this
   * package. The value of this field is one of the constants defined in the
   * {@link ClassWriter ClassWriter} class.
   */

  int type;

  /**
   * Value of this item, for a {@link ClassWriter#INT INT} item.
   */

  int intVal;

  /**
   * Value of this item, for a {@link ClassWriter#LONG LONG} item.
   */

  long longVal;

  /**
   * Value of this item, for a {@link ClassWriter#FLOAT FLOAT} item.
   */

  float floatVal;

  /**
   * Value of this item, for a {@link ClassWriter#DOUBLE DOUBLE} item.
   */

  double doubleVal;

  /**
   * First part of the value of this item, for items that do not hold a
   * primitive value.
   */

  String strVal1;

  /**
   * Second part of the value of this item, for items that do not hold a
   * primitive value.
   */

  String strVal2;

  /**
   * Third part of the value of this item, for items that do not hold a
   * primitive value.
   */

  String strVal3;

  /**
   * The hash code value of this constant pool item.
   */

  int hashCode;

  /**
   * Link to another constant pool item, used for collision lists in the
   * constant pool's hash table.
   */

  Item next;

  /**
   * Constructs an uninitialized {@link Item Item} object.
   */

  Item () {
  }

  /**
   * Constructs a copy of the given item.
   *
   * @param index index of the item to be constructed.
   * @param i the item that must be copied into the item to be constructed.
   */

  Item (final short index, final Item i) {
    this.index = index;
    type = i.type;
    intVal = i.intVal;
    longVal = i.longVal;
    floatVal = i.floatVal;
    doubleVal = i.doubleVal;
    strVal1 = i.strVal1;
    strVal2 = i.strVal2;
    strVal3 = i.strVal3;
    hashCode = i.hashCode;
  }

  /**
   * Sets this item to an {@link ClassWriter#INT INT} item.
   *
   * @param intVal the value of this item.
   */

  void set (final int intVal) {
    this.type = ClassWriter.INT;
    this.intVal = intVal;
    this.hashCode = type + intVal;
  }

  /**
   * Sets this item to a {@link ClassWriter#LONG LONG} item.
   *
   * @param longVal the value of this item.
   */

  void set (final long longVal) {
    this.type = ClassWriter.LONG;
    this.longVal = longVal;
    this.hashCode = type + (int)longVal;
  }

  /**
   * Sets this item to a {@link ClassWriter#FLOAT FLOAT} item.
   *
   * @param floatVal the value of this item.
   */

  void set (final float floatVal) {
    this.type = ClassWriter.FLOAT;
    this.floatVal = floatVal;
    this.hashCode = type + (int)floatVal;
  }

  /**
   * Sets this item to a {@link ClassWriter#DOUBLE DOUBLE} item.
   *
   * @param doubleVal the value of this item.
   */

  void set (final double doubleVal) {
    this.type = ClassWriter.DOUBLE;
    this.doubleVal = doubleVal;
    this.hashCode = type + (int)doubleVal;
  }

  /**
   * Sets this item to an item that do not hold a primitive value.
   *
   * @param type the type of this item.
   * @param strVal1 first part of the value of this item.
   * @param strVal2 second part of the value of this item.
   * @param strVal3 third part of the value of this item.
   */

  void set (
    final int type,
    final String strVal1,
    final String strVal2,
    final String strVal3)
  {
    this.type = type;
    this.strVal1 = strVal1;
    this.strVal2 = strVal2;
    this.strVal3 = strVal3;
    switch (type) {
      case ClassWriter.UTF8:
      case ClassWriter.STR:
      case ClassWriter.CLASS:
        hashCode = type + strVal1.hashCode();
        return;
      case ClassWriter.NAME_TYPE:
        hashCode = type + strVal1.hashCode()*strVal2.hashCode();
        return;
      //case ClassWriter.FIELD:
      //case ClassWriter.METH:
      //case ClassWriter.IMETH:
      default:
        hashCode = type + strVal1.hashCode()*strVal2.hashCode()*strVal3.hashCode();
        return;
    }
  }

  /**
   * Indicates if the given item is equal to this one.
   *
   * @param i the item to be compared to this one.
   * @return <tt>true</tt> if the given item if equal to this one,
   *      <tt>false</tt> otherwise.
   */

  boolean isEqualTo (final Item i) {
    if (i.type == type) {
      switch (type) {
        case ClassWriter.INT:
          return i.intVal == intVal;
        case ClassWriter.LONG:
          return i.longVal == longVal;
        case ClassWriter.FLOAT:
          return i.floatVal == floatVal;
        case ClassWriter.DOUBLE:
          return i.doubleVal == doubleVal;
        case ClassWriter.UTF8:
        case ClassWriter.STR:
        case ClassWriter.CLASS:
          return i.strVal1.equals(strVal1);
        case ClassWriter.NAME_TYPE:
          return i.strVal1.equals(strVal1) &&
                 i.strVal2.equals(strVal2);
        //case ClassWriter.FIELD:
        //case ClassWriter.METH:
        //case ClassWriter.IMETH:
        default:
          return i.strVal1.equals(strVal1) &&
                 i.strVal2.equals(strVal2) &&
                 i.strVal3.equals(strVal3);
      }
    }
    return false;
  }
}
