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
	Static routines supporing type comparison and conversion in BeanShell.

 The following are notes on type comparison and conversion in BeanShell.


*/
class Types 
{
	/*
		Type conversion identifiers.  An ASSIGNMENT allows conversions that would
		normally happen on assignment.  A CAST performs numeric conversions to smaller
		types (as in an explicit Java cast) and things allowed only in variable and array
		declarations (e.g. byte b = 42;)
	*/
	static final int CAST=0, ASSIGNMENT=1;
	
	static final int 
		JAVA_BASE_ASSIGNABLE = 1,
		JAVA_BOX_TYPES_ASSIGABLE = 2,
		JAVA_VARARGS_ASSIGNABLE = 3,
		BSH_ASSIGNABLE = 4;

	static final int
		FIRST_ROUND_ASSIGNABLE = JAVA_BASE_ASSIGNABLE,
		LAST_ROUND_ASSIGNABLE = BSH_ASSIGNABLE;

	/**
		Special value that indicates by identity that the result of a cast
		operation was a valid cast.  This is used by castObject() and
		castPrimitive() in the checkOnly mode of operation.  This value is a
		Primitive type so that it can be returned by castPrimitive.
	*/
	static Primitive VALID_CAST = new Primitive(1);
	static Primitive INVALID_CAST = new Primitive(-1);

	/**
		Get the Java types of the arguments.
	*/
    public static Class[] getTypes( Object[] args )
    {
        if ( args == null )
            return new Class[0];

        Class[] types = new Class[ args.length ];

        for( int i=0; i<args.length; i++ )
        {
			if ( args[i] == null )
				types[i] = null;
            else
			if ( args[i] instanceof Primitive )
                types[i] = ((Primitive)args[i]).getType();
            else
                types[i] = args[i].getClass();
        }

        return types;
    }

	/**
	 Is the 'from' signature (argument types) assignable to the 'to'
	 signature (candidate method types)
	 This method handles the special case of null values in 'to' types
	 indicating a loose type and matching anything.
	 */
	/* Should check for strict java here and limit to isJavaAssignable() */
	static boolean isSignatureAssignable( Class[] from, Class[] to, int round )
	{
		if ( round != JAVA_VARARGS_ASSIGNABLE && from.length != to.length )
			return false;

		switch ( round )
		{
			case JAVA_BASE_ASSIGNABLE:
				for( int i=0; i<from.length; i++ )
					if ( !isJavaBaseAssignable( to[i], from[i] ) )
						return false;
				return true;
			case JAVA_BOX_TYPES_ASSIGABLE:
				for( int i=0; i<from.length; i++ )
					if ( !isJavaBoxTypesAssignable( to[i], from[i] ) )
						return false;
				return true;
			case JAVA_VARARGS_ASSIGNABLE:
				return isSignatureVarargsAssignable( from, to );
			case BSH_ASSIGNABLE:
				for( int i=0; i<from.length; i++ )
					if ( !isBshAssignable( to[i], from[i] ) )
						return false;
				return true;
			default:
				throw new InterpreterError("bad case");
		}
	}

	/**
	 * Are the two signatures exactly equal? This is checked for a special
	 * case in overload resolution.
	 */
	static boolean areSignaturesEqual(Class[] from, Class[] to)
	{
		if (from.length != to.length)
			return false;

		for (int i = 0; i < from.length; i++)
			if (from[i] != to[i])
				return false;

		return true;
	}	
	
	private static boolean isSignatureVarargsAssignable(
		Class[] from, Class[] to )
	{
		return false;
	}

	/**
		Test if a conversion of the rhsType type to the lhsType type is legal via
	 standard Java assignment conversion rules (i.e. without a cast).
	 The rules include Java 5 autoboxing/unboxing.
		<p/>

		For Java primitive TYPE classes this method takes primitive promotion
		into account.  The ordinary Class.isAssignableFrom() does not take
		primitive promotion conversions into account.  Note that Java allows
		additional assignments without a cast in combination with variable
		declarations and array allocations.  Those are handled elsewhere
	 	(maybe should be here with a flag?)
		<p/>
		This class accepts a null rhsType type indicating that the rhsType was the
		value Primitive.NULL and allows it to be assigned to any reference lhsType
		type (non primitive).
		<p/>

		Note that the getAssignableForm() method is the primary bsh method for
		checking assignability.  It adds additional bsh conversions, etc.

		@see #isBshAssignable( Class, Class )
		@param lhsType assigning from rhsType to lhsType
		@param rhsType assigning from rhsType to lhsType
	*/
	static boolean isJavaAssignable( Class lhsType, Class rhsType ) {
		return isJavaBaseAssignable( lhsType, rhsType )
			|| isJavaBoxTypesAssignable( lhsType, rhsType );
	}

	/**
		Is the assignment legal via original Java (up to version 1.4)
		assignment rules, not including auto-boxing/unboxing.
	 @param rhsType may be null to indicate primitive null value
	*/
	static boolean isJavaBaseAssignable( Class lhsType, Class rhsType )
	{
		/*
			Assignment to loose type, defer to bsh extensions
			Note: we could shortcut this here:
			if ( lhsType == null ) return true;
			rather than forcing another round.  It's not strictly a Java issue,
			so does it belong here?
		*/
		if ( lhsType == null )
			return false;

		// null rhs type corresponds to type of Primitive.NULL
		// assignable to any object type
		if ( rhsType == null )
			return !lhsType.isPrimitive();

		if ( lhsType.isPrimitive() && rhsType.isPrimitive() )
		{
			if ( lhsType == rhsType )
				return true;

			// handle primitive widening conversions - JLS 5.1.2
			if ( (rhsType == Byte.TYPE) &&
				(lhsType == Short.TYPE || lhsType == Integer.TYPE
				|| lhsType == Long.TYPE || lhsType == Float.TYPE
				|| lhsType == Double.TYPE))
                    return true;

            if ( (rhsType == Short.TYPE) &&
				(lhsType == Integer.TYPE || lhsType == Long.TYPE ||
                lhsType == Float.TYPE || lhsType == Double.TYPE))
                    return true;

            if ((rhsType == Character.TYPE) &&
				(lhsType == Integer.TYPE || lhsType == Long.TYPE ||
                lhsType == Float.TYPE || lhsType == Double.TYPE))
                    return true;

            if ((rhsType == Integer.TYPE) &&
				(lhsType == Long.TYPE || lhsType == Float.TYPE ||
                lhsType == Double.TYPE))
                    return true;

            if ((rhsType == Long.TYPE) &&
				(lhsType == Float.TYPE || lhsType == Double.TYPE))
                return true;

            if ((rhsType == Float.TYPE) && (lhsType == Double.TYPE))
                return true;
        }
        else
            if ( lhsType.isAssignableFrom(rhsType) )
                return true;

        return false;
    }

	/**
		Determine if the type is assignable via Java boxing/unboxing rules.
	*/
	static boolean isJavaBoxTypesAssignable(
		Class lhsType, Class rhsType )
	{
		// Assignment to loose type... defer to bsh extensions
		if ( lhsType == null )
			return false;

		// prim can be boxed and assigned to Object
		if ( lhsType == Object.class )
			return true;

		// prim numeric type can be boxed and assigned to number
		if ( lhsType == Number.class
			&& rhsType != Character.TYPE
			&& rhsType != Boolean.TYPE
		)
			return true;

		// General case prim type to wrapper or vice versa.
		// I don't know if this is faster than a flat list of 'if's like above.
		// wrapperMap maps both prim to wrapper and wrapper to prim types,
		// so this test is symmetric
		if ( Primitive.wrapperMap.get( lhsType ) == rhsType )
			return true;

		return false;
	}

	/**
	 Test if a type can be converted to another type via BeanShell
	 extended syntax rules (a superset of Java conversion rules).
	 */
	static boolean isBshAssignable( Class toType, Class fromType )
	{
		try {
			return castObject(
				toType, fromType, null/*fromValue*/,
				ASSIGNMENT, true/*checkOnly*/
			) == VALID_CAST;
		} catch ( UtilEvalError e ) {
			// This should not happen with checkOnly true
			throw new InterpreterError("err in cast check: "+e);
		}
	}

	/**
		Attempt to cast an object instance to a new type if possible via
	 BeanShell extended syntax rules.  These rules are always a superset of
	 Java conversion rules.  If you wish to impose context sensitive
	 conversion rules then you must test before calling this method.
	 <p/>

		This method can handle fromValue Primitive types (representing
		primitive casts) as well as fromValue object casts requiring interface
		generation, etc.

		@param toType the class type of the cast result, which may include
		primitive types, e.g. Byte.TYPE

		@param fromValue an Object or bsh.Primitive primitive value (including
			Primitive.NULL or Primitive.VOID )

		@see #isBshAssignable( Class, Class )
	*/
	public static Object castObject(
		Object fromValue, Class toType, int operation )
		throws UtilEvalError
	{
		if ( fromValue == null )
			throw new InterpreterError("null fromValue");

		Class fromType =
			fromValue instanceof Primitive ?
				((Primitive)fromValue).getType()
				: fromValue.getClass();

		return castObject(
			toType, fromType, fromValue, operation, false/*checkonly*/ );
	}

	/**
	 Perform a type conversion or test if a type conversion is possible with
	 respect to BeanShell extended rules.  These rules are always a superset of
	 the Java language rules, so this method can also perform (but not test)
	 any Java language assignment or cast conversion.
	 <p/>

	 This method can perform the functionality of testing if an assignment
	 or cast is ultimately possible (with respect to BeanShell) as well as the
	 functionality of performing the necessary conversion of a value based
	 on the specified target type.  This combined functionality is done for
	 expediency and could be separated later.
	 <p/>

	 Other methods such as isJavaAssignable() should be used to determine the
	 suitability of an assignment in a fine grained or restrictive way based
	 on context before calling this method
	 <p/>

	 A CAST is stronger than an ASSIGNMENT operation in that it will attempt to
	 perform primtive operations that cast to a smaller type. e.g. (byte)myLong;
	 These are used in explicit primitive casts, primitive delclarations and
	 array declarations. I don't believe there are any object conversions which are
	 different between  ASSIGNMENT and CAST (e.g. scripted object to interface proxy
	 in bsh is done on assignment as well as cast).
	 <p/>

	 This method does not obey strictJava(), you must test first before
	 using this method if you care. (See #isJavaAssignable()).
	 <p/>

		@param toType the class type of the cast result, which may include
			primitive types, e.g. Byte.TYPE.  toType may be null to indicate a
			loose type assignment (which matches any fromType).

		@param fromType is the class type of the value to be cast including
			java primitive TYPE classes for primitives.
			If fromValue is (or would be) Primitive.NULL then fromType should be null.

		@param fromValue an Object or bsh.Primitive primitive value (including
			Primitive.NULL or Primitive.VOID )

		@param checkOnly If checkOnly is true then fromValue must be null.
			FromType is checked for the cast to toType...
			If checkOnly is false then fromValue must be non-null
			(Primitive.NULL is ok) and the actual cast is performed.

		@throws UtilEvalError on invalid assignment (when operation is
			assignment ).

		@throws UtilTargetError wrapping ClassCastException on cast error
			(when operation is cast)

		@param operation is Types.CAST or Types.ASSIGNMENT

		@see bsh.Primitive.getType()
	*/
	/*
		Notes: This method is currently responsible for auto-boxing/unboxing
		conversions...  Where does that need to go?
	*/
	private static Object castObject(
		Class toType, Class fromType, Object fromValue,
		int operation, boolean checkOnly )
		throws UtilEvalError
	{
		/*
			Lots of preconditions checked here...
			Once things are running smoothly we might comment these out
			(That's what assertions are for).
		*/
		if ( checkOnly && fromValue != null )
			throw new InterpreterError("bad cast params 1");
		if ( !checkOnly && fromValue == null )
			throw new InterpreterError("bad cast params 2");
		if ( fromType == Primitive.class )
			throw new InterpreterError("bad from Type, need to unwrap");
		if ( fromValue == Primitive.NULL && fromType != null )
			throw new InterpreterError("inconsistent args 1");
		if ( fromValue == Primitive.VOID && fromType != Void.TYPE )
			throw new InterpreterError("inconsistent args 2");
		if ( toType == Void.TYPE )
			throw new InterpreterError("loose toType should be null");
		
		// assignment to loose type, void type, or exactly same type
		if ( toType == null || toType == fromType )
			return checkOnly ? VALID_CAST :
				fromValue;

		// Casting to primitive type
        if ( toType.isPrimitive() )
		{
			if ( fromType == Void.TYPE || fromType == null 
				|| fromType.isPrimitive() )
			{
				// Both primitives, do primitive cast
				return Primitive.castPrimitive( 
					toType, fromType, (Primitive)fromValue, 
					checkOnly, operation );
			} else
			{
				if ( Primitive.isWrapperType( fromType ) )
				{
					// wrapper to primitive
					// Convert value to Primitive and check/cast it.

					//Object r = checkOnly ? VALID_CAST :
					Class unboxedFromType = Primitive.unboxType( fromType );
					Primitive primFromValue;
					if ( checkOnly ) 
						primFromValue = null; // must be null in checkOnly
					else
						primFromValue = (Primitive)Primitive.wrap( 
							fromValue, unboxedFromType );

					return Primitive.castPrimitive( 
						toType, unboxedFromType, primFromValue, 
						checkOnly, operation );
				} else
				{
					// Cannot cast from arbitrary object to primitive
					if ( checkOnly )
						return INVALID_CAST;
					else
						throw castError( toType, fromType, operation );
				}
			}
        }

		// Else, casting to reference type

		// Casting from primitive or void (to reference type)
		if ( fromType == Void.TYPE || fromType == null
			|| fromType.isPrimitive() )
		{
			// cast from primitive to wrapper type
			if ( Primitive.isWrapperType( toType )
				&& fromType != Void.TYPE && fromType != null )
			{
				// primitive to wrapper type
				return checkOnly ? VALID_CAST :
					Primitive.castWrapper( 
						Primitive.unboxType(toType), 
						((Primitive)fromValue).getValue() );
			}

			// Primitive (not null or void) to Object.class type
			if ( toType == Object.class 
				&& fromType != Void.TYPE && fromType != null )
			{
				// box it
				return checkOnly ? VALID_CAST :
					((Primitive)fromValue).getValue();
			}

			// Primitive to arbitrary object type. 
			// Allow Primitive.castToType() to handle it as well as cases of 
			// Primitive.NULL and Primitive.VOID
			return Primitive.castPrimitive( 
				toType, fromType, (Primitive)fromValue, checkOnly, operation );
		}

		// If type already assignable no cast necessary
		// We do this last to allow various errors above to be caught.
		// e.g cast Primitive.Void to Object would pass this
		if ( toType.isAssignableFrom( fromType ) )
			return checkOnly ? VALID_CAST : 
				fromValue;

		// Can we use the proxy mechanism to cast a bsh.This to 
		// the correct interface?
		if ( toType.isInterface() 
			&& bsh.This.class.isAssignableFrom( fromType ) 
			&& Capabilities.canGenerateInterfaces() 
		)
			return checkOnly ? VALID_CAST : 
				((bsh.This)fromValue).getInterface( toType );

		// Both numeric wrapper types? 
		// Try numeric style promotion wrapper cast
		if ( Primitive.isWrapperType( toType ) 
			&& Primitive.isWrapperType( fromType ) 
		)
			return checkOnly ? VALID_CAST :
				Primitive.castWrapper( toType, fromValue );
		
		if ( checkOnly )
			return INVALID_CAST;
		else
			throw castError( toType, fromType , operation  );
	}

	/**
		Return a UtilEvalError or UtilTargetError wrapping a ClassCastException
		describing an illegal assignment or illegal cast, respectively.	
	*/
    static UtilEvalError castError( 
		Class lhsType, Class rhsType, int operation   ) 
    {
		return castError( 
			Reflect.normalizeClassName(lhsType),
			Reflect.normalizeClassName(rhsType), operation  );
    }

    static UtilEvalError castError( 
		String lhs, String rhs, int operation   ) 
    {
		if ( operation == ASSIGNMENT )
			return new UtilEvalError (
				"Can't assign " + rhs + " to "+ lhs );

		Exception cce = new ClassCastException(
			"Cannot cast " + rhs + " to " + lhs );
		return new UtilTargetError( cce );
    }

}
