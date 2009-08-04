/*****************************************************************************
 *                                                                           *
 *  This file is part of the BeanShell Java Scripting distribution.          *
 *  Documentation and updates may be found at http://www.beanshell.org/      *
 *                                                                           *
 *  Sun Public License Notice:                                               *
 *                                                                           *
 *  The contents of this file are subject to the Sun Public License Version  *
 *  1.0 (the "License"); you may not use this file except in compliance with *
 *  the License. A copy of the License is available at http://www.sun.com    * 
 *                                                                           *
 *  The Original Code is BeanShell. The Initial Developer of the Original    *
 *  Code is Pat Niemeyer. Portions created by Pat Niemeyer are Copyright     *
 *  (C) 2000.  All Rights Reserved.                                          *
 *                                                                           *
 *  GNU Public License Notice:                                               *
 *                                                                           *
 *  Alternatively, the contents of this file may be used under the terms of  *
 *  the GNU Lesser General Public License (the "LGPL"), in which case the    *
 *  provisions of LGPL are applicable instead of those above. If you wish to *
 *  allow use of your version of this file only under the  terms of the LGPL *
 *  and not to allow others to use your version of this file under the SPL,  *
 *  indicate your decision by deleting the provisions above and replace      *
 *  them with the notice and other provisions required by the LGPL.  If you  *
 *  do not delete the provisions above, a recipient may use your version of  *
 *  this file under either the SPL or the LGPL.                              *
 *                                                                           *
 *  Patrick Niemeyer (pat@pat.net)                                           *
 *  Author of Learning Java, O'Reilly & Associates                           *
 *  http://www.pat.net/~pat/                                                 *
 *                                                                           *
 *****************************************************************************/

package bsh;

import java.util.Vector;

/**
	A stack of NameSpaces representing the call path.
	Each method invocation, for example, pushes a new NameSpace onto the stack.
	The top of the stack is always the current namespace of evaluation.
	<p>

	This is used to support the this.caller magic reference and to print
	script "stack traces" when evaluation errors occur.
	<p>

	Note: it would be awefully nice to use the java.util.Stack here.
	Sigh... have to stay 1.1 compatible.
	<p>

	Note: How can this be thread safe, you might ask?  Wouldn't a thread 
	executing various beanshell methods be mutating the callstack?  Don't we 
	need one CallStack per Thread in the interpreter?  The answer is that we do.
	Any java.lang.Thread enters our script via an external (hard) Java 
	reference via a This type interface, e.g.  the Runnable interface 
	implemented by This or an arbitrary interface implemented by XThis.  
	In that case the This invokeMethod() method (called by any interface that 
	it exposes) creates a new CallStack for each external call.
	<p>
*/
public class CallStack 
{
	private Vector stack = new Vector(2);

	public CallStack() { }

	public CallStack( NameSpace namespace ) { 
		push( namespace );
	}

	public void clear() {
		stack.removeAllElements();
	}

	public void push( NameSpace ns ) {
		stack.insertElementAt( ns, 0 );
	}

	public NameSpace top() {
		return get(0);
	}

	/**
		zero based.
	*/
	public NameSpace get(int depth) {
		if ( depth >= depth() )
			return NameSpace.JAVACODE;
		else
			return (NameSpace)(stack.elementAt(depth));
	}
	
	/**
		This is kind of crazy, but used by the setNameSpace command.
		zero based.
	*/
	public void set(int depth, NameSpace ns) {
		stack.setElementAt(ns, depth );
	}

	public NameSpace pop() {
		if ( depth() < 1 )
			throw new InterpreterError("pop on empty CallStack");
		NameSpace top = top();
		stack.removeElementAt(0);
		return top;
	}

	/**
		Swap in the value as the new top of the stack and return the old
		value.
	*/
	public NameSpace swap( NameSpace newTop ) {
		NameSpace oldTop = (NameSpace)(stack.elementAt(0));
		stack.setElementAt( newTop, 0 );
		return oldTop;
	}

	public int depth() {
		return stack.size();
	}

	public NameSpace [] toArray() {
		NameSpace [] nsa = new NameSpace [ depth() ];
		stack.copyInto( nsa );
		return nsa;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("CallStack:\n");
		NameSpace [] nsa = toArray();
		for(int i=0; i<nsa.length; i++)
			sb.append("\t"+nsa[i]+"\n");

		return sb.toString();
	}

	/**
		Occasionally we need to freeze the callstack for error reporting
		purposes, etc.
	*/
	public CallStack copy() {
		CallStack cs = new CallStack();
		cs.stack = (Vector)this.stack.clone();
		return cs;
	}
}
