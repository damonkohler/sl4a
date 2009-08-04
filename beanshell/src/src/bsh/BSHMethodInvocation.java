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

import java.lang.reflect.InvocationTargetException;

class BSHMethodInvocation extends SimpleNode
{
	BSHMethodInvocation (int id) { super(id); }

	BSHAmbiguousName getNameNode() {
		return (BSHAmbiguousName)jjtGetChild(0);
	}

	BSHArguments getArgsNode() {
		return (BSHArguments)jjtGetChild(1);
	}

	/**
		Evaluate the method invocation with the specified callstack and 
		interpreter
	*/
	public Object eval( CallStack callstack, Interpreter interpreter )
		throws EvalError
	{
		NameSpace namespace = callstack.top();
		BSHAmbiguousName nameNode = getNameNode();

		// Do not evaluate methods this() or super() in class instance space
		// (i.e. inside a constructor)
		if ( namespace.getParent() != null && namespace.getParent().isClass
			&& ( nameNode.text.equals("super") || nameNode.text.equals("this") )
		)
			return Primitive.VOID;
 
		Name name = nameNode.getName(namespace);
		Object[] args = getArgsNode().getArguments(callstack, interpreter);

// This try/catch block is replicated is BSHPrimarySuffix... need to
// factor out common functionality...
// Move to Reflect?
		try {
			return name.invokeMethod( interpreter, args, callstack, this);
		} catch ( ReflectError e ) {
			throw new EvalError(
				"Error in method invocation: " + e.getMessage(), 
				this, callstack );
		} catch ( InvocationTargetException e ) 
		{
			String msg = "Method Invocation "+name;
			Throwable te = e.getTargetException();

			/*
				Try to squeltch the native code stack trace if the exception
				was caused by a reflective call back into the bsh interpreter
				(e.g. eval() or source()
			*/
			boolean isNative = true;
			if ( te instanceof EvalError ) 
				if ( te instanceof TargetError )
					isNative = ((TargetError)te).inNativeCode();
				else
					isNative = false;
			
			throw new TargetError( msg, te, this, callstack, isNative );
		} catch ( UtilEvalError e ) {
			throw e.toEvalError( this, callstack );
		}
	}
}

