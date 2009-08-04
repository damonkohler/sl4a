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

/**
	Implement binary expressions...
	Note: this is too complicated... need some cleanup and simplification.
	@see Primitive.binaryOperation
*/
class BSHBinaryExpression extends SimpleNode 
	implements ParserConstants 
{
    public int kind;

    BSHBinaryExpression(int id) { super(id); }

    public Object eval( CallStack callstack, Interpreter interpreter)  
		throws EvalError
    {
        Object lhs = ((SimpleNode)jjtGetChild(0)).eval(callstack, interpreter);

		/*
			Doing instanceof?  Next node is a type.
		*/
        if (kind == INSTANCEOF)
        {
			// null object ref is not instance of any type
			if ( lhs == Primitive.NULL )
				return Primitive.FALSE;

            Class rhs = ((BSHType)jjtGetChild(1)).getType( 
				callstack, interpreter );
		/*
			// primitive (number or void) cannot be tested for instanceof
            if (lhs instanceof Primitive)
				throw new EvalError("Cannot be instance of primitive type." );
		*/
			/*
				Primitive (number or void) is not normally an instanceof
				anything.  But for internal use we'll test true for the
				bsh.Primitive class.  
				i.e. (5 instanceof bsh.Primitive) will be true
			*/
			if ( lhs instanceof Primitive )
				if ( rhs == bsh.Primitive.class )
					return Primitive.TRUE;
				else
					return Primitive.FALSE;

			// General case - performe the instanceof based on assignability
            boolean ret = Types.isJavaBaseAssignable( rhs, lhs.getClass() );
            return new Primitive(ret);
        }


		// The following two boolean checks were tacked on.
		// This could probably be smoothed out.

		/*
			Look ahead and short circuit evaluation of the rhs if:
				we're a boolean AND and the lhs is false.
		*/
		if ( kind == BOOL_AND || kind == BOOL_ANDX ) {
			Object obj = lhs;
			if ( isPrimitiveValue(lhs) )
				obj = ((Primitive)lhs).getValue();
			if ( obj instanceof Boolean && 
				( ((Boolean)obj).booleanValue() == false ) )
				return Primitive.FALSE;
		}
		/*
			Look ahead and short circuit evaluation of the rhs if:
				we're a boolean AND and the lhs is false.
		*/
		if ( kind == BOOL_OR || kind == BOOL_ORX ) {
			Object obj = lhs;
			if ( isPrimitiveValue(lhs) )
				obj = ((Primitive)lhs).getValue();
			if ( obj instanceof Boolean && 
				( ((Boolean)obj).booleanValue() == true ) )
				return Primitive.TRUE;
		}

		// end stuff that was tacked on for boolean short-circuiting.

		/*
			Are both the lhs and rhs either wrappers or primitive values?
			do binary op
		*/
		boolean isLhsWrapper = isWrapper( lhs );
        Object rhs = ((SimpleNode)jjtGetChild(1)).eval(callstack, interpreter);
		boolean isRhsWrapper = isWrapper( rhs );
		if ( 
			( isLhsWrapper || isPrimitiveValue( lhs ) )
			&& ( isRhsWrapper || isPrimitiveValue( rhs ) )
		)
        {
			// Special case for EQ on two wrapper objects
			if ( (isLhsWrapper && isRhsWrapper && kind == EQ)) 
			{
				/*  
					Don't auto-unwrap wrappers (preserve identity semantics)
					FALL THROUGH TO OBJECT OPERATIONS BELOW.
				*/
			} else
				try {
					return Primitive.binaryOperation(lhs, rhs, kind);
				} catch ( UtilEvalError e ) {
					throw e.toEvalError( this, callstack  );
				}
        }
	/*
	Doing the following makes it hard to use untyped vars...
	e.g. if ( arg == null ) ...what if arg is a primitive?
	The answer is that we should test only if the var is typed...?
	need to get that info here...

		else
		{
		// Do we have a mixture of primitive values and non-primitives ?  
		// (primitiveValue = not null, not void)

		int primCount = 0;
		if ( isPrimitiveValue( lhs ) )
			++primCount;
		if ( isPrimitiveValue( rhs ) )
			++primCount;

		if ( primCount > 1 )
			// both primitive types, should have been handled above
			throw new InterpreterError("should not be here");
		else 
		if ( primCount == 1 )
			// mixture of one and the other
			throw new EvalError("Operator: '" + tokenImage[kind]
				+"' inappropriate for object / primitive combination.", 
				this, callstack );

		// else fall through to handle both non-primitive types

		// end check for primitive and non-primitive mix 
		}
	*/

		/*
			Treat lhs and rhs as arbitrary objects and do the operation.
			(including NULL and VOID represented by their Primitive types)
		*/
		//System.out.println("binary op arbitrary obj: {"+lhs+"}, {"+rhs+"}");
        switch(kind)
        {
            case EQ:
                return (lhs == rhs) ? Primitive.TRUE : Primitive.FALSE;

            case NE:
                return (lhs != rhs) ? Primitive.TRUE : Primitive.FALSE;

            case PLUS:
                if(lhs instanceof String || rhs instanceof String)
                    return lhs.toString() + rhs.toString();

            // FALL THROUGH TO DEFAULT CASE!!!

            default:
                if(lhs instanceof Primitive || rhs instanceof Primitive)
                    if ( lhs == Primitive.VOID || rhs == Primitive.VOID )
                        throw new EvalError(
				"illegal use of undefined variable, class, or 'void' literal", 
							this, callstack );
                    else 
					if ( lhs == Primitive.NULL || rhs == Primitive.NULL )
                        throw new EvalError(
				"illegal use of null value or 'null' literal", this, callstack);

                throw new EvalError("Operator: '" + tokenImage[kind] +
                    "' inappropriate for objects", this, callstack );
        }
    }

	/*
		object is a non-null and non-void Primitive type
	*/
	private boolean isPrimitiveValue( Object obj ) {
        return ( (obj instanceof Primitive) 
			&& (obj != Primitive.VOID) && (obj != Primitive.NULL) );
	}

	/*
		object is a java.lang wrapper for boolean, char, or number type
	*/
	private boolean isWrapper( Object obj ) {
        return ( obj instanceof Boolean || 
			obj instanceof Character || obj instanceof Number );
	}
}
