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

class BSHAssignment extends SimpleNode implements ParserConstants
{
    public int operator;

    BSHAssignment(int id) { super(id); }

    public Object eval(
		CallStack callstack, Interpreter interpreter) 
		throws EvalError
    {
        BSHPrimaryExpression lhsNode = 
			(BSHPrimaryExpression)jjtGetChild(0);

		if ( lhsNode == null )
			throw new InterpreterError( "Error, null LHSnode" );

		boolean strictJava = interpreter.getStrictJava();
        LHS lhs = lhsNode.toLHS( callstack, interpreter);
        if ( lhs == null )
            throw new InterpreterError( "Error, null LHS" );

		// For operator-assign operations save the lhs value before evaluating
		// the rhs.  This is correct Java behavior for postfix operations
		// e.g. i=1; i+=i++; // should be 2 not 3
		Object lhsValue = null;
		if ( operator != ASSIGN ) // assign doesn't need the pre-value
			try {
				lhsValue = lhs.getValue();
			} catch ( UtilEvalError e ) {
				throw e.toEvalError( this, callstack );
			}

        SimpleNode rhsNode = (SimpleNode)jjtGetChild(1);

        Object rhs;
		
		// implement "blocks" foo = { };
		// if ( rhsNode instanceof BSHBlock )
		//    rsh =
		// else
        rhs = rhsNode.eval(callstack, interpreter);

        if ( rhs == Primitive.VOID )
            throw new EvalError("Void assignment.", this, callstack );

		try {
			switch(operator)
			{
				case ASSIGN:
					return lhs.assign( rhs, strictJava );

				case PLUSASSIGN:
					return lhs.assign( 
						operation(lhsValue, rhs, PLUS), strictJava );

	            case MINUSASSIGN:
					return lhs.assign( 
						operation(lhsValue, rhs, MINUS), strictJava );

				case STARASSIGN:
					return lhs.assign( 
						operation(lhsValue, rhs, STAR), strictJava );

	            case SLASHASSIGN:
					return lhs.assign( 
						operation(lhsValue, rhs, SLASH), strictJava );

	            case ANDASSIGN:
				case ANDASSIGNX:
					return lhs.assign( 
						operation(lhsValue, rhs, BIT_AND), strictJava );

	            case ORASSIGN:
	            case ORASSIGNX:
	                return lhs.assign( 
						operation(lhsValue, rhs, BIT_OR), strictJava );

	            case XORASSIGN:
	                return lhs.assign( 
						operation(lhsValue, rhs, XOR), strictJava );

	            case MODASSIGN:
	                return lhs.assign( 
						operation(lhsValue, rhs, MOD), strictJava );

	            case LSHIFTASSIGN:
	            case LSHIFTASSIGNX:
	                return lhs.assign( 
						operation(lhsValue, rhs, LSHIFT), strictJava );

	            case RSIGNEDSHIFTASSIGN:
	            case RSIGNEDSHIFTASSIGNX:
	                return lhs.assign( 
					operation(lhsValue, rhs, RSIGNEDSHIFT ), strictJava );

	            case RUNSIGNEDSHIFTASSIGN:
	            case RUNSIGNEDSHIFTASSIGNX:
	                return lhs.assign( 
						operation(lhsValue, rhs, RUNSIGNEDSHIFT), 
						strictJava );

				default:
					throw new InterpreterError(
						"unimplemented operator in assignment BSH");
			}
		} catch ( UtilEvalError e ) {
			throw e.toEvalError( this, callstack );
		}
    }

    private Object operation( Object lhs, Object rhs, int kind ) 
		throws UtilEvalError
    {
		/*
			Implement String += value;
			According to the JLS, value may be anything.
			In BeanShell, we'll disallow VOID (undefined) values.
			(or should we map them to the empty string?)
		*/
		if ( lhs instanceof String && rhs != Primitive.VOID ) {
			if ( kind != PLUS )
				throw new UtilEvalError(
					"Use of non + operator with String LHS" );

			return (String)lhs + rhs;
		}

        if ( lhs instanceof Primitive || rhs instanceof Primitive )
            if(lhs == Primitive.VOID || rhs == Primitive.VOID)
                throw new UtilEvalError(
					"Illegal use of undefined object or 'void' literal" );
            else if ( lhs == Primitive.NULL || rhs == Primitive.NULL )
                throw new UtilEvalError(
					"Illegal use of null object or 'null' literal" );


        if( (lhs instanceof Boolean || lhs instanceof Character ||
             lhs instanceof Number || lhs instanceof Primitive) &&
            (rhs instanceof Boolean || rhs instanceof Character ||
             rhs instanceof Number || rhs instanceof Primitive) )
        {
            return Primitive.binaryOperation(lhs, rhs, kind);
        }

        throw new UtilEvalError("Non primitive value in operator: " +
            lhs.getClass() + " " + tokenImage[kind] + " " + rhs.getClass() );
    }
}
