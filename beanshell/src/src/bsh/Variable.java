package bsh;

public class Variable implements java.io.Serializable 
{
	static final int DECLARATION=0, ASSIGNMENT=1;
	/** A null type means an untyped variable */
	String name;
	Class type = null;
	String typeDescriptor;
	Object value;
	Modifiers modifiers;
	LHS lhs;

	Variable( String name, Class type, LHS lhs ) 
	{
		this.name = name;
		this.lhs = lhs;
		this.type = type;
	}
	
	Variable( String name, Object value, Modifiers modifiers )
		throws UtilEvalError
	{
		this( name, (Class)null/*type*/, value, modifiers );
	}

	/**
		This constructor is used in class generation.
	*/
	Variable( 
		String name, String typeDescriptor, Object value, Modifiers modifiers 
	)
		throws UtilEvalError
	{
		this( name, (Class)null/*type*/, value, modifiers );
		this.typeDescriptor = typeDescriptor;
	}

	/**
		@param value may be null if this 
	*/
	Variable( String name, Class type, Object value, Modifiers modifiers )
		throws UtilEvalError
	{

		this.name=name;
		this.type =	type;
		this.modifiers = modifiers;
		setValue( value, DECLARATION );
	}

	/**
		Set the value of the typed variable.
		@param value should be an object or wrapped bsh Primitive type.
		if value is null the appropriate default value will be set for the
		type: e.g. false for boolean, zero for integer types.
	*/
	public void setValue( Object value, int context ) 
		throws UtilEvalError
	{

		// check this.value
		if ( hasModifier("final") && this.value != null )
			throw new UtilEvalError ("Final variable, can't re-assign.");

		if ( value == null )
			value = Primitive.getDefaultValue( type );

		if ( lhs != null )
		{
			lhs.assign( Primitive.unwrap(value), false/*strictjava*/ );
			return;
		}

		// TODO: should add isJavaCastable() test for strictJava
		// (as opposed to isJavaAssignable())
		if ( type != null )
			value = Types.castObject( value, type, 
				context == DECLARATION ? Types.CAST : Types.ASSIGNMENT
			);

		this.value= value;
	}

	/*
		Note: UtilEvalError here comes from lhs.getValue().
		A Variable can represent an LHS for the case of an imported class or
		object field.
	*/
	Object getValue() 
		throws UtilEvalError
	{ 
		if ( lhs != null )
			return type == null ?
				lhs.getValue() : Primitive.wrap( lhs.getValue(), type );

		return value; 
	}

	/** A type of null means loosely typed variable */
	public Class getType() { return type;	}

	public String getTypeDescriptor() { return typeDescriptor; }

	public Modifiers getModifiers() { return modifiers; }

	public String getName() { return name; }

	public boolean hasModifier( String name ) {
		return modifiers != null && modifiers.hasModifier(name);
	}

	public String toString() { 
		return "Variable: "+super.toString()+" "+name+", type:"+type
			+", value:"+value +", lhs = "+lhs;
	}
}
