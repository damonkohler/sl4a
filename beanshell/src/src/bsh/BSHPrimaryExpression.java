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

class BSHPrimaryExpression extends SimpleNode
{
	BSHPrimaryExpression(int id) { super(id); }

	/**
		Evaluate to a value object.
	*/
	public Object eval( CallStack callstack, Interpreter interpreter)  
		throws EvalError
	{
		return eval( false, callstack, interpreter );
	}

	/**
		Evaluate to a value object.
	*/
	public LHS toLHS( CallStack callstack, Interpreter interpreter)  
		throws EvalError
	{
		Object obj = eval( true, callstack, interpreter );

		if ( ! (obj instanceof LHS) )
			throw new EvalError("Can't assign to:", this, callstack );
		else
			return (LHS)obj;
	}

	/*
		Our children are a prefix expression and any number of suffixes.
		<p>

		We don't eval() any nodes until the suffixes have had an
		opportunity to work through them.  This lets the suffixes decide
		how to interpret an ambiguous name (e.g. for the .class operation).
	*/
	private Object eval( boolean toLHS, 
		CallStack callstack, Interpreter interpreter)  
		throws EvalError
	{
		Object obj = jjtGetChild(0);
		int numChildren = jjtGetNumChildren(); 

		for(int i=1; i<numChildren; i++)
			obj = ((BSHPrimarySuffix)jjtGetChild(i)).doSuffix(
				obj, toLHS, callstack, interpreter);

		/*
			If the result is a Node eval() it to an object or LHS
			(as determined by toLHS)
		*/
		if ( obj instanceof SimpleNode )
			if ( obj instanceof BSHAmbiguousName )
				if ( toLHS )
					obj = ((BSHAmbiguousName)obj).toLHS(
						callstack, interpreter);
				else
					obj = ((BSHAmbiguousName)obj).toObject(
						callstack, interpreter);
			else 
				// Some arbitrary kind of node
				if ( toLHS )
					// is this right?
					throw new EvalError("Can't assign to prefix.", 
						this, callstack );
				else
					obj = ((SimpleNode)obj).eval(callstack, interpreter);	

		// return LHS or value object as determined by toLHS
		if ( obj instanceof LHS )
			if ( toLHS )
				return obj;
			else
				try {
					return ((LHS)obj).getValue();
				} catch ( UtilEvalError e ) {
					throw e.toEvalError( this, callstack );
				}
		else
			return obj;
	}
}

