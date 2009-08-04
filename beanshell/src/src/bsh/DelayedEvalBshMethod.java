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

public class DelayedEvalBshMethod extends BshMethod
{
	String returnTypeDescriptor;
	BSHReturnType returnTypeNode;
	String [] paramTypeDescriptors;
	BSHFormalParameters paramTypesNode;

	// used for the delayed evaluation...
	transient CallStack callstack;
	transient Interpreter interpreter;

	/**
		This constructor is used in class generation.  It supplies String type
		descriptors for return and parameter class types and allows delay of 
		the evaluation of those types until they are requested.  It does this
		by holding BSHType nodes, as well as an evaluation callstack, and
		interpreter which are called when the class types are requested. 
	*/
	/*
		Note: technically I think we could get by passing in only the
		current namespace or perhaps BshClassManager here instead of 
		CallStack and Interpreter.  However let's just play it safe in case
		of future changes - anywhere you eval a node you need these.
	*/
	DelayedEvalBshMethod( 
		String name, 
		String returnTypeDescriptor, BSHReturnType returnTypeNode,
		String [] paramNames,
		String [] paramTypeDescriptors, BSHFormalParameters paramTypesNode,
		BSHBlock methodBody, 
		NameSpace declaringNameSpace, Modifiers modifiers,
		CallStack callstack, Interpreter interpreter
	) {
		super( name, null/*returnType*/, paramNames, null/*paramTypes*/,
			methodBody, declaringNameSpace, modifiers );

		this.returnTypeDescriptor = returnTypeDescriptor;
		this.returnTypeNode = returnTypeNode;
		this.paramTypeDescriptors = paramTypeDescriptors;
		this.paramTypesNode = paramTypesNode;
		this.callstack = callstack;
		this.interpreter = interpreter;
	}

	public String getReturnTypeDescriptor() { return returnTypeDescriptor; }

	public Class getReturnType() 
	{ 
		if ( returnTypeNode == null )
			return null;

		// BSHType will cache the type for us
		try {
			return returnTypeNode.evalReturnType( callstack, interpreter );
		} catch ( EvalError e ) {
			throw new InterpreterError("can't eval return type: "+e);
		}
	}

	public String [] getParamTypeDescriptors() { return paramTypeDescriptors; }

	public Class [] getParameterTypes() 
	{ 
		// BSHFormalParameters will cache the type for us
		try {
			return (Class [])paramTypesNode.eval( callstack, interpreter );
		} catch ( EvalError e ) {
			throw new InterpreterError("can't eval param types: "+e);
		}
	}
}
