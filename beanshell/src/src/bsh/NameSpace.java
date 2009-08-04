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


package	bsh;

import java.util.*;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
    A namespace	in which methods, variables, and imports (class names) live.  
	This is package public because it is used in the implementation of some 
	bsh commands.  However for normal use you should be using methods on 
	bsh.Interpreter to interact with your scripts.
	<p>

	A bsh.This object is a thin layer over a NameSpace that associates it with
	an Interpreter instance.  Together they comprise a Bsh scripted object 
	context.
	<p>

	Note: I'd really like to use collections here, but we have to keep this
	compatible with JDK1.1 
*/
/*
	Thanks to Slava Pestov (of jEdit fame) for import caching enhancements.
	Note: This class has gotten too big.  It should be broken down a bit.
*/
public class NameSpace 
	implements java.io.Serializable, BshClassManager.Listener, 
	NameSource
{
	public static final NameSpace JAVACODE = 
		new NameSpace((BshClassManager)null, "Called from compiled Java code.");
	static {
		JAVACODE.isMethod = true;
	}

	// Begin instance data
	// Note: if we add something here we should reset it in the clear() method.

	/**
		The name of this namespace.  If the namespace is a method body
		namespace then this is the name of the method.  If it's a class or
		class instance then it's the name of the class.
	*/
	private String nsName; 
    private NameSpace parent;
    private Hashtable variables;
    private Hashtable methods;

    protected Hashtable importedClasses;
    private Vector importedPackages;
    private Vector importedCommands;
	private Vector importedObjects;
	private Vector importedStatic;
	private String packageName;

	transient private BshClassManager classManager;

	// See notes in getThis()
    private This thisReference;

	/** Name resolver objects */
    private Hashtable names;

	/** The node associated with the creation of this namespace.
		This is used support getInvocationLine() and getInvocationText(). */
	SimpleNode callerInfoNode;

	/** 
		Note that the namespace is a method body namespace.  This is used for
		printing stack traces in exceptions.  
	*/
	boolean isMethod;

	/**
		Note that the namespace is a class body or class instance namespace.  
		This is used for controlling static/object import precedence, etc.
	*/
	/*
		Note: We can move this class related behavior out to a subclass of 
		NameSpace, but we'll start here.
	*/
	boolean isClass;
	Class classStatic;	
	Object classInstance;

	void setClassStatic( Class clas ) {
		this.classStatic = clas;
		importStatic( clas );
	}
	void setClassInstance( Object instance ) {
		this.classInstance = instance;
		importObject( instance );
	}
	Object getClassInstance()
		throws UtilEvalError
	{
		if ( classInstance != null )
			return classInstance;

		if ( classStatic != null 
			//|| ( getParent()!=null && getParent().classStatic != null ) 
		)
			throw new UtilEvalError(
				"Can't refer to class instance from static context.");
		else
			throw new InterpreterError( 
				"Can't resolve class instance 'this' in: "+this);
	}


	/**
		Local class cache for classes resolved through this namespace using 
		getClass() (taking into account imports).  Only unqualified class names
		are cached here (those which might be imported).  Qualified names are 
		always absolute and are cached by BshClassManager.
	*/
    transient private Hashtable classCache;

	// End instance data

	// Begin constructors

	/**
		@parent the parent namespace of this namespace.  Child namespaces
		inherit all variables and methods of their parent and can (of course)
		override / shadow them.
	*/
    public NameSpace( NameSpace parent, String name ) 
	{
		// Note: in this case parent must have a class manager.
		this( parent, null, name );
	}

    public NameSpace( BshClassManager classManager, String name ) 
	{
		this( null, classManager, name );
	}

    public NameSpace( 
		NameSpace parent, BshClassManager classManager, String name ) 
	{
		// We might want to do this here rather than explicitly in Interpreter
		// for global (see also prune())
		//if ( classManager == null && (parent == null ) )
			// create our own class manager?

		setName(name);
		setParent(parent);
		setClassManager( classManager );

		// Register for notification of classloader change
		if ( classManager != null )
			classManager.addListener(this);
    }

	// End constructors

	public void setName( String name ) {
		this.nsName = name;
	}

	/**
		The name of this namespace.  If the namespace is a method body
		namespace then this is the name of the method.  If it's a class or
		class instance then it's the name of the class.
	*/
	public String getName() {
		return this.nsName;
	}

	/**
		Set the node associated with the creation of this namespace.
		This is used in debugging and to support the getInvocationLine()
		and getInvocationText() methods.
	*/
	void setNode( SimpleNode node ) {
		callerInfoNode = node;
	}

	/**
	*/
	SimpleNode getNode() 
	{
		if ( callerInfoNode != null )
			return callerInfoNode;
		if ( parent != null )
			return parent.getNode();
		else
			return null;
	}

	/**
		Resolve name to an object through this namespace.
	*/
	public Object get( String name, Interpreter interpreter ) 
		throws UtilEvalError 
	{
		CallStack callstack = new CallStack( this );
		return getNameResolver( name ).toObject( callstack, interpreter );
	}

	/**
		Set the variable through this namespace.
		This method obeys the LOCALSCOPING property to determine how variables
		are set.
		<p>
		Note: this method is primarily intended for use internally.  If you use
		this method outside of the bsh package and wish to set variables with
		primitive values you will have to wrap them using bsh.Primitive.
		@see bsh.Primitive
		<p>
		Setting a new variable (which didn't exist before) or removing
		a variable causes a namespace change.

		@param strictJava specifies whether strict java rules are applied.
	*/
    public void	setVariable( String name, Object value, boolean strictJava ) 
		throws UtilEvalError 
	{
		// if localscoping switch follow strictJava, else recurse
		boolean recurse = Interpreter.LOCALSCOPING ? strictJava : true;
		setVariable( name, value, strictJava, recurse );
	}

	/**
		Set a variable explicitly in the local scope.
	*/
    void setLocalVariable( 
		String name, Object value, boolean strictJava ) 
		throws UtilEvalError 
	{
		setVariable( name, value, strictJava, false/*recurse*/ );
	}

	/**
		Set the value of a the variable 'name' through this namespace.
		The variable may be an existing or non-existing variable.
		It may live in this namespace or in a parent namespace if recurse is 
		true.
		<p>
		Note: This method is not public and does *not* know about LOCALSCOPING.
		Its caller methods must set recurse intelligently in all situations 
		(perhaps based on LOCALSCOPING).

		<p>
		Note: this method is primarily intended for use internally.  If you use
		this method outside of the bsh package and wish to set variables with
		primitive values you will have to wrap them using bsh.Primitive.
		@see bsh.Primitive
		<p>
		Setting a new variable (which didn't exist before) or removing
		a variable causes a namespace change.

		@param strictJava specifies whether strict java rules are applied.
		@param recurse determines whether we will search for the variable in
		  our parent's scope before assigning locally.
	*/
    void setVariable( 
		String name, Object value, boolean strictJava, boolean recurse ) 
		throws UtilEvalError 
	{
		if ( variables == null )
			variables =	new Hashtable();

		// primitives should have been wrapped
		if ( value == null )
			throw new InterpreterError("null variable value");

		// Locate the variable definition if it exists.
		Variable existing = getVariableImpl( name, recurse );

		// Found an existing variable here (or above if recurse allowed)
		if ( existing != null )
		{
			try {
				existing.setValue( value, Variable.ASSIGNMENT );
			} catch ( UtilEvalError e ) {
				throw new UtilEvalError(
					"Variable assignment: " + name + ": " + e.getMessage());
			}
		} else 
		// No previous variable definition found here (or above if recurse)
		{
			if ( strictJava )
				throw new UtilEvalError(
					"(Strict Java mode) Assignment to undeclared variable: "
					+name );

			// If recurse, set global untyped var, else set it here.	
			//NameSpace varScope = recurse ? getGlobal() : this;
			// This modification makes default allocation local
			NameSpace varScope = this;

			varScope.variables.put( 
				name, createVariable( name, value, null/*modifiers*/ ) );

			// nameSpaceChanged() on new variable addition
			nameSpaceChanged();
    	}
	}
    
    ////////////////////////////////////////////////////////////////////////////
    
	/**
		<p>
		Sets a variable or property.  See "setVariable" for rules regarding
		scoping.
		</p>
		
		<p>
		We first check for the existence of the variable.  If it exists, we set 
		it.
		If the variable does not exist we look for a property.  If the  property
		exists and is writable we set it.
		Finally, if neither the variable or the property exist, we create a new
		variable.
		</p>
	
		@param strictJava specifies whether strict java rules are applied.
	*/
	public void	setVariableOrProperty( String name, Object value, boolean strictJava ) 
		throws UtilEvalError 
	{
		// if localscoping switch follow strictJava, else recurse
		boolean recurse = Interpreter.LOCALSCOPING ? strictJava : true;
		setVariableOrProperty( name, value, strictJava, recurse );
	}
	
	/**
		Set a variable or property explicitly in the local scope.
		
		<p>
		Sets a variable or property.  See "setLocalVariable" for rules regarding
		scoping.
		</p>
		
		<p>
		We first check for the existence of the variable.  If it exists, we set 
		it.
		If the variable does not exist we look for a property.  If the  property
		exists and is writable we set it.
		Finally, if neither the variable or the property exist, we create a new
		variable.
		</p>		
	*/
	void setLocalVariableOrProperty( 
		String name, Object value, boolean strictJava ) 
		throws UtilEvalError 
	{
		setVariableOrProperty( name, value, strictJava, false/*recurse*/ );
	}
	
	/**
		Set the value of a the variable or property 'name' through this
		namespace.
	
		<p>
		Sets a variable or property.  See "setVariableOrProperty" for rules 
		regarding scope.
		</p>
		
		<p>
		We first check for the existence of the variable.  If it exists, we set 
		it.
		If the variable does not exist we look for a property.  If the  property
		exists and is writable we set it.
		Finally, if neither the variable or the property exist, we create a new
		variable.
		</p>	
	
		@param strictJava specifies whether strict java rules are applied.
		@param recurse determines whether we will search for the variable in
		  our parent's scope before assigning locally.
	*/
	void setVariableOrProperty( 
		String name, Object value, boolean strictJava, boolean recurse ) 
		throws UtilEvalError 
	{
		if ( variables == null )
			variables =	new Hashtable();

		// primitives should have been wrapped
		if ( value == null )
			throw new InterpreterError("null variable value");

		// Locate the variable definition if it exists.
		Variable existing = getVariableImpl( name, recurse );

		// Found an existing variable here (or above if recurse allowed)
		if ( existing != null )
		{
			try {
				existing.setValue( value, Variable.ASSIGNMENT );
			} catch ( UtilEvalError e ) {
				throw new UtilEvalError(
					"Variable assignment: " + name + ": " + e.getMessage());
			}
		} else 
		// No previous variable definition found here (or above if recurse)
		{
			if ( strictJava )
				throw new UtilEvalError(
					"(Strict Java mode) Assignment to undeclared variable: "
					+name );

			boolean setProp = attemptSetPropertyValue(name, value,
					null);
			if(setProp)
				return;
			
			// If recurse, set global untyped var, else set it here.	
			//NameSpace varScope = recurse ? getGlobal() : this;
			// This modification makes default allocation local
			NameSpace varScope = this;

			varScope.variables.put( 
				name, createVariable( name, value, null/*modifiers*/ ) );

			// nameSpaceChanged() on new variable addition
			nameSpaceChanged();
    	}
	}

	protected Variable createVariable(
		String name, Object value, Modifiers mods )
		throws UtilEvalError
	{
		return createVariable( name, null/*type*/, value, mods );
	}

	protected Variable createVariable(
		String name, Class type, Object value, Modifiers mods )
		throws UtilEvalError
	{
		return new Variable( name, type, value, mods );
	}

	protected Variable createVariable(
		String name, Class type, LHS lhs )
		throws UtilEvalError
	{
		return new Variable( name, type, lhs );
	}

	/**
		Remove the variable from the namespace.
	*/
	public void unsetVariable( String name )
	{
		if ( variables != null )
		{
			variables.remove( name );
			nameSpaceChanged();
		}
	}

	/**
		Get the names of variables defined in this namespace.
		(This does not show variables in parent namespaces).
	*/
	public String [] getVariableNames() {
		if ( variables == null )
			return new String [0];
		else
			return enumerationToStringArray( variables.keys() );
	}

	/**
		Get the names of methods declared in this namespace.
		(This does not include methods in parent namespaces).
	*/
	public String [] getMethodNames() 
	{
		if ( methods == null )
			return new String [0];
		else
			return enumerationToStringArray( methods.keys() );
	}

	/**
		Get the methods defined in this namespace.
		(This does not show methods in parent namespaces).
		Note: This will probably be renamed getDeclaredMethods()
	*/
	public BshMethod [] getMethods() 
	{
		if ( methods == null )
			return new BshMethod [0];
		else
			return flattenMethodCollection( methods.elements() );
	}

	private String [] enumerationToStringArray( Enumeration e ) {
		Vector v = new Vector();
		while ( e.hasMoreElements() )
			v.addElement( e.nextElement() );
		String [] sa = new String [ v.size() ];
		v.copyInto( sa );
		return sa;
	}

	/**
		Flatten the vectors of overloaded methods to a single array.
		@see #getMethods()
	*/
    private BshMethod [] flattenMethodCollection( Enumeration e ) {
        Vector v = new Vector();
        while ( e.hasMoreElements() ) {
            Object o = e.nextElement();
            if ( o instanceof BshMethod )
                v.addElement( o );
            else {
                Vector ov = (Vector)o;
                for(int i=0; i<ov.size(); i++)
                    v.addElement( ov.elementAt( i ) );
            }
        }
        BshMethod [] bma = new BshMethod [ v.size() ];
        v.copyInto( bma );
        return bma;
    }

	/**
		Get the parent namespace.
		Note: this isn't quite the same as getSuper().
		getSuper() returns 'this' if we are at the root namespace.
	*/
	public NameSpace getParent() {
		return parent;
	}

	/**
		Get the parent namespace' This reference or this namespace' This
		reference if we are the top.
	*/
    public This getSuper( Interpreter declaringInterpreter )
    {
		if ( parent != null )
			return parent.getThis( declaringInterpreter );
		else
			return getThis( declaringInterpreter );
    }

	/**
		Get the top level namespace or this namespace if we are the top.
		Note: this method should probably return type bsh.This to be consistent
		with getThis();
	*/
    public This getGlobal( Interpreter declaringInterpreter )
    {
		if ( parent != null )
			return parent.getGlobal( declaringInterpreter );
		else
			return getThis( declaringInterpreter );
    }

	
	/**
		A This object is a thin layer over a namespace, comprising a bsh object
		context.  It handles things like the interface types the bsh object
		supports and aspects of method invocation on it.  
		<p>

		The declaringInterpreter is here to support callbacks from Java through
		generated proxies.  The scripted object "remembers" who created it for
		things like printing messages and other per-interpreter phenomenon
		when called externally from Java.
	*/
	/*
		Note: we need a singleton here so that things like 'this == this' work
		(and probably a good idea for speed).

		Caching a single instance here seems technically incorrect,
		considering the declaringInterpreter could be different under some
		circumstances.  (Case: a child interpreter running a source() / eval() 
		command ).  However the effect is just that the main interpreter that
		executes your script should be the one involved in call-backs from Java.

		I do not know if there are corner cases where a child interpreter would
		be the first to use a This reference in a namespace or if that would
		even cause any problems if it did...  We could do some experiments
		to find out... and if necessary we could cache on a per interpreter
		basis if we had weak references...  We might also look at skipping 
		over child interpreters and going to the parent for the declaring 
		interpreter, so we'd be sure to get the top interpreter.
	*/
    public This getThis( Interpreter declaringInterpreter )
	{
		if ( thisReference == null )
			thisReference = This.getThis( this, declaringInterpreter );

		return thisReference;
    }

	public BshClassManager getClassManager() 
	{
		if ( classManager != null )
			return classManager;
		if ( parent != null && parent != JAVACODE )
			return parent.getClassManager();

//System.out.println("experiment: creating class manager");
		classManager = BshClassManager.createClassManager( null/*interp*/ );
		
		//Interpreter.debug("No class manager namespace:" +this);
		return classManager;
	}

	void setClassManager( BshClassManager classManager ) {
		this.classManager = classManager;
	}

	/**
		Used for serialization
	*/
	public void prune() 
	{
		// Cut off from parent, we must have our own class manager.
		// Can't do this in the run() command (needs to resolve stuff)
		// Should we do it by default when we create a namespace will no
		// parent of class manager?

		if ( this.classManager == null )
// XXX if we keep the createClassManager in getClassManager then we can axe
// this?
			setClassManager( 
				BshClassManager.createClassManager( null/*interp*/ ) );

		setParent( null );
	}

	public void setParent( NameSpace parent ) 
	{
		this.parent = parent;

		// If we are disconnected from root we need to handle the def imports
		if ( parent == null )
			loadDefaultImports();
	}
	
	/**
		<p>
		Get the specified variable or property in this namespace or a parent
		namespace.
		</p>
	
		<p>
		We first search for a variable name, and then a property.
		</p>
	
		@return The variable or property value or Primitive.VOID if neither is
			defined.
	*/
	public Object getVariableOrProperty( String name, Interpreter interp ) 
		throws UtilEvalError
	{
		Object val = getVariable( name, true );
		return (val==Primitive.VOID) ? getPropertyValue(name, interp) : val;
	}		

	/**
		Get the specified variable in this namespace or a parent namespace.
		<p>
		Note: this method is primarily intended for use internally.  If you use
		this method outside of the bsh package you will have to use 
		Primitive.unwrap() to get primitive values.
		@see Primitive#unwrap( Object )

		@return The variable value or Primitive.VOID if it is not defined.
	*/
    public Object getVariable( String name ) 
		throws UtilEvalError
	{
		return getVariable( name, true );
	}

	/**
		Get the specified variable in this namespace.
		@param recurse If recurse is true then we recursively search through 
		parent namespaces for the variable.
		<p>
		Note: this method is primarily intended for use internally.  If you use
		this method outside of the bsh package you will have to use 
		Primitive.unwrap() to get primitive values.
		@see Primitive#unwrap( Object )

		@return The variable value or Primitive.VOID if it is not defined.
	*/
    public Object getVariable( String name, boolean recurse ) 
		throws UtilEvalError
	{
		Variable var = getVariableImpl( name, recurse );
		return unwrapVariable( var );
    }

	/**
		Locate a variable and return the Variable object with optional 
		recursion through parent name spaces.
		<p/>
		If this namespace is static, return only static variables.

		@return the Variable value or null if it is not defined
	*/
    protected Variable getVariableImpl( String name, boolean recurse ) 
		throws UtilEvalError
	{
		Variable var = null;

		// Change import precedence if we are a class body/instance
		// Get imported first.
		if ( var == null && isClass )
			var = getImportedVar( name );

		if ( var == null && variables != null )
			var	= (Variable)variables.get(name);

		// Change import precedence if we are a class body/instance
		if ( var == null && !isClass )
			var = getImportedVar( name );

		// try parent
		if ( recurse && (var == null) && (parent != null) )
			var	= parent.getVariableImpl( name, recurse );

		return var;
    }
	
	/*
		Get variables declared in this namespace.
	*/
	public Variable [] getDeclaredVariables() 
	{
		if ( variables == null )
			return new Variable[0];
		Variable [] vars = new Variable [ variables.size() ];
		int i=0;
		for( Enumeration e = variables.elements(); e.hasMoreElements(); )
			vars[i++] = (Variable)e.nextElement();
		return vars;
	}

	/**
		Unwrap a variable to its value.
		@return return the variable value.  A null var is mapped to 
			Primitive.VOID
	*/
	protected Object unwrapVariable( Variable var ) 
		throws UtilEvalError
	{
		return (var == null) ? Primitive.VOID :	var.getValue();
	}

	/**
		@deprecated See #setTypedVariable( String, Class, Object, Modifiers )
	*/
    public void	setTypedVariable(
		String	name, Class type, Object value,	boolean	isFinal )
		throws UtilEvalError 
	{
		Modifiers modifiers = new Modifiers();
		if ( isFinal )
			modifiers.addModifier( Modifiers.FIELD, "final" );
		setTypedVariable( name, type, value, modifiers );
	}

    /**
		Declare a variable in the local scope and set its initial value.
		Value may be null to indicate that we would like the default value 
		for the variable type. (e.g.  0 for integer types, null for object 
		types).  An existing typed variable may only be set to the same type.
		If an untyped variable of the same name exists it will be overridden 
		with the new typed var.
		The set will perform a Types.getAssignableForm() on the value if 
		necessary.

		<p>
		Note: this method is primarily intended for use internally.  If you use
		this method outside of the bsh package and wish to set variables with
		primitive values you will have to wrap them using bsh.Primitive.
		@see bsh.Primitive

		@param value If value is null, you'll get the default value for the type
		@param modifiers may be null
    */
    public void	setTypedVariable(
		String	name, Class type, Object value,	Modifiers modifiers )
		throws UtilEvalError 
	{
		//checkVariableModifiers( name, modifiers );

		if ( variables == null )
			variables =	new Hashtable();

		// Setting a typed variable is always a local operation.
		Variable existing = getVariableImpl( name, false/*recurse*/ );

		// Null value is just a declaration
		// Note: we might want to keep any existing value here instead of reset

		// does the variable already exist?
		if ( existing != null ) 
		{
			// Is it typed?
			if ( existing.getType() != null ) 
			{
				// If it had a different type throw error.
				// This allows declaring the same var again, but not with
				// a different (even if assignable) type.
				if ( existing.getType() != type )
				{
					throw new UtilEvalError( "Typed variable: "+name
						+" was previously declared with type: " 
						+ existing.getType() );
				} else 
				{
					// else set it and return
					existing.setValue( value, Variable.DECLARATION );
					return;
				}
			}
			// Careful here:
			// else fall through to override and install the new typed version
		} 

		// Add the new typed var
		variables.put( name, createVariable( name, type, value, modifiers ) );
    }

	/**
		Dissallow static vars outside of a class
		@param name is here just to allow the error message to use it
	protected void checkVariableModifiers( String name, Modifiers modifiers )
		throws UtilEvalError
	{
		if ( modifiers!=null && modifiers.hasModifier("static") )
			throw new UtilEvalError(
				"Can't declare static variable outside of class: "+name );
	}
	*/

	/**
		Note: this is primarily for internal use.
		@see Interpreter#source( String )
		@see Interpreter#eval( String )
	*/
    public void	setMethod( String name, BshMethod method )
		throws UtilEvalError
	{
		//checkMethodModifiers( method );

		if ( methods == null )
			methods = new Hashtable();

		Object m = methods.get(name);

		if ( m == null )
			methods.put(name, method);
		else 
		if ( m instanceof BshMethod ) {
			Vector v = new Vector();
			v.addElement( m );
			v.addElement( method );
			methods.put( name, v );
		} else // Vector
			((Vector)m).addElement( method );
    }

	/**
		@see #getMethod( String, Class [], boolean )
		@see #getMethod( String, Class [] )
	*/
    public BshMethod getMethod( String name, Class [] sig ) 
		throws UtilEvalError
	{
		return getMethod( name, sig, false/*declaredOnly*/ );
	}

	/**
		Get the bsh method matching the specified signature declared in 
		this name space or a parent.
		<p>
		Note: this method is primarily intended for use internally.  If you use
		this method outside of the bsh package you will have to be familiar
		with BeanShell's use of the Primitive wrapper class.
		@see bsh.Primitive
		@return the BshMethod or null if not found
		@param declaredOnly if true then only methods declared directly in this
			namespace will be found and no inherited or imported methods will
			be visible.
	*/
    public BshMethod getMethod( 
		String name, Class [] sig, boolean declaredOnly ) 
		throws UtilEvalError
	{
		BshMethod method = null;

		// Change import precedence if we are a class body/instance
		// Get import first.
		if ( method == null && isClass && !declaredOnly )
			method = getImportedMethod( name, sig );

		Object m = null;
		if ( method == null && methods != null )
		{
			m = methods.get(name);

			// m contains either BshMethod or Vector of BshMethod
			if ( m != null ) 
			{
				// unwrap 
				BshMethod [] ma;
				if ( m instanceof Vector ) 
				{
					Vector vm = (Vector)m;
					ma = new BshMethod[ vm.size() ];
					vm.copyInto( ma );
				} else
					ma = new BshMethod[] { (BshMethod)m };

				// Apply most specific signature matching
				Class [][] candidates = new Class[ ma.length ][];
				for( int i=0; i< ma.length; i++ )
					candidates[i] = ma[i].getParameterTypes();

				int match = 
					Reflect.findMostSpecificSignature( sig, candidates );
				if ( match != -1 )
					method = ma[match];
			}
		}

		if ( method == null && !isClass && !declaredOnly )
			method = getImportedMethod( name, sig );
		
		// try parent
		if ( !declaredOnly && (method == null) && (parent != null) )
			return parent.getMethod( name, sig );

		return method;
    }

	/**
		Import a class name.
		Subsequent imports override earlier ones
	*/
    public void	importClass(String name)
    {
		if ( importedClasses == null )
			importedClasses = new Hashtable();

		importedClasses.put( Name.suffix(name, 1), name );
		nameSpaceChanged();
    }

	/**
		subsequent imports override earlier ones
	*/
    public void	importPackage(String name)
    {
		if(importedPackages == null)
			importedPackages = new Vector();

		// If it exists, remove it and add it at the end (avoid memory leak)
		if ( importedPackages.contains( name ) )
			importedPackages.remove( name );

		importedPackages.addElement(name);
		nameSpaceChanged();
    }

	/**
		Import scripted or compiled BeanShell commands in the following package
		in the classpath.  You may use either "/" path or "." package notation.
		e.g. importCommands("/bsh/commands") or importCommands("bsh.commands")
		are equivalent.  If a relative path style specifier is used then it is
		made into an absolute path by prepending "/".
	*/
    public void	importCommands( String name )
    {
		if ( importedCommands == null )
			importedCommands = new Vector();

		// dots to slashes
		name = name.replace('.','/');
		// absolute
		if ( !name.startsWith("/") )
			name = "/"+name;
		// remove trailing (but preserve case of simple "/")
		if ( name.length() > 1 && name.endsWith("/") )
			name = name.substring( 0, name.length()-1 );

		// If it exists, remove it and add it at the end (avoid memory leak)
		if ( importedCommands.contains( name ) )
			importedCommands.remove( name );

		importedCommands.addElement(name);
		nameSpaceChanged();
    }

	/**
		A command is a scripted method or compiled command class implementing a 
		specified method signature.  Commands are loaded from the classpath
		and may be imported using the importCommands() method.
		<p/>

		This method searches the imported commands packages for a script or
		command object corresponding to the name of the method.  If it is a
		script the script is sourced into this namespace and the BshMethod for
		the requested signature is returned.  If it is a compiled class the
		class is returned.  (Compiled command classes implement static invoke()
		methods).
		<p/>

		The imported packages are searched in reverse order, so that later
		imports take priority.
		Currently only the first object (script or class) with the appropriate
		name is checked.  If another, overloaded form, is located in another
		package it will not currently be found.  This could be fixed.
		<p/>

		@return a BshMethod, Class, or null if no such command is found.
		@param name is the name of the desired command method
		@param argTypes is the signature of the desired command method.
		@throws UtilEvalError if loadScriptedCommand throws UtilEvalError
			i.e. on errors loading a script that was found
	*/
	public Object getCommand( 	
		String name, Class [] argTypes, Interpreter interpreter ) 
		throws UtilEvalError
	{
		if (Interpreter.DEBUG) Interpreter.debug("getCommand: "+name);
		BshClassManager bcm = interpreter.getClassManager();

		if ( importedCommands != null )
		{
			// loop backwards for precedence
			for(int i=importedCommands.size()-1; i>=0; i--)
			{
				String path = (String)importedCommands.elementAt(i);

				String scriptPath; 
				if ( path.equals("/") )
					scriptPath = path + name +".bsh";
				else
					scriptPath = path +"/"+ name +".bsh";

				Interpreter.debug("searching for script: "+scriptPath );

        		InputStream in = bcm.getResourceAsStream( scriptPath );

				if ( in != null )
					return loadScriptedCommand( 
						in, name, argTypes, scriptPath, interpreter );

				// Chop leading "/" and change "/" to "."
				String className;
				if ( path.equals("/") )
					className = name;
				else
					className = path.substring(1).replace('/','.') +"."+name;

				Interpreter.debug("searching for class: "+className);
        		Class clas = bcm.classForName( className );
				if ( clas != null )
					return clas;
			}
		}

		if ( parent != null )
			return parent.getCommand( name, argTypes, interpreter );
		else
			return null;
	}

	protected BshMethod getImportedMethod( String name, Class [] sig ) 
		throws UtilEvalError
	{
		// Try object imports
		if ( importedObjects != null )
		for(int i=0; i<importedObjects.size(); i++)
		{
			Object object = importedObjects.elementAt(i);
			Class clas = object.getClass();
			Method method = Reflect.resolveJavaMethod( 
				getClassManager(), clas, name, sig, false/*onlyStatic*/ );
			if ( method != null )
				return new BshMethod( method, object );
		}

		// Try static imports
		if ( importedStatic!= null )
		for(int i=0; i<importedStatic.size(); i++)
		{
			Class clas = (Class)importedStatic.elementAt(i);
			Method method = Reflect.resolveJavaMethod( 
				getClassManager(), clas, name, sig, true/*onlyStatic*/ );
			if ( method != null )
				return new BshMethod( method, null/*object*/ );
		}

		return null;
	}

	protected Variable getImportedVar( String name ) 
		throws UtilEvalError
	{
		// Try object imports
		if ( importedObjects != null )
		for(int i=0; i<importedObjects.size(); i++)
		{
			Object object = importedObjects.elementAt(i);
			Class clas = object.getClass();
			Field field = Reflect.resolveJavaField( 
				clas, name, false/*onlyStatic*/ );
			if ( field != null )
				return createVariable(
					name, field.getType(), new LHS( object, field ) );
		}

		// Try static imports
		if ( importedStatic!= null )
		for(int i=0; i<importedStatic.size(); i++)
		{
			Class clas = (Class)importedStatic.elementAt(i);
			Field field = Reflect.resolveJavaField( 
				clas, name, true/*onlyStatic*/ );
			if ( field != null )
				return createVariable( name, field.getType(), new LHS( field ) );
		}

		return null;
	}

	/**
		Load a command script from the input stream and find the BshMethod in
		the target namespace.
		@throws UtilEvalError on error in parsing the script or if the the
			method is not found after parsing the script.
	*/
	/*
		If we want to support multiple commands in the command path we need to
		change this to not throw the exception.
	*/
	private BshMethod loadScriptedCommand( 
		InputStream in, String name, Class [] argTypes, String resourcePath, 
		Interpreter interpreter )
		throws UtilEvalError
	{
		try {
			interpreter.eval( 
				new InputStreamReader(in), this, resourcePath );
		} catch ( EvalError e ) {
		/* 
			Here we catch any EvalError from the interpreter because we are
			using it as a tool to load the command, not as part of the
			execution path.
		*/
			Interpreter.debug( e.toString() );
			throw new UtilEvalError( 
				"Error loading script: "+ e.getMessage());
		}

		// Look for the loaded command 
		BshMethod meth = getMethod( name, argTypes );
		/*
		if ( meth == null )
			throw new UtilEvalError("Loaded resource: " + resourcePath +
				"had an error or did not contain the correct method" );
		*/

		return meth;
	}

	/**
		Helper that caches class.
	*/
	void cacheClass( String name, Class c ) {
		if ( classCache == null ) {
			classCache = new Hashtable();
			//cacheCount++; // debug
		}

		classCache.put(name, c);
	}

	/**
		Load a class through this namespace taking into account imports.
		The class search will proceed through the parent namespaces if
		necessary.

		@return null if not found.
	*/
    public Class getClass( String name )
		throws UtilEvalError
    {
		Class c = getClassImpl(name);
		if ( c != null )
			return c;
		else
			// implement the recursion for getClassImpl()
			if ( parent != null )
				return parent.getClass( name );
			else
				return null;
	}

	/**
		Implementation of getClass() 

		Load a class through this namespace taking into account imports.
		<p>

		Check the cache first.  If an unqualified name look for imported 
		class or package.  Else try to load absolute name.
		<p>

		This method implements caching of unqualified names (normally imports).
		Qualified names are cached by the BshClassManager.
		Unqualified absolute class names (e.g. unpackaged Foo) are cached too
		so that we don't go searching through the imports for them each time.

		@return null if not found.
	*/
    private Class getClassImpl( String name )
		throws UtilEvalError
    {
		Class c = null;

		// Check the cache
		if (classCache != null) {
			c =	(Class)classCache.get(name);

			if ( c != null )
				return c;
		}

		// Unqualified (simple, non-compound) name
		boolean unqualifiedName = !Name.isCompound(name);

		// Unqualified name check imported
		if ( unqualifiedName ) 
		{
			// Try imported class
			if ( c == null )
				c = getImportedClassImpl( name );

			// if found as imported also cache it
			if ( c != null ) {
				cacheClass( name, c );
				return c;
			}
		}

		// Try absolute
		c = classForName( name );
		if ( c != null ) {
			// Cache unqualified names to prevent import check again
			if ( unqualifiedName )
				cacheClass( name, c );
			return c;
		}

		// Not found
		if ( Interpreter.DEBUG ) 
			Interpreter.debug("getClass(): " + name	+ " not	found in "+this);
		return null;
    }

	/**
		Try to make the name into an imported class.
		This method takes into account only imports (class or package)
		found directly in this NameSpace (no parent chain).
	*/
    private Class getImportedClassImpl( String name )
		throws UtilEvalError
    {
		// Try explicitly imported class, e.g. import foo.Bar;
		String fullname = null;
		if ( importedClasses != null )
			fullname = (String)importedClasses.get(name);
		
		// not sure if we should really recurse here for explicitly imported
		// class in parent...  

		if ( fullname != null ) 
		{
			/*
				Found the full name in imported classes.
			*/
			// Try to make the full imported name
			Class clas=classForName(fullname);
			
			// Handle imported inner class case
			if ( clas == null ) 
			{
				// Imported full name wasn't found as an absolute class
				// If it is compound, try to resolve to an inner class.  
				// (maybe this should happen in the BshClassManager?)

				if ( Name.isCompound( fullname ) )
					try {
						clas = getNameResolver( fullname ).toClass();
					} catch ( ClassNotFoundException e ) { /* not a class */ }
				else 
					if ( Interpreter.DEBUG ) Interpreter.debug(
						"imported unpackaged name not found:" +fullname);

				// If found cache the full name in the BshClassManager
				if ( clas != null ) {
					// (should we cache info in not a class case too?)
					getClassManager().cacheClassInfo( fullname, clas );
					return clas;
				}
			} else
				return clas;

			// It was explicitly imported, but we don't know what it is.
			// should we throw an error here??
			return null;  
		}

		/*
			Try imported packages, e.g. "import foo.bar.*;"
			in reverse order of import...
			(give later imports precedence...)
		*/
		if ( importedPackages != null )
			for(int i=importedPackages.size()-1; i>=0; i--)
			{
				String s = ((String)importedPackages.elementAt(i)) + "." + name;
				Class c=classForName(s);
				if ( c != null )
					return c;
			}

		BshClassManager bcm = getClassManager();
		/*
			Try super import if available
			Note: we do this last to allow explicitly imported classes
			and packages to take priority.  This method will also throw an
			error indicating ambiguity if it exists...
		*/
		if ( bcm.hasSuperImport() ) 
		{
			String s = bcm.getClassNameByUnqName( name );
			if ( s != null )
				return classForName( s );
		}

		return null;
    }

	private Class classForName( String name ) 
	{
		return getClassManager().classForName( name );
	}

	/**
		Implements NameSource
		@return all variable and method names in this and all parent
		namespaces
	*/
	public String [] getAllNames() 
	{
		Vector vec = new Vector();
		getAllNamesAux( vec );
		String [] names = new String [ vec.size() ];
		vec.copyInto( names );
		return names;
	}

	/**
		Helper for implementing NameSource
	*/
	protected void getAllNamesAux( Vector vec ) 
	{
		Enumeration varNames = variables.keys();
		while( varNames.hasMoreElements() )
			vec.addElement( varNames.nextElement() );

		Enumeration methodNames = methods.keys();
		while( methodNames.hasMoreElements() )
			vec.addElement( methodNames.nextElement() );

		if ( parent != null )
			parent.getAllNamesAux( vec );
	}

	Vector nameSourceListeners;
	/**
		Implements NameSource
		Add a listener who is notified upon changes to names in this space.
	*/
	public void addNameSourceListener( NameSource.Listener listener ) {
		if ( nameSourceListeners == null )
			nameSourceListeners = new Vector();
		nameSourceListeners.addElement( listener );
	}
	
	/**
		Perform "import *;" causing the entire classpath to be mapped.
		This can take a while.
	*/
	public void doSuperImport() 
		throws UtilEvalError
	{
		getClassManager().doSuperImport();
	}


	public String toString() {
		return "NameSpace: " 
			+ ( nsName==null
				? super.toString()
				: nsName + " (" + super.toString() +")" )
			+ ( isClass ? " (isClass) " : "" )
			+ ( isMethod ? " (method) " : "" )
			+ ( classStatic != null ? " (class static) " : "" )
			+ ( classInstance != null ? " (class instance) " : "" );
	}

	/*
		For serialization.
		Don't serialize non-serializable objects.
	*/
    private synchronized void writeObject(java.io.ObjectOutputStream s)
        throws IOException 
	{
		// clear name resolvers... don't know if this is necessary.
		names = null;
	
		s.defaultWriteObject();
	}

	/**
		Invoke a method in this namespace with the specified args and
		interpreter reference.  No caller information or call stack is
		required.  The method will appear as if called externally from Java.
		<p>

		@see bsh.This.invokeMethod( 
			String methodName, Object [] args, Interpreter interpreter, 
			CallStack callstack, SimpleNode callerInfo, boolean )
	*/
	public Object invokeMethod( 
		String methodName, Object [] args, Interpreter interpreter ) 
		throws EvalError
	{
		return invokeMethod( 
			methodName, args, interpreter, null, null );
	}

	/**
		This method simply delegates to This.invokeMethod();
		<p>
		@see bsh.This.invokeMethod( 
			String methodName, Object [] args, Interpreter interpreter, 
			CallStack callstack, SimpleNode callerInfo )
	*/
	public Object invokeMethod( 
		String methodName, Object [] args, Interpreter interpreter, 
		CallStack callstack, SimpleNode callerInfo ) 
		throws EvalError
	{
		return getThis( interpreter ).invokeMethod( 
			methodName, args, interpreter, callstack, callerInfo,
			false/*declaredOnly*/ );
	}

	/**
		Clear all cached classes and names
	*/
	public void classLoaderChanged() {
		nameSpaceChanged();
	}

	/**
		Clear all cached classes and names
	*/
	public void nameSpaceChanged() {
		classCache = null;
		names = null;
	}

	/**
		Import standard packages.  Currently:
		<pre>
			importClass("bsh.EvalError");
			importClass("bsh.Interpreter");
			importPackage("javax.swing.event");
			importPackage("javax.swing");
			importPackage("java.awt.event");
			importPackage("java.awt");
			importPackage("java.net");
			importPackage("java.util");
			importPackage("java.io");
			importPackage("java.lang");
			importCommands("/bsh/commands");
		</pre>
	*/
    public void loadDefaultImports()
    {
		/**
			Note: the resolver looks through these in reverse order, per
			precedence rules...  so for max efficiency put the most common
			ones later.
		*/
		importClass("bsh.EvalError");
		importClass("bsh.Interpreter");
		importPackage("javax.swing.event");
		importPackage("javax.swing");
		importPackage("java.awt.event");
		importPackage("java.awt");
		importPackage("java.net");
		importPackage("java.util");
		importPackage("java.io");
		importPackage("java.lang");
		importCommands("/bsh/commands");
    }

	/**
		This is the factory for Name objects which resolve names within
		this namespace (e.g. toObject(), toClass(), toLHS()).
		<p>

		This was intended to support name resolver caching, allowing 
		Name objects to cache info about the resolution of names for 
		performance reasons.  However this not proven useful yet.  
		<p>

		We'll leave the caching as it will at least minimize Name object
		creation.
		<p>

		(This method would be called getName() if it weren't already used for 
		the simple name of the NameSpace)
		<p>

		This method was public for a time, which was a mistake.  
		Use get() instead.
	*/
	Name getNameResolver( String ambigname ) 
	{
		if ( names == null )
			names = new Hashtable();

		Name name = (Name)names.get( ambigname );

		if ( name == null ) {
			name = new Name( this, ambigname );
			names.put( ambigname, name );
		} 

		return name;
	}

	public int getInvocationLine() {
		SimpleNode node = getNode();
		if ( node != null )
			return node.getLineNumber();
		else
			return -1;
	}
	public String getInvocationText() {
		SimpleNode node = getNode();
		if ( node != null )
			return node.getText();
		else
			return "<invoked from Java code>";
	}

	/**
		This is a helper method for working inside of bsh scripts and commands.
		In that context it is impossible to see a ClassIdentifier object
		for what it is.  Attempting to access a method on a ClassIdentifier
		will look like a static method invocation.  
		
		This method is in NameSpace for convenience (you don't have to import
		bsh.ClassIdentifier to use it );
	*/
	public static Class identifierToClass( ClassIdentifier ci ) 
	{
		return ci.getTargetClass();
	}


	/**
		Clear all variables, methods, and imports from this namespace.
		If this namespace is the root, it will be reset to the default 
		imports.
		@see #loadDefaultImports()
	*/
	public void clear() 
	{
		variables = null;
		methods = null;
		importedClasses = null;
		importedPackages = null;
		importedCommands = null;
		importedObjects = null;
		if ( parent == null )
			loadDefaultImports();	
    	classCache = null;
		names = null;
	}

	/**
		Import a compiled Java object's methods and variables into this 
		namespace.  When no scripted method / command or variable is found
		locally in this namespace method / fields of the object will be
		checked.  Objects are checked in the order of import with later imports
		taking precedence.
		<p/>
	*/
	/*
		Note: this impor pattern is becoming common... could factor it out into
		an importedObject Vector class.
	*/
	public void importObject( Object obj ) 
	{
		if ( importedObjects == null )
			importedObjects = new Vector();

		// If it exists, remove it and add it at the end (avoid memory leak)
		if ( importedObjects.contains( obj ) )
			importedObjects.remove( obj );

		importedObjects.addElement( obj );
		nameSpaceChanged();

	}

	/**
	*/
	public void importStatic( Class clas ) 
	{
		if ( importedStatic == null )
			importedStatic = new Vector();

		// If it exists, remove it and add it at the end (avoid memory leak)
		if ( importedStatic.contains( clas ) )
			importedStatic.remove( clas );

		importedStatic.addElement( clas );
		nameSpaceChanged();
	}

	/**
		Set the package name for classes defined in this namespace.
		Subsequent sets override the package.
	*/
	void setPackage( String packageName ) 
	{
		this.packageName = packageName;
	}

	String getPackage() 
	{
		if ( packageName != null )
			return packageName;

		if ( parent != null )
			return parent.getPackage();
		
		return null;
	}
	
    /**
     * If a writable property exists for the given name, set it and return true,
     * otherwise do nothing and return false.
     */
    boolean attemptSetPropertyValue(String propName,
    		Object value, Interpreter interp)
    	throws UtilEvalError {
    	
    	String accessorName = Reflect.accessorName( "set", propName );
    	
    	Class[] classArray = new Class[] {value==null ? null : value.getClass()};
    	
    	BshMethod m = getMethod(accessorName, classArray);
    	
    	if(m!=null) {
	    	try {  
	    		invokeMethod(accessorName, new Object[] {value},
	    				interp);
	    		// m.invoke(new Object[] {value}, interp);
	    		return true;	
	    	} catch(EvalError ee) {
	    		throw new UtilEvalError(
	    			"'This' property accessor threw exception: " +
	    			ee.getMessage());
	    	}
    	}
    	
    	return false;
	}
    
    /**
     * Get a property from a scripted object or Primitive.VOID if no such
     * property exists.
     * @throws UtilEvalError 
     * @throws UtilEvalError 
     */
    Object getPropertyValue(String propName, Interpreter interp)
    	throws UtilEvalError {
    	
    	String accessorName = Reflect.accessorName( "get", propName );
    	Class[] classArray = new Class[0];
    	
    	BshMethod m = getMethod(accessorName, classArray);
    	
    	try {
	    	if(m!=null)
	    		return m.invoke((Object[])null, interp);
	    	
	    	accessorName = Reflect.accessorName( "is", propName );
	    	m = getMethod(accessorName, classArray);
	    	if(m!=null)
	    		return m.invoke((Object[])null, interp);
	    	
	    	return Primitive.VOID;
    	} catch(EvalError ee) {
    		throw new UtilEvalError("'This' property accessor threw exception: "
    				+ ee.getMessage() );
    	}
	}   
}

