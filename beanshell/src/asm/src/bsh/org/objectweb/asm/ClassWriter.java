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
 * A {@link ClassVisitor ClassVisitor} that generates Java class files. More
 * precisely this visitor generates a byte array conforming to the Java class
 * file format. It can be used alone, to generate a Java class "from scratch",
 * or with one or more {@link ClassReader ClassReader} and adapter class
 * visitor to generate a modified class from one or more existing Java classes.
 */

public class ClassWriter implements ClassVisitor {

  /**
   * The type of CONSTANT_Class constant pool items.
   */

  final static int CLASS = 7;

  /**
   * The type of CONSTANT_Fieldref constant pool items.
   */

  final static int FIELD = 9;

  /**
   * The type of CONSTANT_Methodref constant pool items.
   */

  final static int METH = 10;

  /**
   * The type of CONSTANT_InterfaceMethodref constant pool items.
   */

  final static int IMETH = 11;

  /**
   * The type of CONSTANT_String constant pool items.
   */

  final static int STR = 8;

  /**
   * The type of CONSTANT_Integer constant pool items.
   */

  final static int INT = 3;

  /**
   * The type of CONSTANT_Float constant pool items.
   */

  final static int FLOAT = 4;

  /**
   * The type of CONSTANT_Long constant pool items.
   */

  final static int LONG = 5;

  /**
   * The type of CONSTANT_Double constant pool items.
   */

  final static int DOUBLE = 6;

  /**
   * The type of CONSTANT_NameAndType constant pool items.
   */

  final static int NAME_TYPE = 12;

  /**
   * The type of CONSTANT_Utf8 constant pool items.
   */

  final static int UTF8 = 1;

  /**
   * Index of the next item to be added in the constant pool.
   */

  private short index;

  /**
   * The constant pool of this class.
   */

  private ByteVector pool;

  /**
   * The constant pool's hash table data.
   */

  private Item[] table;

  /**
   * The threshold of the constant pool's hash table.
   */

  private int threshold;

  /**
   * The access flags of this class.
   */

  private int access;

  /**
   * The constant pool item that contains the internal name of this class.
   */

  private int name;

  /**
   * The constant pool item that contains the internal name of the super class
   * of this class.
   */

  private int superName;

  /**
   * Number of interfaces implemented or extended by this class or interface.
   */

  private int interfaceCount;

  /**
   * The interfaces implemented or extended by this class or interface. More
   * precisely, this array contains the indexes of the constant pool items
   * that contain the internal names of these interfaces.
   */

  private int[] interfaces;

  /**
   * The constant pool item that contains the name of the source file from
   * which this class was compiled.
   */

  private Item sourceFile;

  /**
   * Number of fields of this class.
   */

  private int fieldCount;

  /**
   * The fields of this class.
   */

  private ByteVector fields;

  /**
   * <tt>true</tt> if the maximum stack size and number of local variables must
   * be automatically computed.
   */

  private boolean computeMaxs;

  /**
   * The methods of this class. These methods are stored in a linked list of
   * {@link CodeWriter CodeWriter} objects, linked to each other by their {@link
   * CodeWriter#next} field. This field stores the first element of this list.
   */

  CodeWriter firstMethod;

  /**
   * The methods of this class. These methods are stored in a linked list of
   * {@link CodeWriter CodeWriter} objects, linked to each other by their {@link
   * CodeWriter#next} field. This field stores the last element of this list.
   */

  CodeWriter lastMethod;

  /**
   * The number of entries in the InnerClasses attribute.
   */

  private int innerClassesCount;

  /**
   * The InnerClasses attribute.
   */

  private ByteVector innerClasses;

  /**
   * A reusable key used to look for items in the hash {@link #table table}.
   */

  Item key;

  /**
   * A reusable key used to look for items in the hash {@link #table table}.
   */

  Item key2;

  /**
   * A reusable key used to look for items in the hash {@link #table table}.
   */

  Item key3;

  /**
   * The type of instructions without any label.
   */

  final static int NOARG_INSN = 0;

  /**
   * The type of instructions with an signed byte label.
   */

  final static int SBYTE_INSN = 1;

  /**
   * The type of instructions with an signed short label.
   */

  final static int SHORT_INSN = 2;

  /**
   * The type of instructions with a local variable index label.
   */

  final static int VAR_INSN = 3;

  /**
   * The type of instructions with an implicit local variable index label.
   */

  final static int IMPLVAR_INSN = 4;

  /**
   * The type of instructions with a type descriptor argument.
   */

  final static int TYPE_INSN = 5;

  /**
   * The type of field and method invocations instructions.
   */

  final static int FIELDORMETH_INSN = 6;

  /**
   * The type of the INVOKEINTERFACE instruction.
   */

  final static int ITFMETH_INSN = 7;

  /**
   * The type of instructions with a 2 bytes bytecode offset label.
   */

  final static int LABEL_INSN = 8;

  /**
   * The type of instructions with a 4 bytes bytecode offset label.
   */

  final static int LABELW_INSN = 9;

  /**
   * The type of the LDC instruction.
   */

  final static int LDC_INSN = 10;

  /**
   * The type of the LDC_W and LDC2_W instructions.
   */

  final static int LDCW_INSN = 11;

  /**
   * The type of the IINC instruction.
   */

  final static int IINC_INSN = 12;

  /**
   * The type of the TABLESWITCH instruction.
   */

  final static int TABL_INSN = 13;

  /**
   * The type of the LOOKUPSWITCH instruction.
   */

  final static int LOOK_INSN = 14;

  /**
   * The type of the MULTIANEWARRAY instruction.
   */

  final static int MANA_INSN = 15;

  /**
   * The type of the WIDE instruction.
   */

  final static int WIDE_INSN = 16;

  /**
   * The instruction types of all JVM opcodes.
   */

  static byte[] TYPE;

  // --------------------------------------------------------------------------
  // Static initializer
  // --------------------------------------------------------------------------

  /**
   * Computes the instruction types of JVM opcodes.
   */

  static {
    int i;
    byte[] b = new byte[220];
    String s =
      "AAAAAAAAAAAAAAAABCKLLDDDDDEEEEEEEEEEEEEEEEEEEEAAAAAAAADDDDDEEEEEEEEE" +
      "EEEEEEEEEEEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAA" +
      "AAAAAAAAAAAAAAAAAIIIIIIIIIIIIIIIIDNOAAAAAAGGGGGGGHAFBFAAFFAAQPIIJJII" +
      "IIIIIIIIIIIIIIII";
    for (i = 0; i < b.length; ++i) {
      b[i] = (byte)(s.charAt(i) - 'A');
    }
    TYPE = b;

    /* code to generate the above string

    // SBYTE_INSN instructions
    b[Constants.NEWARRAY] = SBYTE_INSN;
    b[Constants.BIPUSH] = SBYTE_INSN;

    // SHORT_INSN instructions
    b[Constants.SIPUSH] = SHORT_INSN;

    // (IMPL)VAR_INSN instructions
    b[Constants.RET] = VAR_INSN;
    for (i = Constants.ILOAD; i <= Constants.ALOAD; ++i) {
      b[i] = VAR_INSN;
    }
    for (i = Constants.ISTORE; i <= Constants.ASTORE; ++i) {
      b[i] = VAR_INSN;
    }
    for (i = 26; i <= 45; ++i) { // ILOAD_0 to ALOAD_3
      b[i] = IMPLVAR_INSN;
    }
    for (i = 59; i <= 78; ++i) { // ISTORE_0 to ASTORE_3
      b[i] = IMPLVAR_INSN;
    }

    // TYPE_INSN instructions
    b[Constants.NEW] = TYPE_INSN;
    b[Constants.ANEWARRAY] = TYPE_INSN;
    b[Constants.CHECKCAST] = TYPE_INSN;
    b[Constants.INSTANCEOF] = TYPE_INSN;

    // (Set)FIELDORMETH_INSN instructions
    for (i = Constants.GETSTATIC; i <= Constants.INVOKESTATIC; ++i) {
      b[i] = FIELDORMETH_INSN;
    }
    b[Constants.INVOKEINTERFACE] = ITFMETH_INSN;

    // LABEL(W)_INSN instructions
    for (i = Constants.IFEQ; i <= Constants.JSR; ++i) {
      b[i] = LABEL_INSN;
    }
    b[Constants.IFNULL] = LABEL_INSN;
    b[Constants.IFNONNULL] = LABEL_INSN;
    b[200] = LABELW_INSN; // GOTO_W
    b[201] = LABELW_INSN; // JSR_W
    // temporary opcodes used internally by ASM - see Label and CodeWriter
    for (i = 202; i < 220; ++i) {
      b[i] = LABEL_INSN;
    }

    // LDC(_W) instructions
    b[Constants.LDC] = LDC_INSN;
    b[19] = LDCW_INSN; // LDC_W
    b[20] = LDCW_INSN; // LDC2_W

    // special instructions
    b[Constants.IINC] = IINC_INSN;
    b[Constants.TABLESWITCH] = TABL_INSN;
    b[Constants.LOOKUPSWITCH] = LOOK_INSN;
    b[Constants.MULTIANEWARRAY] = MANA_INSN;
    b[196] = WIDE_INSN; // WIDE

    for (i = 0; i < b.length; ++i) {
      System.err.print((char)('A' + b[i]));
    }
    System.err.println();
    */
  }

  // --------------------------------------------------------------------------
  // Constructor
  // --------------------------------------------------------------------------

  /**
   * Constructs a new {@link ClassWriter ClassWriter} object.
   *
   * @param computeMaxs <tt>true</tt> if the maximum stack size and the maximum
   *      number of local variables must be automatically computed. If this flag
   *      is <tt>true</tt>, then the arguments of the {@link
   *      CodeVisitor#visitMaxs visitMaxs} method of the {@link CodeVisitor
   *      CodeVisitor} returned by the {@link #visitMethod visitMethod} method
   *      will be ignored, and computed automatically from the signature and
   *      the bytecode of each method.
   */

  public ClassWriter (final boolean computeMaxs) {
    index = 1;
    pool = new ByteVector();
    table = new Item[64];
    threshold = (int)(0.75d*table.length);
    key = new Item();
    key2 = new Item();
    key3 = new Item();
    this.computeMaxs = computeMaxs;
  }

  // --------------------------------------------------------------------------
  // Implementation of the ClassVisitor interface
  // --------------------------------------------------------------------------

  public void visit (
    final int access,
    final String name,
    final String superName,
    final String[] interfaces,
    final String sourceFile)
  {
    this.access = access;
    this.name = newClass(name).index;
    this.superName = superName == null ? 0 : newClass(superName).index;
    if (interfaces != null && interfaces.length > 0) {
      interfaceCount = interfaces.length;
      this.interfaces = new int[interfaceCount];
      for (int i = 0; i < interfaceCount; ++i) {
        this.interfaces[i] = newClass(interfaces[i]).index;
      }
    }
    if (sourceFile != null) {
      newUTF8("SourceFile");
      this.sourceFile = newUTF8(sourceFile);
    }
    if ((access & Constants.ACC_DEPRECATED) != 0) {
      newUTF8("Deprecated");
    }
  }

  public void visitInnerClass (
    final String name,
    final String outerName,
    final String innerName,
    final int access)
  {
    if (innerClasses == null) {
      newUTF8("InnerClasses");
      innerClasses = new ByteVector();
    }
    ++innerClassesCount;
    innerClasses.put2(name == null ? 0 : newClass(name).index);
    innerClasses.put2(outerName == null ? 0 : newClass(outerName).index);
    innerClasses.put2(innerName == null ? 0 : newUTF8(innerName).index);
    innerClasses.put2(access);
  }

  public void visitField (
    final int access,
    final String name,
    final String desc,
    final Object value)
  {
    ++fieldCount;
    if (fields == null) {
      fields = new ByteVector();
    }
    fields.put2(access).put2(newUTF8(name).index).put2(newUTF8(desc).index);
    int attributeCount = 0;
    if (value != null) {
      ++attributeCount;
    }
    if ((access & Constants.ACC_SYNTHETIC) != 0) {
      ++attributeCount;
    }
    if ((access & Constants.ACC_DEPRECATED) != 0) {
      ++attributeCount;
    }
    fields.put2(attributeCount);
    if (value != null) {
      fields.put2(newUTF8("ConstantValue").index);
      fields.put4(2).put2(newCst(value).index);
    }
    if ((access & Constants.ACC_SYNTHETIC) != 0) {
      fields.put2(newUTF8("Synthetic").index).put4(0);
    }
    if ((access & Constants.ACC_DEPRECATED) != 0) {
      fields.put2(newUTF8("Deprecated").index).put4(0);
    }
  }

  public CodeVisitor visitMethod (
    final int access,
    final String name,
    final String desc,
    final String[] exceptions)
  {
    CodeWriter cw = new CodeWriter(this, computeMaxs);
    cw.init(access, name, desc, exceptions);
    return cw;
  }

  public void visitEnd () {
  }

  // --------------------------------------------------------------------------
  // Other public methods
  // --------------------------------------------------------------------------

  /**
   * Returns the bytecode of the class that was build with this class writer.
   *
   * @return the bytecode of the class that was build with this class writer.
   */

  public byte[] toByteArray () {
    // computes the real size of the bytecode of this class
    int size = 24 + 2*interfaceCount;
    if (fields != null) {
      size += fields.length;
    }
    int nbMethods = 0;
    CodeWriter cb = firstMethod;
    while (cb != null) {
      ++nbMethods;
      size += cb.getSize();
      cb = cb.next;
    }
    size += pool.length;
    int attributeCount = 0;
    if (sourceFile != null) {
      ++attributeCount;
      size += 8;
    }
    if ((access & Constants.ACC_DEPRECATED) != 0) {
      ++attributeCount;
      size += 6;
    }
    if (innerClasses != null) {
      ++attributeCount;
      size += 8 + innerClasses.length;
    }
    // allocates a byte vector of this size, in order to avoid unnecessary
    // arraycopy operations in the ByteVector.enlarge() method
    ByteVector out = new ByteVector(size);
    out.put4(0xCAFEBABE).put2(3).put2(45);
    out.put2(index).putByteArray(pool.data, 0, pool.length);
    out.put2(access).put2(name).put2(superName);
    out.put2(interfaceCount);
    for (int i = 0; i < interfaceCount; ++i) {
      out.put2(interfaces[i]);
    }
    out.put2(fieldCount);
    if (fields != null) {
      out.putByteArray(fields.data, 0, fields.length);
    }
    out.put2(nbMethods);
    cb = firstMethod;
    while (cb != null) {
      cb.put(out);
      cb = cb.next;
    }
    out.put2(attributeCount);
    if (sourceFile != null) {
      out.put2(newUTF8("SourceFile").index).put4(2).put2(sourceFile.index);
    }
    if ((access & Constants.ACC_DEPRECATED) != 0) {
      out.put2(newUTF8("Deprecated").index).put4(0);
    }
    if (innerClasses != null) {
      out.put2(newUTF8("InnerClasses").index);
      out.put4(innerClasses.length + 2).put2(innerClassesCount);
      out.putByteArray(innerClasses.data, 0, innerClasses.length);
    }
    return out.data;
  }

  // --------------------------------------------------------------------------
  // Utility methods: constant pool management
  // --------------------------------------------------------------------------

  /**
   * Adds a number or string constant to the constant pool of the class being
   * build. Does nothing if the constant pool already contains a similar item.
   *
   * @param cst the value of the constant to be added to the constant pool. This
   *      parameter must be an {@link java.lang.Integer Integer}, a {@link
   *      java.lang.Float Float}, a {@link java.lang.Long Long}, a {@link
          java.lang.Double Double} or a {@link String String}.
   * @return a new or already existing constant item with the given value.
   */

  Item newCst (final Object cst) {
    if (cst instanceof Integer) {
      int val = ((Integer)cst).intValue();
      return newInteger(val);
    } else if (cst instanceof Float) {
      float val = ((Float)cst).floatValue();
      return newFloat(val);
    } else if (cst instanceof Long) {
      long val = ((Long)cst).longValue();
      return newLong(val);
    } else if (cst instanceof Double) {
      double val = ((Double)cst).doubleValue();
      return newDouble(val);
    } else if (cst instanceof String) {
      return newString((String)cst);
    } else {
      throw new IllegalArgumentException("value " + cst);
    }
  }

  /**
   * Adds an UTF string to the constant pool of the class being build. Does
   * nothing if the constant pool already contains a similar item.
   *
   * @param value the String value.
   * @return a new or already existing UTF8 item.
   */

  Item newUTF8 (final String value) {
    key.set(UTF8, value, null, null);
    Item result = get(key);
    if (result == null) {
      pool.put1(UTF8).putUTF(value);
      result = new Item(index++, key);
      put(result);
    }
    return result;
  }

  /**
   * Adds a class reference to the constant pool of the class being build. Does
   * nothing if the constant pool already contains a similar item.
   *
   * @param value the internal name of the class.
   * @return a new or already existing class reference item.
   */

  Item newClass (final String value) {
    key2.set(CLASS, value, null, null);
    Item result = get(key2);
    if (result == null) {
      pool.put12(CLASS, newUTF8(value).index);
      result = new Item(index++, key2);
      put(result);
    }
    return result;
  }

  /**
   * Adds a field reference to the constant pool of the class being build. Does
   * nothing if the constant pool already contains a similar item.
   *
   * @param owner the internal name of the field's owner class.
   * @param name the field's name.
   * @param desc the field's descriptor.
   * @return a new or already existing field reference item.
   */

  Item newField (
    final String owner,
    final String name,
    final String desc)
  {
    key3.set(FIELD, owner, name, desc);
    Item result = get(key3);
    if (result == null) {
      put122(FIELD, newClass(owner).index, newNameType(name, desc).index);
      result = new Item(index++, key3);
      put(result);
    }
    return result;
  }

  /**
   * Adds a method reference to the constant pool of the class being build. Does
   * nothing if the constant pool already contains a similar item.
   *
   * @param owner the internal name of the method's owner class.
   * @param name the method's name.
   * @param desc the method's descriptor.
   * @return a new or already existing method reference item.
   */

  Item newMethod (
    final String owner,
    final String name,
    final String desc)
  {
    key3.set(METH, owner, name, desc);
    Item result = get(key3);
    if (result == null) {
      put122(METH, newClass(owner).index, newNameType(name, desc).index);
      result = new Item(index++, key3);
      put(result);
    }
    return result;
  }

  /**
   * Adds an interface method reference to the constant pool of the class being
   * build. Does nothing if the constant pool already contains a similar item.
   *
   * @param ownerItf the internal name of the method's owner interface.
   * @param name the method's name.
   * @param desc the method's descriptor.
   * @return a new or already existing interface method reference item.
   */

  Item newItfMethod (
    final String ownerItf,
    final String name,
    final String desc)
  {
    key3.set(IMETH, ownerItf, name, desc);
    Item result = get(key3);
    if (result == null) {
      put122(IMETH, newClass(ownerItf).index, newNameType(name, desc).index);
      result = new Item(index++, key3);
      put(result);
    }
    return result;
  }

  /**
   * Adds an integer to the constant pool of the class being build. Does nothing
   * if the constant pool already contains a similar item.
   *
   * @param value the int value.
   * @return a new or already existing int item.
   */

  private Item newInteger (final int value) {
    key.set(value);
    Item result = get(key);
    if (result == null) {
      pool.put1(INT).put4(value);
      result = new Item(index++, key);
      put(result);
    }
    return result;
  }

  /**
   * Adds a float to the constant pool of the class being build. Does nothing if
   * the constant pool already contains a similar item.
   *
   * @param value the float value.
   * @return a new or already existing float item.
   */

  private Item newFloat (final float value) {
    key.set(value);
    Item result = get(key);
    if (result == null) {
      pool.put1(FLOAT).put4(Float.floatToIntBits(value));
      result = new Item(index++, key);
      put(result);
    }
    return result;
  }

  /**
   * Adds a long to the constant pool of the class being build. Does nothing if
   * the constant pool already contains a similar item.
   *
   * @param value the long value.
   * @return a new or already existing long item.
   */

  private Item newLong (final long value) {
    key.set(value);
    Item result = get(key);
    if (result == null) {
      pool.put1(LONG).put8(value);
      result = new Item(index, key);
      put(result);
      index += 2;
    }
    return result;
  }

  /**
   * Adds a double to the constant pool of the class being build. Does nothing
   * if the constant pool already contains a similar item.
   *
   * @param value the double value.
   * @return a new or already existing double item.
   */

  private Item newDouble (final double value) {
    key.set(value);
    Item result = get(key);
    if (result == null) {
      pool.put1(DOUBLE).put8(Double.doubleToLongBits(value));
      result = new Item(index, key);
      put(result);
      index += 2;
    }
    return result;
  }

  /**
   * Adds a string to the constant pool of the class being build. Does nothing
   * if the constant pool already contains a similar item.
   *
   * @param value the String value.
   * @return a new or already existing string item.
   */

  private Item newString (final String value) {
    key2.set(STR, value, null, null);
    Item result = get(key2);
    if (result == null) {
      pool.put12(STR, newUTF8(value).index);
      result = new Item(index++, key2);
      put(result);
    }
    return result;
  }

  /**
   * Adds a name and type to the constant pool of the class being build. Does
   * nothing if the constant pool already contains a similar item.
   *
   * @param name a name.
   * @param desc a type descriptor.
   * @return a new or already existing name and type item.
   */

  private Item newNameType (final String name, final String desc) {
    key2.set(NAME_TYPE, name, desc, null);
    Item result = get(key2);
    if (result == null) {
      put122(NAME_TYPE, newUTF8(name).index, newUTF8(desc).index);
      result = new Item(index++, key2);
      put(result);
    }
    return result;
  }

  /**
   * Returns the constant pool's hash table item which is equal to the given
   * item.
   *
   * @param key a constant pool item.
   * @return the constant pool's hash table item which is equal to the given
   *      item, or <tt>null</tt> if there is no such item.
   */

  private Item get (final Item key) {
    Item tab[] = table;
    int hashCode = key.hashCode;
    int index = (hashCode & 0x7FFFFFFF) % tab.length;
    for (Item i = tab[index]; i != null; i = i.next) {
      if (i.hashCode == hashCode && key.isEqualTo(i)) {
        return i;
      }
    }
    return null;
  }

  /**
   * Puts the given item in the constant pool's hash table. The hash table
   * <i>must</i> not already contains this item.
   *
   * @param i the item to be added to the constant pool's hash table.
   */

  private void put (final Item i) {
    if (index > threshold) {
      int oldCapacity = table.length;
      Item oldMap[] = table;
      int newCapacity = oldCapacity * 2 + 1;
      Item newMap[] = new Item[newCapacity];
      threshold = (int)(newCapacity * 0.75);
      table = newMap;
      for (int j = oldCapacity; j-- > 0; ) {
        for (Item old = oldMap[j]; old != null; ) {
          Item e = old;
          old = old.next;
          int index = (e.hashCode & 0x7FFFFFFF) % newCapacity;
          e.next = newMap[index];
          newMap[index] = e;
        }
      }
    }
    int index = (i.hashCode & 0x7FFFFFFF) % table.length;
    i.next = table[index];
    table[index] = i;
  }

  /**
   * Puts one byte and two shorts into the constant pool.
   *
   * @param b a byte.
   * @param s1 a short.
   * @param s2 another short.
   */

  private void put122 (final int b, final int s1, final int s2) {
    pool.put12(b, s1).put2(s2);
  }
}
