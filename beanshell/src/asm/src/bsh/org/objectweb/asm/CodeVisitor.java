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
 * A visitor to visit the bytecode instructions of a Java method. The methods
 * of this visitor must be called in the sequential order of the bytecode
 * instructions of the visited code. The {@link #visitMaxs visitMaxs} method
 * must be called after all the instructions have been visited. The {@link
 * #visitTryCatchBlock visitTryCatchBlock}, {@link #visitLocalVariable
 * visitLocalVariable} and {@link #visitLineNumber visitLineNumber} methods may
 * be called in any order, at any time (provided the labels passed as arguments
 * have already been visited with {@link #visitLabel visitLabel}).
 */

public interface CodeVisitor {

  /**
   * Visits a zero operand instruction.
   *
   * @param opcode the opcode of the instruction to be visited. This opcode is
   *      either NOP, ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2,
   *      ICONST_3, ICONST_4, ICONST_5, LCONST_0, LCONST_1, FCONST_0, FCONST_1,
   *      FCONST_2, DCONST_0, DCONST_1,
   *
   *      IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD,
   *      IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE,
   *      SASTORE,
   *
   *      POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP,
   *
   *      IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL,
   *      DMUL, IDIV, LDIV, FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG,
   *      FNEG, DNEG, ISHL, LSHL, ISHR, LSHR, IUSHR, LUSHR, IAND, LAND, IOR,
   *      LOR, IXOR, LXOR,
   *
   *      I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, I2B, I2C,
   *      I2S,
   *
   *      LCMP, FCMPL, FCMPG, DCMPL, DCMPG,
   *
   *      IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN,
   *
   *      ARRAYLENGTH,
   *
   *      ATHROW,
   *
   *      MONITORENTER, or MONITOREXIT.
   */

  void visitInsn (int opcode);

  /**
   * Visits an instruction with a single int operand.
   *
   * @param opcode the opcode of the instruction to be visited. This opcode is
   *      either BIPUSH, SIPUSH or NEWARRAY.
   * @param operand the operand of the instruction to be visited.
   */

  void visitIntInsn (int opcode, int operand);

  /**
   * Visits a local variable instruction. A local variable instruction is an
   * instruction that loads or stores the value of a local variable.
   *
   * @param opcode the opcode of the local variable instruction to be visited.
   *      This opcode is either ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE,
   *      LSTORE, FSTORE, DSTORE, ASTORE or RET.
   * @param var the operand of the instruction to be visited. This operand is
   *      the index of a local variable.
   */

  void visitVarInsn (int opcode, int var);

  /**
   * Visits a type instruction. A type instruction is an instruction that
   * takes a type descriptor as parameter.
   *
   * @param opcode the opcode of the type instruction to be visited. This opcode
   *      is either NEW, ANEWARRAY, CHECKCAST or INSTANCEOF.
   * @param desc the operand of the instruction to be visited. This operand is
   *      must be a fully qualified class name in internal form, or the type
   *      descriptor of an array type (see {@link Type Type}).
   */

  void visitTypeInsn (int opcode, String desc);

  /**
   * Visits a field instruction. A field instruction is an instruction that
   * loads or stores the value of a field of an object.
   *
   * @param opcode the opcode of the type instruction to be visited. This opcode
   *      is either GETSTATIC, PUTSTATIC, GETFIELD or PUTFIELD.
   * @param owner the internal name of the field's owner class (see {@link
   *      Type#getInternalName getInternalName}).
   * @param name the field's name.
   * @param desc the field's descriptor (see {@link Type Type}).
   */

  void visitFieldInsn (int opcode, String owner, String name, String desc);

  /**
   * Visits a method instruction. A method instruction is an instruction that
   * invokes a method.
   *
   * @param opcode the opcode of the type instruction to be visited. This opcode
   *      is either INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC or
   *      INVOKEINTERFACE.
   * @param owner the internal name of the method's owner class (see {@link
   *      Type#getInternalName getInternalName}).
   * @param name the method's name.
   * @param desc the method's descriptor (see {@link Type Type}).
   */

  void visitMethodInsn (int opcode, String owner, String name, String desc);

  /**
   * Visits a jump instruction. A jump instruction is an instruction that may
   * jump to another instruction.
   *
   * @param opcode the opcode of the type instruction to be visited. This opcode
   *      is either IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE,
   *      IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE,
   *      GOTO, JSR, IFNULL or IFNONNULL.
   * @param label the operand of the instruction to be visited. This operand is
   *      a label that designates the instruction to which the jump instruction
   *      may jump.
   */

  void visitJumpInsn (int opcode, Label label);

  /**
   * Visits a label. A label designates the instruction that will be visited
   * just after it.
   *
   * @param label a {@link Label Label} object.
   */

  void visitLabel (Label label);

  // -------------------------------------------------------------------------
  // Special instructions
  // -------------------------------------------------------------------------

  /**
   * Visits a LDC instruction.
   *
   * @param cst the constant to be loaded on the stack. This parameter must be
   *      a non null {@link java.lang.Integer Integer}, a {@link java.lang.Float
   *      Float}, a {@link java.lang.Long Long}, a {@link java.lang.Double
   *      Double} or a {@link String String}.
   */

  void visitLdcInsn (Object cst);

  /**
   * Visits an IINC instruction.
   *
   * @param var index of the local variable to be incremented.
   * @param increment amount to increment the local variable by.
   */

  void visitIincInsn (int var, int increment);

  /**
   * Visits a TABLESWITCH instruction.
   *
   * @param min the minimum key value.
   * @param max the maximum key value.
   * @param dflt beginning of the default handler block.
   * @param labels beginnings of the handler blocks. <tt>labels[i]</tt> is the
   *      beginning of the handler block for the <tt>min + i</tt> key.
   */

  void visitTableSwitchInsn (int min, int max, Label dflt, Label labels[]);

  /**
   * Visits a LOOKUPSWITCH instruction.
   *
   * @param dflt beginning of the default handler block.
   * @param keys the values of the keys.
   * @param labels beginnings of the handler blocks. <tt>labels[i]</tt> is the
   *      beginning of the handler block for the <tt>keys[i]</tt> key.
   */

  void visitLookupSwitchInsn (Label dflt, int keys[], Label labels[]);

  /**
   * Visits a MULTIANEWARRAY instruction.
   *
   * @param desc an array type descriptor (see {@link Type Type}).
   * @param dims number of dimensions of the array to allocate.
   */

  void visitMultiANewArrayInsn (String desc, int dims);

  // -------------------------------------------------------------------------
  // Exceptions table entries, max stack size and max locals
  // -------------------------------------------------------------------------

  /**
   * Visits a try catch block.
   *
   * @param start beginning of the exception handler's scope (inclusive).
   * @param end end of the exception handler's scope (exclusive).
   * @param handler beginning of the exception handler's code.
   * @param type internal name of the type of exceptions handled by the handler,
   *      or <tt>null</tt> to catch any exceptions (for "finally" blocks).
   * @throws IllegalArgumentException if one of the labels has not already been
   *      visited by this visitor (by the {@link #visitLabel visitLabel}
   *      method).
   */

  void visitTryCatchBlock (Label start, Label end, Label handler, String type);

  /**
   * Visits the maximum stack size and the maximum number of local variables of
   * the method.
   *
   * @param maxStack maximum stack size of the method.
   * @param maxLocals maximum number of local variables for the method.
   */

  void visitMaxs (int maxStack, int maxLocals);

  // -------------------------------------------------------------------------
  // Debug information
  // -------------------------------------------------------------------------

  /**
   * Visits a local variable declaration.
   *
   * @param name the name of a local variable.
   * @param desc the type descriptor of this local variable.
   * @param start the first instruction corresponding to the scope of this
   *      local variable (inclusive).
   * @param end the last instruction corresponding to the scope of this
   *      local variable (exclusive).
   * @param index the local variable's index.
   * @throws IllegalArgumentException if one of the labels has not already been
   *      visited by this visitor (by the {@link #visitLabel visitLabel}
   *      method).
   */

  void visitLocalVariable (
    String name,
    String desc,
    Label start,
    Label end,
    int index);

  /**
   * Visits a line number declaration.
   *
   * @param line a line number. This number refers to the source file
   *      from which the class was compiled.
   * @param start the first instruction corresponding to this line number.
   * @throws IllegalArgumentException if <tt>start</tt> has not already been
   *      visited by this visitor (by the {@link #visitLabel visitLabel}
   *      method).
   */

  void visitLineNumber (int line, Label start);
}
