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

class BSHSwitchStatement 
	extends SimpleNode 
	implements ParserConstants 
{

	public BSHSwitchStatement(int id) { super(id); }

    public Object eval( CallStack callstack, Interpreter interpreter )
		throws EvalError
	{
		int numchild = jjtGetNumChildren();
		int child = 0;
		SimpleNode switchExp = ((SimpleNode)jjtGetChild(child++));
		Object switchVal = switchExp.eval( callstack, interpreter );

		/*
			Note: this could be made clearer by adding an inner class for the
			cases and an object context for the child traversal.
		*/
		// first label
		BSHSwitchLabel label;
		Object node;
		ReturnControl returnControl=null;

		// get the first label
		if ( child >= numchild )
			throw new EvalError("Empty switch statement.", this, callstack );
		label = ((BSHSwitchLabel)jjtGetChild(child++));

		// while more labels or blocks and haven't hit return control
		while ( child < numchild && returnControl == null ) 
		{
			// if label is default or equals switchVal
			if ( label.isDefault 
				|| primitiveEquals( 
					switchVal, label.eval( callstack, interpreter ), 
					callstack, switchExp )
				)
			{
				// execute nodes, skipping labels, until a break or return
				while ( child < numchild ) 
				{
					node = jjtGetChild(child++);
					if ( node instanceof BSHSwitchLabel )
						continue;
					// eval it
					Object value = 
						((SimpleNode)node).eval( callstack, interpreter ); 

					// should check to disallow continue here?
					if ( value instanceof ReturnControl ) {
						returnControl = (ReturnControl)value;
						break;
					}
				}
			} else 
			{
				// skip nodes until next label
				while ( child < numchild ) 
				{
					node = jjtGetChild(child++);
					if ( node instanceof BSHSwitchLabel ) {
						label = (BSHSwitchLabel)node;
						break;
					}
				}
			}
		}

		if ( returnControl != null && returnControl.kind == RETURN )
			return returnControl;
		else
			return Primitive.VOID;
	}

	/**
		Helper method for testing equals on two primitive or boxable objects.
		yuck: factor this out into Primitive.java
	*/
	private boolean primitiveEquals( 
		Object switchVal, Object targetVal, 
		CallStack callstack, SimpleNode switchExp  ) 
		throws EvalError
	{
		if ( switchVal instanceof Primitive || targetVal instanceof Primitive )
			try {
				// binaryOperation can return Primitive or wrapper type 
				Object result = Primitive.binaryOperation( 
					switchVal, targetVal, ParserConstants.EQ );
				result = Primitive.unwrap( result ); 
				return result.equals( Boolean.TRUE ); 
			} catch ( UtilEvalError e ) {
				throw e.toEvalError(
					"Switch value: "+switchExp.getText()+": ", 
					this, callstack );
			}
		else
			return switchVal.equals( targetVal );
	}
}

