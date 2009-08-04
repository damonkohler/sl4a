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

class BSHMethodDeclaration extends SimpleNode
{
	public String name;

	// Begin Child node structure evaluated by insureNodesParsed

	BSHReturnType returnTypeNode;
	BSHFormalParameters paramsNode;
	BSHBlock blockNode;
	// index of the first throws clause child node
	int firstThrowsClause;

	// End Child node structure evaluated by insureNodesParsed

	public Modifiers modifiers;

	// Unsafe caching of type here.
	Class returnType;  // null (none), Void.TYPE, or a Class
	int numThrows = 0;

	BSHMethodDeclaration(int id) { super(id); }

	/**
		Set the returnTypeNode, paramsNode, and blockNode based on child
		node structure.  No evaluation is done here.
	*/
	synchronized void insureNodesParsed() 
	{
		if ( paramsNode != null ) // there is always a paramsNode
			return;

		Object firstNode = jjtGetChild(0);
		firstThrowsClause = 1;
		if ( firstNode instanceof BSHReturnType )
		{
			returnTypeNode = (BSHReturnType)firstNode;
			paramsNode = (BSHFormalParameters)jjtGetChild(1);
			if ( jjtGetNumChildren() > 2+numThrows )
				blockNode = (BSHBlock)jjtGetChild(2+numThrows); // skip throws
			++firstThrowsClause;
		}
		else
		{
			paramsNode = (BSHFormalParameters)jjtGetChild(0);
			blockNode = (BSHBlock)jjtGetChild(1+numThrows); // skip throws
		}
	}

	/**
		Evaluate the return type node.
		@return the type or null indicating loosely typed return
	*/
	Class evalReturnType( CallStack callstack, Interpreter interpreter )
		throws EvalError
	{
		insureNodesParsed();
		if ( returnTypeNode != null )
			return returnTypeNode.evalReturnType( callstack, interpreter );
		else 
			return null;
	}

	String getReturnTypeDescriptor( 
		CallStack callstack, Interpreter interpreter, String defaultPackage )
	{
		insureNodesParsed();
		if ( returnTypeNode == null )
			return null;
		else
			return returnTypeNode.getTypeDescriptor( 
				callstack, interpreter, defaultPackage );
	}

	BSHReturnType getReturnTypeNode() {
		insureNodesParsed();
		return returnTypeNode;
	}

	/**
		Evaluate the declaration of the method.  That is, determine the
		structure of the method and install it into the caller's namespace.
	*/
	public Object eval( CallStack callstack, Interpreter interpreter )
		throws EvalError
	{
		returnType = evalReturnType( callstack, interpreter );
		evalNodes( callstack, interpreter );

		// Install an *instance* of this method in the namespace.
		// See notes in BshMethod 

// This is not good...
// need a way to update eval without re-installing...
// so that we can re-eval params, etc. when classloader changes
// look into this

		NameSpace namespace = callstack.top();
		BshMethod bshMethod = new BshMethod( this, namespace, modifiers );
		try {
			namespace.setMethod( name, bshMethod );
		} catch ( UtilEvalError e ) {
			throw e.toEvalError(this,callstack);
		}

		return Primitive.VOID;
	}

	private void evalNodes( CallStack callstack, Interpreter interpreter ) 
		throws EvalError
	{
		insureNodesParsed();
		
		// validate that the throws names are class names
		for(int i=firstThrowsClause; i<numThrows+firstThrowsClause; i++)
			((BSHAmbiguousName)jjtGetChild(i)).toClass( 
				callstack, interpreter );

		paramsNode.eval( callstack, interpreter );

		// if strictJava mode, check for loose parameters and return type
		if ( interpreter.getStrictJava() )
		{
			for(int i=0; i<paramsNode.paramTypes.length; i++)
				if ( paramsNode.paramTypes[i] == null )
					// Warning: Null callstack here.  Don't think we need
					// a stack trace to indicate how we sourced the method.
					throw new EvalError(
				"(Strict Java Mode) Undeclared argument type, parameter: " +
					paramsNode.getParamNames()[i] + " in method: " 
					+ name, this, null );

			if ( returnType == null )
				// Warning: Null callstack here.  Don't think we need
				// a stack trace to indicate how we sourced the method.
				throw new EvalError(
				"(Strict Java Mode) Undeclared return type for method: "
					+ name, this, null );
		}
	}

	public String toString() {
		return "MethodDeclaration: "+name;
	}
}
