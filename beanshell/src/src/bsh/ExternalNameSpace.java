package	bsh;

import java.util.*;

/**
	A namespace which maintains an external map of values held in variables in
	its scope.  This mechanism provides a standard collections based interface
	to the namespace as well as a convenient way to export and view values of
	the namespace without the ordinary BeanShell wrappers.   
	</p>

	Variables are maintained internally in the normal fashion to support
	meta-information (such as variable type and visibility modifiers), but
	exported and imported in a synchronized way.  Variables are exported each
	time they are written by BeanShell.  Imported variables from the map appear
	in the BeanShell namespace as untyped variables with no modifiers and
	shadow any previously defined variables in the scope. 
	<p/>

	Note: this class is inherentely dependent on Java 1.2 (for Map), however
	it is not used directly by the core as other than type NameSpace, so no
	dependency is introduced.
*/
/*
	Implementation notes:

	It would seem that we should have been accomplished this by overriding the
	getImportedVar() method of NameSpace, which behaves in a similar way
	for fields of classes and objects.  However we need more control here to
	be able to bump up the precedence and remove items that have been removed
	via the map.  So we override getVariableImp().  We should reevaluate this
	at some point.  All of NameSpace is a mess.

	The primary abstraction here is that we override createVariable() to
	create LHS Variables bound to the map for this namespace.

	Methods:

	bsh methods are not currently exported to the
	external namespace.  All that would be required to add this is to override
	setMethod() and provide a friendlier view than vector (currently used) for
	overloaded forms (perhaps a map by method SignatureKey).

*/
public class ExternalNameSpace extends NameSpace
{
	private Map externalMap;

    public ExternalNameSpace() 
	{
		this( null, "External Map Namespace", null );
	}

	/**
	*/
    public ExternalNameSpace( NameSpace parent, String name, Map externalMap ) 
	{
		super( parent, name );

		if ( externalMap == null )
			externalMap = new HashMap();
			
		this.externalMap = externalMap;

	}

	/**
		Get the map view of this namespace.
	*/
	public Map getMap() { return externalMap; }

	/**
		Set the external Map which to which this namespace synchronizes.
		The previous external map is detached from this namespace.  Previous
		map values are retained in the external map, but are removed from the
		BeanShell namespace.
	*/
	public void setMap( Map map ) 
	{ 
		// Detach any existing namespace to preserve it, then clear this
		// namespace and set the new one
		this.externalMap = null; 
		clear();
		this.externalMap = map ; 
	}

	/**
	*/
	public void unsetVariable( String name )
	{
		super.unsetVariable( name );
		externalMap.remove( name );
	}

	/**
	*/
	public String [] getVariableNames() 
	{
		// union of the names in the enclosing namespace and external map
		Set nameSet = new HashSet();
		String [] nsNames = super.getVariableNames();
		nameSet.addAll( Arrays.asList( nsNames ) );
		nameSet.addAll( externalMap.keySet() );
		return (String [])nameSet.toArray( new String[0] );
	}

	/**
	*/
	/*
		Notes: This implementation of getVariableImpl handles the following
		cases:
		1) var in map not in local scope - var was added through map
		2) var in map and in local scope - var was added through namespace
		3) var not in map but in local scope - var was removed via map
		4) var not in map and not in local scope - non-existent var

		Note: It would seem that we could simply override getImportedVar()
		in NameSpace, rather than this higher level method.  However we need
		more control here to change the import precedence and remove variables
		if they are removed via the extenal map.
	*/
		protected Variable getVariableImpl( String name, boolean recurse )
		throws UtilEvalError
	{
		// check the external map for the variable name
		Object value =  externalMap.get( name );

		Variable var;
		if ( value == null ) 
		{
			// The var is not in external map and it should therefore not be
			// found in local scope (it may have been removed via the map).  
			// Clear it prophalactically.
			super.unsetVariable( name ); 

			// Search parent for var if applicable.
			var = super.getVariableImpl( name, recurse );
		} else
		{
			// Var in external map may be found in local scope with type and
			// modifier info.
			Variable localVar = super.getVariableImpl( name, false );

			// If not in local scope then it was added via the external map,
			// we'll wrap it and pass it along.  Else we'll use the one we
			// found.
			if ( localVar == null ) 
				var = createVariable( name, null/*type*/, value, null/*mods*/ );
			else
				var = localVar;
		}

		return var;
    }

	public Variable createVariable(
		String name, Class type, Object value, Modifiers mods )
	{
		LHS lhs = new LHS( externalMap, name );
		// Is this race condition worth worrying about?
		// value will appear in map before it's really in the interpreter
		try {
			lhs.assign( value, false/*strict*/ );
		} catch ( UtilEvalError e) {
			throw new InterpreterError( e.toString() );
		}
		return new Variable( name, type, lhs );
	}

	/**
		Clear all variables, methods, and imports from this namespace and clear
		all values from the external map (via Map clear()).
	*/
	public void clear() 
	{
		super.clear();
		externalMap.clear();
	}

}

