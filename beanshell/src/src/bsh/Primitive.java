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

import java.util.Hashtable;

/**
 * <p>Wrapper for primitive types in Bsh.  This is package public because it 
 * is used in the implementation of some bsh commands.</p>
 *
 * <p>See the note in LHS.java about wrapping objects.</p>
 * 
 * @author Pat Niemeyer
 * @author Daniel Leuck
 */
public final class Primitive implements ParserConstants, java.io.Serializable
{
	/*
	Note: this class is final because we may test == Primitive.class in places.
	If we need to change that search for those tests.
	*/	
	
	/*
	static Hashtable primitiveToWrapper = new Hashtable();
	static Hashtable wrapperToPrimitive = new Hashtable();
	static {
		primitiveToWrapper.put( Boolean.TYPE, Boolean.class );
		primitiveToWrapper.put( Byte.TYPE, Byte.class );
		primitiveToWrapper.put( Short.TYPE, Short.class );
		primitiveToWrapper.put( Character.TYPE, Character.class );
		primitiveToWrapper.put( Integer.TYPE, Integer.class );
		primitiveToWrapper.put( Long.TYPE, Long.class );
		primitiveToWrapper.put( Float.TYPE, Float.class );
		primitiveToWrapper.put( Double.TYPE, Double.class );
		wrapperToPrimitive.put( Boolean.class, Boolean.TYPE );
		wrapperToPrimitive.put( Byte.class, Byte.TYPE );
		wrapperToPrimitive.put( Short.class, Short.TYPE );
		wrapperToPrimitive.put( Character.class, Character.TYPE );
		wrapperToPrimitive.put( Integer.class, Integer.TYPE );
		wrapperToPrimitive.put( Long.class, Long.TYPE );
		wrapperToPrimitive.put( Float.class, Float.TYPE );
		wrapperToPrimitive.put( Double.class, Double.TYPE );
	}
	*/
	static Hashtable wrapperMap = new Hashtable();
	static {
		wrapperMap.put( Boolean.TYPE, Boolean.class );
		wrapperMap.put( Byte.TYPE, Byte.class );
		wrapperMap.put( Short.TYPE, Short.class );
		wrapperMap.put( Character.TYPE, Character.class );
		wrapperMap.put( Integer.TYPE, Integer.class );
		wrapperMap.put( Long.TYPE, Long.class );
		wrapperMap.put( Float.TYPE, Float.class );
		wrapperMap.put( Double.TYPE, Double.class );
		wrapperMap.put( Boolean.class, Boolean.TYPE );
		wrapperMap.put( Byte.class, Byte.TYPE );
		wrapperMap.put( Short.class, Short.TYPE );
		wrapperMap.put( Character.class, Character.TYPE );
		wrapperMap.put( Integer.class, Integer.TYPE );
		wrapperMap.put( Long.class, Long.TYPE );
		wrapperMap.put( Float.class, Float.TYPE );
		wrapperMap.put( Double.class, Double.TYPE );
	}

    /** The primitive value stored in its java.lang wrapper class */
    private Object value;

    private static class Special implements java.io.Serializable
    {
        private Special() { }

        public static final Special NULL_VALUE = new Special();
        public static final Special VOID_TYPE = new Special();
    }

    /*
        NULL means "no value".
        This ia a placeholder for primitive null value.
    */
    public static final Primitive NULL = new Primitive(Special.NULL_VALUE);
    
	public static Primitive TRUE = new Primitive(true);	
	public static Primitive FALSE = new Primitive(false);

    /**
        VOID means "no type".
        Strictly speaking, this makes no sense here.  But for practical
        reasons we'll consider the lack of a type to be a special value.
    */
    public static final Primitive VOID = new Primitive(Special.VOID_TYPE);

    // private to prevent invocation with param that isn't a primitive-wrapper
    public Primitive( Object value )
    {
        if ( value == null )
            throw new InterpreterError(
				"Use Primitve.NULL instead of Primitive(null)");

		if ( value != Special.NULL_VALUE 
			&& value != Special.VOID_TYPE &&
			!isWrapperType( value.getClass() ) 
		)
            throw new InterpreterError( "Not a wrapper type: "+value.getClass());

        this.value = value;
    }

    public Primitive(boolean value) { this(value ? Boolean.TRUE : Boolean.FALSE); }
    public Primitive(byte value) { this(new Byte(value)); }
    public Primitive(short value) { this(new Short(value)); }
    public Primitive(char value) { this(new Character(value)); }
    public Primitive(int value) { this(new Integer(value)); }
    public Primitive(long value) { this(new Long(value)); }
    public Primitive(float value) { this(new Float(value)); }
    public Primitive(double value) { this(new Double(value)); }

	/**
    	Return the primitive value stored in its java.lang wrapper class
	*/
    public Object getValue()
    {
        if ( value == Special.NULL_VALUE )
            return null;
        else 
		if ( value == Special.VOID_TYPE )
                throw new InterpreterError("attempt to unwrap void type");
        else
            return value;
    }

    public String toString()
    {
        if(value == Special.NULL_VALUE)
            return "null";
        else if(value == Special.VOID_TYPE)
            return "void";
        else
            return value.toString();
    }

	/**
		Get the corresponding Java primitive TYPE class for this Primitive.
		@return the primitive TYPE class type of the value or Void.TYPE for
		Primitive.VOID or null value for type of Primitive.NULL
	*/
    public Class getType()
    {
		if ( this == Primitive.VOID )
			return Void.TYPE;

		// NULL return null as type... we currently use null type to indicate 
		// loose typing throughout bsh.
		if ( this == Primitive.NULL )
			return null;

		return unboxType( value.getClass() );
    }

	/**
		Perform a binary operation on two Primitives or wrapper types.
		If both original args were Primitives return a Primitive result
		else it was mixed (wrapper/primitive) return the wrapper type.
		The exception is for boolean operations where we will return the 
		primitive type either way.
	*/
    public static Object binaryOperation(
		Object obj1, Object obj2, int kind)
        throws UtilEvalError
    {
		// special primitive types
        if ( obj1 == NULL || obj2 == NULL )
            throw new UtilEvalError(
				"Null value or 'null' literal in binary operation");
        if ( obj1 == VOID || obj2 == VOID )
            throw new UtilEvalError(
			"Undefined variable, class, or 'void' literal in binary operation");

		// keep track of the original types
		Class lhsOrgType = obj1.getClass();
		Class rhsOrgType = obj2.getClass();

		// Unwrap primitives
        if ( obj1 instanceof Primitive )
            obj1 = ((Primitive)obj1).getValue();
        if ( obj2 instanceof Primitive )
            obj2 = ((Primitive)obj2).getValue();

        Object[] operands = promotePrimitives(obj1, obj2);
        Object lhs = operands[0];
        Object rhs = operands[1];

        if(lhs.getClass() != rhs.getClass())
            throw new UtilEvalError("Type mismatch in operator.  " 
			+ lhs.getClass() + " cannot be used with " + rhs.getClass() );

		Object result;
		try {
			result = binaryOperationImpl( lhs, rhs, kind );
		} catch ( ArithmeticException e ) {
			throw new UtilTargetError( "Arithemetic Exception in binary op", e);
		}

		
		if(result instanceof Boolean)
			return ((Boolean)result).booleanValue() ? Primitive.TRUE :
				Primitive.FALSE;
		// If both original args were Primitives return a Primitive result
		// else it was mixed (wrapper/primitive) return the wrapper type
		// Exception is for boolean result, return the primitive
		else if ((lhsOrgType == Primitive.class && rhsOrgType == Primitive.class))
			return new Primitive( result );
		else
			return result;
    }

    static Object binaryOperationImpl( Object lhs, Object rhs, int kind )
        throws UtilEvalError
	{
        if(lhs instanceof Boolean)
            return booleanBinaryOperation((Boolean)lhs, (Boolean)rhs, kind);
        else if(lhs instanceof Integer)
            return intBinaryOperation( (Integer)lhs, (Integer)rhs, kind );
        else if(lhs instanceof Long)
            return longBinaryOperation((Long)lhs, (Long)rhs, kind);
        else if(lhs instanceof Float)
            return floatBinaryOperation((Float)lhs, (Float)rhs, kind);
        else if(lhs instanceof Double)
            return doubleBinaryOperation( (Double)lhs, (Double)rhs, kind);
        else
            throw new UtilEvalError("Invalid types in binary operator" );
	}

    static Boolean booleanBinaryOperation(Boolean B1, Boolean B2, int kind)
    {
        boolean lhs = B1.booleanValue();
        boolean rhs = B2.booleanValue();

        switch(kind)
        {
            case EQ:
                return lhs == rhs ? Boolean.TRUE : Boolean.FALSE;

            case NE:
                return lhs != rhs ? Boolean.TRUE : Boolean.FALSE;

            case BOOL_OR:
            case BOOL_ORX:
                return lhs || rhs ? Boolean.TRUE : Boolean.FALSE;

            case BOOL_AND:
            case BOOL_ANDX:
                return lhs && rhs ? Boolean.TRUE : Boolean.FALSE;

            default:
                throw new InterpreterError("unimplemented binary operator");
        }
    }

    // returns Object covering both Long and Boolean return types
    static Object longBinaryOperation(Long L1, Long L2, int kind)
    {
        long lhs = L1.longValue();
        long rhs = L2.longValue();

        switch(kind)
        {
            // boolean
            case LT:
            case LTX:
                return lhs < rhs ? Boolean.TRUE : Boolean.FALSE;

            case GT:
            case GTX:
                return lhs > rhs ? Boolean.TRUE : Boolean.FALSE;

            case EQ:
                return lhs == rhs ? Boolean.TRUE : Boolean.FALSE;

            case LE:
            case LEX:
                return lhs <= rhs ? Boolean.TRUE : Boolean.FALSE;

            case GE:
            case GEX:
                return lhs >= rhs ? Boolean.TRUE : Boolean.FALSE;

            case NE:
                return lhs != rhs ? Boolean.TRUE : Boolean.FALSE;

            // arithmetic
            case PLUS:
                return new Long(lhs + rhs);

            case MINUS:
                return new Long(lhs - rhs);

            case STAR:
                return new Long(lhs * rhs);

            case SLASH:
                return new Long(lhs / rhs);

            case MOD:
                return new Long(lhs % rhs);

            // bitwise
            case LSHIFT:
            case LSHIFTX:
                return new Long(lhs << rhs);

            case RSIGNEDSHIFT:
            case RSIGNEDSHIFTX:
                return new Long(lhs >> rhs);

            case RUNSIGNEDSHIFT:
            case RUNSIGNEDSHIFTX:
                return new Long(lhs >>> rhs);

            case BIT_AND:
            case BIT_ANDX:
                return new Long(lhs & rhs);

            case BIT_OR:
            case BIT_ORX:
                return new Long(lhs | rhs);

            case XOR:
                return new Long(lhs ^ rhs);

            default:
                throw new InterpreterError(
					"Unimplemented binary long operator");
        }
    }

    // returns Object covering both Integer and Boolean return types
    static Object intBinaryOperation(Integer I1, Integer I2, int kind)
    {
        int lhs = I1.intValue();
        int rhs = I2.intValue();

        switch(kind)
        {
            // boolean
            case LT:
            case LTX:
                return lhs < rhs ? Boolean.TRUE : Boolean.FALSE;

            case GT:
            case GTX:
                return lhs > rhs ? Boolean.TRUE : Boolean.FALSE;

            case EQ:
                return lhs == rhs ? Boolean.TRUE : Boolean.FALSE;

            case LE:
            case LEX:
                return lhs <= rhs ? Boolean.TRUE : Boolean.FALSE;

            case GE:
            case GEX:
                return lhs >= rhs ? Boolean.TRUE : Boolean.FALSE;

            case NE:
                return lhs != rhs ? Boolean.TRUE : Boolean.FALSE;

            // arithmetic
            case PLUS:
                return new Integer(lhs + rhs);

            case MINUS:
                return new Integer(lhs - rhs);

            case STAR:
                return new Integer(lhs * rhs);

            case SLASH:
                return new Integer(lhs / rhs);

            case MOD:
                return new Integer(lhs % rhs);

            // bitwise
            case LSHIFT:
            case LSHIFTX:
                return new Integer(lhs << rhs);

            case RSIGNEDSHIFT:
            case RSIGNEDSHIFTX:
                return new Integer(lhs >> rhs);

            case RUNSIGNEDSHIFT:
            case RUNSIGNEDSHIFTX:
                return new Integer(lhs >>> rhs);

            case BIT_AND:
            case BIT_ANDX:
                return new Integer(lhs & rhs);

            case BIT_OR:
            case BIT_ORX:
                return new Integer(lhs | rhs);

            case XOR:
                return new Integer(lhs ^ rhs);

            default:
                throw new InterpreterError(
					"Unimplemented binary integer operator");
        }
    }

    // returns Object covering both Double and Boolean return types
    static Object doubleBinaryOperation(Double D1, Double D2, int kind)
        throws UtilEvalError
    {
        double lhs = D1.doubleValue();
        double rhs = D2.doubleValue();

        switch(kind)
        {
            // boolean
            case LT:
            case LTX:
                return lhs < rhs ? Boolean.TRUE : Boolean.FALSE;

            case GT:
            case GTX:
                return lhs > rhs ? Boolean.TRUE : Boolean.FALSE;

            case EQ:
                return lhs == rhs ? Boolean.TRUE : Boolean.FALSE;

            case LE:
            case LEX:
                return lhs <= rhs ? Boolean.TRUE : Boolean.FALSE;

            case GE:
            case GEX:
                return lhs >= rhs ? Boolean.TRUE : Boolean.FALSE;

            case NE:
                return lhs != rhs ? Boolean.TRUE : Boolean.FALSE;

            // arithmetic
            case PLUS:
                return new Double(lhs + rhs);

            case MINUS:
                return new Double(lhs - rhs);

            case STAR:
                return new Double(lhs * rhs);

            case SLASH:
                return new Double(lhs / rhs);

            case MOD:
                return new Double(lhs % rhs);

            // can't shift floating-point values
            case LSHIFT:
            case LSHIFTX:
            case RSIGNEDSHIFT:
            case RSIGNEDSHIFTX:
            case RUNSIGNEDSHIFT:
            case RUNSIGNEDSHIFTX:
                throw new UtilEvalError("Can't shift doubles");

            default:
                throw new InterpreterError(
					"Unimplemented binary double operator");
        }
    }
    // returns Object covering both Long and Boolean return types
    static Object floatBinaryOperation(Float F1, Float F2, int kind)
        throws UtilEvalError
    {
        float lhs = F1.floatValue();
        float rhs = F2.floatValue();

        switch(kind)
        {
            // boolean
            case LT:
            case LTX:
                return lhs < rhs ? Boolean.TRUE : Boolean.FALSE;

            case GT:
            case GTX:
                return lhs > rhs ? Boolean.TRUE : Boolean.FALSE;

            case EQ:
                return lhs == rhs ? Boolean.TRUE : Boolean.FALSE;

            case LE:
            case LEX:
                return lhs <= rhs ? Boolean.TRUE : Boolean.FALSE;

            case GE:
            case GEX:
                return lhs >= rhs ? Boolean.TRUE : Boolean.FALSE;

            case NE:
                return lhs != rhs ? Boolean.TRUE : Boolean.FALSE;

            // arithmetic
            case PLUS:
                return new Float(lhs + rhs);

            case MINUS:
                return new Float(lhs - rhs);

            case STAR:
                return new Float(lhs * rhs);

            case SLASH:
                return new Float(lhs / rhs);

            case MOD:
                return new Float(lhs % rhs);

            // can't shift floats
            case LSHIFT:
            case LSHIFTX:
            case RSIGNEDSHIFT:
            case RSIGNEDSHIFTX:
            case RUNSIGNEDSHIFT:
            case RUNSIGNEDSHIFTX:
                throw new UtilEvalError("Can't shift floats ");

            default:
                throw new InterpreterError(
					"Unimplemented binary float operator");
        }
    }

	/**
		Promote primitive wrapper type to to Integer wrapper type
	*/
    static Object promoteToInteger(Object wrapper )
    {
        if(wrapper instanceof Character)
            return new Integer(((Character)wrapper).charValue());
        else if((wrapper instanceof Byte) || (wrapper instanceof Short))
            return new Integer(((Number)wrapper).intValue());

        return wrapper;
    }

	/**
		Promote the pair of primitives to the maximum type of the two.
		e.g. [int,long]->[long,long]
	*/
    static Object[] promotePrimitives(Object lhs, Object rhs)
    {
        lhs = promoteToInteger(lhs);
        rhs = promoteToInteger(rhs);

        if((lhs instanceof Number) && (rhs instanceof Number))
        {
            Number lnum = (Number)lhs;
            Number rnum = (Number)rhs;

            boolean b;

            if((b = (lnum instanceof Double)) || (rnum instanceof Double))
            {
                if(b)
                    rhs = new Double(rnum.doubleValue());
                else
                    lhs = new Double(lnum.doubleValue());
            }
            else if((b = (lnum instanceof Float)) || (rnum instanceof Float))
            {
                if(b)
                    rhs = new Float(rnum.floatValue());
                else
                    lhs = new Float(lnum.floatValue());
            }
            else if((b = (lnum instanceof Long)) || (rnum instanceof Long))
            {
                if(b)
                    rhs = new Long(rnum.longValue());
                else
                    lhs = new Long(lnum.longValue());
            }
        }

        return new Object[] { lhs, rhs };
    }

    public static Primitive unaryOperation(Primitive val, int kind)
        throws UtilEvalError
    {
        if (val == NULL)
            throw new UtilEvalError(
				"illegal use of null object or 'null' literal");
        if (val == VOID)
            throw new UtilEvalError(
				"illegal use of undefined object or 'void' literal");

        Class operandType = val.getType();
        Object operand = promoteToInteger(val.getValue());

        if ( operand instanceof Boolean )
            return booleanUnaryOperation((Boolean)operand, kind)
            	? Primitive.TRUE : Primitive.FALSE;
        else if(operand instanceof Integer)
        {
            int result = intUnaryOperation((Integer)operand, kind);

            // ++ and -- must be cast back the original type
            if(kind == INCR || kind == DECR)
            {
                if(operandType == Byte.TYPE)
                    return new Primitive((byte)result);
                if(operandType == Short.TYPE)
                    return new Primitive((short)result);
                if(operandType == Character.TYPE)
                    return new Primitive((char)result);
            }

            return new Primitive(result);
        }
        else if(operand instanceof Long)
            return new Primitive(longUnaryOperation((Long)operand, kind));
        else if(operand instanceof Float)
            return new Primitive(floatUnaryOperation((Float)operand, kind));
        else if(operand instanceof Double)
            return new Primitive(doubleUnaryOperation((Double)operand, kind));
        else
            throw new InterpreterError(
				"An error occurred.  Please call technical support.");
    }

    static boolean booleanUnaryOperation(Boolean B, int kind) 
		throws UtilEvalError
    {
        boolean operand = B.booleanValue();
        switch(kind)
        {
            case BANG:
                return !operand;
            default:
                throw new UtilEvalError("Operator inappropriate for boolean");
        }
    }

    static int intUnaryOperation(Integer I, int kind)
    {
        int operand = I.intValue();

        switch(kind)
        {
            case PLUS:
                return operand;
            case MINUS:
                return -operand;
            case TILDE:
                return ~operand;
            case INCR:
                return operand + 1;
            case DECR:
                return operand - 1;
            default:
                throw new InterpreterError("bad integer unaryOperation");
        }
    }

    static long longUnaryOperation(Long L, int kind)
    {
        long operand = L.longValue();

        switch(kind)
        {
            case PLUS:
                return operand;
            case MINUS:
                return -operand;
            case TILDE:
                return ~operand;
            case INCR:
                return operand + 1;
            case DECR:
                return operand - 1;
            default:
                throw new InterpreterError("bad long unaryOperation");
        }
    }

    static float floatUnaryOperation(Float F, int kind)
    {
        float operand = F.floatValue();

        switch(kind)
        {
            case PLUS:
                return operand;
            case MINUS:
                return -operand;
            default:
                throw new InterpreterError("bad float unaryOperation");
        }
    }

    static double doubleUnaryOperation(Double D, int kind)
    {
        double operand = D.doubleValue();

        switch(kind)
        {
            case PLUS:
                return operand;
            case MINUS:
                return -operand;
            default:
                throw new InterpreterError("bad double unaryOperation");
        }
    }

    public int intValue() throws UtilEvalError
    {
        if(value instanceof Number)
            return((Number)value).intValue();
        else
            throw new UtilEvalError("Primitive not a number");
    }

    public boolean booleanValue() throws UtilEvalError
    {
        if(value instanceof Boolean)
            return((Boolean)value).booleanValue();
        else
            throw new UtilEvalError("Primitive not a boolean");
    }

	/**
		Determine if this primitive is a numeric type.
		i.e. not boolean, null, or void (but including char)
	*/
	public boolean isNumber() {
		return ( !(value instanceof Boolean) 
			&& !(this == NULL) && !(this == VOID) );
	}

    public Number numberValue() throws UtilEvalError
    {
		Object value = this.value;

		// Promote character to Number type for these purposes
		if (value instanceof Character)
			value = new Integer(((Character)value).charValue());

        if (value instanceof Number)
            return (Number)value;
        else
            throw new UtilEvalError("Primitive not a number");
    }

	/**
		Primitives compare equal with other Primitives containing an equal
		wrapped value.
	*/
	public boolean equals( Object obj ) 
	{
		if ( obj instanceof Primitive )
			return ((Primitive)obj).value.equals( this.value );
		else
			return false;
	}

	/**
		The hash of the Primitive is tied to the hash of the wrapped value but
		shifted so that they are not the same.
	*/
	public int hashCode() 
	{
		return this.value.hashCode() * 21; // arbitrary
	}

	/**
		Unwrap primitive values and map voids to nulls.
		Non Primitive types remain unchanged.

		@param obj object type which may be bsh.Primitive
		@return corresponding "normal" Java type, "unwrapping" 
			any bsh.Primitive types to their wrapper types.
	*/
	public static Object unwrap( Object obj ) 
	{
        // map voids to nulls for the outside world
        if (obj == Primitive.VOID)
            return null;

        // unwrap primitives
        if (obj instanceof Primitive)
            return((Primitive)obj).getValue();
        else
            return obj;
	}

    /*
        Unwrap Primitive wrappers to their java.lang wrapper values.
		e.g. Primitive(42) becomes Integer(42)
		@see #unwrap( Object )
    */
    public static Object [] unwrap( Object[] args )
    {
		Object [] oa = new Object[ args.length ];
        for(int i=0; i<args.length; i++)
            oa[i] = unwrap( args[i] );
		return oa;
    }

    /*
    */
    public static Object [] wrap( Object[] args, Class [] paramTypes )
    {
		if ( args == null )
			return null;

		Object [] oa = new Object[ args.length ];
        for(int i=0; i<args.length; i++)
            oa[i] = wrap( args[i], paramTypes[i] );
		return oa;
    }

	/**
		Wrap primitive values (as indicated by type param) and nulls in the 
		Primitive class.  Values not primitive or null are left unchanged.
		Primitive values are represented by their wrapped values in param value.
		<p/>
		The value null is mapped to Primitive.NULL.
		Any value specified with type Void.TYPE is mapped to Primitive.VOID.
	*/
    public static Object wrap(
		Object value, Class type )
    {
        if ( type == Void.TYPE )
            return Primitive.VOID;

        if ( value == null )
            return Primitive.NULL;

        if(value instanceof Boolean)
        	return ((Boolean)value).booleanValue() ? Primitive.TRUE :
        		Primitive.FALSE;
        
		if ( type.isPrimitive() && isWrapperType( value.getClass() ) )
			return new Primitive( value );

		return value;
    }


	/**
		Get the appropriate default value per JLS 4.5.4
	*/
	public static Primitive getDefaultValue( Class type )
	{
		if ( type == null || !type.isPrimitive() )
			return Primitive.NULL;
		if ( type == Boolean.TYPE )
			return Primitive.FALSE;

		// non boolean primitive, get appropriate flavor of zero
		try {
			return new Primitive((int)0).castToType( type, Types.CAST );
		} catch ( UtilEvalError e ) {
			throw new InterpreterError( "bad cast" );
		}
	}

	/**
		Get the corresponding java.lang wrapper class for the primitive TYPE
		class.
		e.g.  Integer.TYPE -> Integer.class
	*/
	public static Class boxType( Class primitiveType )
	{
		Class c = (Class)wrapperMap.get( primitiveType );
		if ( c != null )
			return c;
		throw new InterpreterError( 
			"Not a primitive type: "+ primitiveType );
	}

	/**
		Get the corresponding primitive TYPE class for the java.lang wrapper
		class type.
		e.g.  Integer.class -> Integer.TYPE
	*/
	public static Class unboxType( Class wrapperType )
	{
		Class c = (Class)wrapperMap.get( wrapperType );
		if ( c != null )
			return c;
		throw new InterpreterError( 
			"Not a primitive wrapper type: "+wrapperType );
	}

	/**
		Cast this bsh.Primitive value to a new bsh.Primitive value
		This is usually a numeric type cast.  Other cases include:
			A boolean can be cast to boolen
			null can be cast to any object type and remains null
			Attempting to cast a void causes an exception
		@param toType is the java object or primitive TYPE class
	*/
	public Primitive castToType( Class toType, int operation ) 
		throws UtilEvalError
	{
		return castPrimitive( 
			toType, getType()/*fromType*/, this/*fromValue*/, 
			false/*checkOnly*/, operation );
	}

	/*
		Cast or check a cast of a primitive type to another type.
		Normally both types are primitive (e.g. numeric), but a null value
		(no type) may be cast to any type.
		<p/>

		@param toType is the target type of the cast.  It is normally a
		java primitive TYPE, but in the case of a null cast can be any object
		type.

		@param fromType is the java primitive TYPE type of the primitive to be
		cast or null, to indicate that the fromValue was null or void.

		@param fromValue is, optionally, the value to be converted.  If
		checkOnly is true fromValue must be null.  If checkOnly is false,
		fromValue must be non-null (Primitive.NULL is of course valid).
	*/
	static Primitive castPrimitive( 
		Class toType, Class fromType, Primitive fromValue, 
		boolean checkOnly, int operation ) 
		throws UtilEvalError
	{
		/*
			Lots of preconditions checked here...
			Once things are running smoothly we might comment these out
			(That's what assertions are for).
		*/
		if ( checkOnly && fromValue != null )
			throw new InterpreterError("bad cast param 1");
		if ( !checkOnly && fromValue == null )
			throw new InterpreterError("bad cast param 2");
		if ( fromType != null && !fromType.isPrimitive() )
			throw new InterpreterError("bad fromType:" +fromType);
		if ( fromValue == Primitive.NULL && fromType != null )
			throw new InterpreterError("inconsistent args 1");
		if ( fromValue == Primitive.VOID && fromType != Void.TYPE )
			throw new InterpreterError("inconsistent args 2");

		// can't cast void to anything
		if ( fromType == Void.TYPE )
			if ( checkOnly )
				return Types.INVALID_CAST;
			else
				throw Types.castError( Reflect.normalizeClassName(toType), 
					"void value", operation );

		// unwrap Primitive fromValue to its wrapper value, etc.
		Object value = null; 
		if ( fromValue != null )
			value = fromValue.getValue();

		if ( toType.isPrimitive() )
		{
			// Trying to cast null to primitive type?
			if ( fromType == null )
				if ( checkOnly )
					return Types.INVALID_CAST;
				else
					throw Types.castError(
						"primitive type:" + toType, "Null value", operation );

			// fall through
		} else
		{
			// Trying to cast primitive to an object type
			// Primitive.NULL can be cast to any object type
			if ( fromType == null )
				return checkOnly ? Types.VALID_CAST : 
					Primitive.NULL;

			if ( checkOnly )
				return Types.INVALID_CAST;
			else
				throw Types.castError(
						"object type:" + toType, "primitive value", operation);
		}

		// can only cast boolean to boolean
		if ( fromType == Boolean.TYPE )
		{
			if ( toType != Boolean.TYPE )
				if ( checkOnly )
					return Types.INVALID_CAST;
				else
					throw Types.castError( toType, fromType, operation );

			return checkOnly ? Types.VALID_CAST :
				fromValue;
		}

		// Do numeric cast

		// Only allow legal Java assignment unless we're a CAST operation
		if ( operation == Types.ASSIGNMENT 
			&& !Types.isJavaAssignable( toType, fromType ) 
		) {
			if ( checkOnly )
				return Types.INVALID_CAST;
			else
				throw Types.castError( toType, fromType, operation );
		}

		return checkOnly ? Types.VALID_CAST :
			new Primitive( castWrapper(toType, value) );
	}

	public static boolean isWrapperType( Class type )
	{
		return wrapperMap.get( type ) != null && !type.isPrimitive();
	}

	/**
		Cast a primitive value represented by its java.lang wrapper type to the
		specified java.lang wrapper type.  e.g.  Byte(5) to Integer(5) or
		Integer(5) to Byte(5) 
		@param toType is the java TYPE type
		@param value is the value in java.lang wrapper.
		value may not be null.
	*/
	static Object castWrapper( 
		Class toType, Object value ) 
	{
		if ( !toType.isPrimitive() )
			throw new InterpreterError("invalid type in castWrapper: "+toType);
		if ( value == null )
			throw new InterpreterError("null value in castWrapper, guard");
		if ( value instanceof Boolean )
		{
			if ( toType != Boolean.TYPE )
				throw new InterpreterError("bad wrapper cast of boolean");
			else
				return value;
		}

		// first promote char to Number type to avoid duplicating code
		if ( value instanceof Character )
			value = new Integer(((Character)value).charValue());

		if ( !(value instanceof Number) )
			throw new InterpreterError("bad type in cast");

		Number number = (Number)value;

		if (toType == Byte.TYPE)
			return new Byte(number.byteValue());
		if (toType == Short.TYPE)
			return new Short(number.shortValue());
		if (toType == Character.TYPE)
			return new Character((char)number.intValue());
		if (toType == Integer.TYPE)
			return new Integer(number.intValue());
		if (toType == Long.TYPE)
			return new Long(number.longValue());
		if (toType == Float.TYPE)
			return new Float(number.floatValue());
		if (toType == Double.TYPE)
			return new Double(number.doubleValue());

		throw new InterpreterError("error in wrapper cast");
	}

}
