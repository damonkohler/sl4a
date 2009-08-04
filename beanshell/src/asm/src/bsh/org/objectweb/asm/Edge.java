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
 * An edge in the control flow graph of a method body. See {@link Label Label}.
 */

class Edge {

  /**
   * The (relative) stack size in the basic block from which this edge
   * originates. This size is equal to the stack size at the "jump" instruction
   * to which this edge corresponds, relatively to the stack size at the
   * beginning of the originating basic block.
   */

  int stackSize;

  /**
   * The successor block of the basic block from which this edge originates.
   */

  Label successor;

  /**
   * The next edge in the list of successors of the originating basic block.
   * See {@link Label#successors successors}.
   */

  Edge next;

  /**
   * The next available edge in the pool. See {@link CodeWriter#pool pool}.
   */

  Edge poolNext;
}
