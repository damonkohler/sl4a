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

import java.lang.reflect.*;
import java.util.Vector;

/**
 * All of the reflection API code lies here.  It is in the form of static
 * utilities.  Maybe this belongs in LHS.java or a generic object
 * wrapper class.
 * 
 * @author Pat Niemeyer
 * @author Daniel Leuck
 */
/*
	Note: This class is messy.  The method and field resolution need to be
	rewritten.  Various methods in here catch NoSuchMethod or NoSuchField
	exceptions during their searches.  These should be rewritten to avoid
	having to catch the exceptions.  Method lookups are now cached at a high 
	level so they are less important, however the logic is messy.
*/
class Reflect 
{
    /**
		Invoke method on arbitrary object instance.
		invocation may be static (through the object instance) or dynamic.
		Object may be a bsh scripted object (bsh.This type).
	 	@return the result of the method call
	*/
    public static Object invokeObjectMethod(
		Object object, String methodName, Object[] args, 
		Interpreter interpreter, CallStack callstack, SimpleNode callerInfo ) 
		throws ReflectError, EvalError, InvocationTargetException
	{
		// Bsh scripted object
		if ( object instanceof This && !This.isExposedThisMethod(methodName) )
			return ((This)object).invokeMethod( 
				methodName, args, interpreter, callstack, callerInfo,
				false/*delcaredOnly*/
			);

		// Plain Java object, find the java method
		try {
			BshClassManager bcm =
				interpreter == null ? null : interpreter.getClassManager();
			Class clas = object.getClass();

			Method method = resolveExpectedJavaMethod(
				bcm, clas, object, methodName, args, false );

			return invokeMethod( method, object, args );
		} catch ( UtilEvalError e ) {
			throw e.toEvalError( callerInfo, callstack );
		}
    }

    /** 
		Invoke a method known to be static.
		No object instance is needed and there is no possibility of the 
		method being a bsh scripted method.
	*/
    public static Object invokeStaticMethod(
		BshClassManager bcm, Class clas, String methodName, Object [] args )
        throws ReflectError, UtilEvalError, InvocationTargetException
    {
        Interpreter.debug("invoke static Method");
        Method method = resolveExpectedJavaMethod( 
			bcm, clas, null, methodName, args, true );
		return invokeMethod( method, null, args );
    }

	/**
		Invoke the Java method on the specified object, performing needed
	 	type mappings on arguments and return values.
		@param args may be null
	*/
	static Object invokeMethod(
		Method method, Object object, Object[] args ) 
		throws ReflectError, InvocationTargetException
	{
		if ( args == null )
			args = new Object[0];

		logInvokeMethod( "Invoking method (entry): ", method, args );

		// Map types to assignable forms, need to keep this fast...
		Object [] tmpArgs = new Object [ args.length ];
		Class [] types = method.getParameterTypes();
		try {
			for (int i=0; i<args.length; i++)
				tmpArgs[i] = Types.castObject(
					args[i]/*rhs*/, types[i]/*lhsType*/, Types.ASSIGNMENT );
		} catch ( UtilEvalError e ) {
			throw new InterpreterError(
				"illegal argument type in method invocation: "+e );
		}

		// unwrap any primitives
		tmpArgs = Primitive.unwrap( tmpArgs );

		logInvokeMethod( "Invoking method (after massaging values): ",
			method, tmpArgs );

		try {
			Object returnValue = method.invoke( object, tmpArgs );
			if ( returnValue == null )
				returnValue = Primitive.NULL;
			Class returnType = method.getReturnType();

			return Primitive.wrap( returnValue, returnType );
		} catch( IllegalAccessException e ) {
			throw new ReflectError( "Cannot access method " 
				+ StringUtil.methodString(
					method.getName(), method.getParameterTypes() ) 
				+ " in '" + method.getDeclaringClass() + "' :" + e );
		}
	}

	public static Object getIndex(Object array, int index)
        throws ReflectError, UtilTargetError
    {
		if ( Interpreter.DEBUG ) 
			Interpreter.debug("getIndex: "+array+", index="+index);
        try {
            Object val = Array.get(array, index);
            return Primitive.wrap( val, array.getClass().getComponentType() );
        }
        catch( ArrayIndexOutOfBoundsException  e1 ) {
			throw new UtilTargetError( e1 );
        } catch(Exception e) {
            throw new ReflectError("Array access:" + e);
        }
    }

    public static void setIndex(Object array, int index, Object val)
        throws ReflectError, UtilTargetError
    {
        try {
            val = Primitive.unwrap(val);
            Array.set(array, index, val);
        }
        catch( ArrayStoreException e2 ) {
			throw new UtilTargetError( e2 );
        } catch( IllegalArgumentException e1 ) {
			throw new UtilTargetError( 
				new ArrayStoreException( e1.toString() ) );
        } catch(Exception e) {
            throw new ReflectError("Array access:" + e);
        }
    }

    public static Object getStaticFieldValue(Class clas, String fieldName)
        throws UtilEvalError, ReflectError
    {
        return getFieldValue( clas, null, fieldName, true/*onlystatic*/);
    }

	/**
	 * Check for a field with the given name in a java object or scripted object
	 * if the field exists fetch the value, if not check for a property value.
	 * If neither is found return Primitive.VOID. 
	 */
    public static Object getObjectFieldValue( Object object, String fieldName )
        throws UtilEvalError, ReflectError
    {
		if ( object instanceof This ) {
			This t = (This)object;
			return t.namespace.getVariableOrProperty(fieldName, null);
		} else {
			try {
				return getFieldValue(
					object.getClass(), object, fieldName, false/*onlystatic*/);
			} catch ( ReflectError e ) {
				// no field, try property acces

				if ( hasObjectPropertyGetter( object.getClass(), fieldName ) )
					return getObjectProperty( object, fieldName );
				else
					throw e;
			}
		}
    }

	static LHS getLHSStaticField(Class clas, String fieldName)
        throws UtilEvalError, ReflectError
    {
        Field f = resolveExpectedJavaField( 
			clas, fieldName, true/*onlystatic*/);
        return new LHS(f);
    }

	/**
		Get an LHS reference to an object field.

		This method also deals with the field style property access.
		In the field does not exist we check for a property setter.
	*/
    static LHS getLHSObjectField( Object object, String fieldName )
        throws UtilEvalError, ReflectError
    {
		if ( object instanceof This )
		{
			// I guess this is when we pass it as an argument?
			// Setting locally
			boolean recurse = false; 
			return new LHS( ((This)object).namespace, fieldName, recurse );
		}

		try {
			Field f = resolveExpectedJavaField( 
				object.getClass(), fieldName, false/*staticOnly*/ );
			return new LHS(object, f);
		} catch ( ReflectError e ) 
		{
			// not a field, try property access
			if ( hasObjectPropertySetter( object.getClass(), fieldName ) )
				return new LHS( object, fieldName );
			else
				throw e;
		}
    }

    private static Object getFieldValue(
		Class clas, Object object, String fieldName, boolean staticOnly )
		throws UtilEvalError, ReflectError
    {
        try {
            Field f = resolveExpectedJavaField( clas, fieldName, staticOnly );

            Object value = f.get(object);
            Class returnType = f.getType();
            return Primitive.wrap( value, returnType );

        } catch( NullPointerException e ) { // shouldn't happen
            throw new ReflectError(
				"???" + fieldName + " is not a static field.");
        } catch(IllegalAccessException e) {
            throw new ReflectError("Can't access field: " + fieldName);
        }
    }

	/*
		Note: this method and resolveExpectedJavaField should be rewritten
		to invert this logic so that no exceptions need to be caught
		unecessarily.  This is just a temporary impl.
		@return the field or null if not found
	*/
    protected static Field resolveJavaField( 
		Class clas, String fieldName, boolean staticOnly )
        throws UtilEvalError
    {
		try {
			return resolveExpectedJavaField( clas, fieldName, staticOnly );
		} catch ( ReflectError e ) { 
			return null;
		}
	}

	/**
		@throws ReflectError if the field is not found.
	*/
	/*
		Note: this should really just throw NoSuchFieldException... need
		to change related signatures and code.
	*/
    protected static Field resolveExpectedJavaField( 
		Class clas, String fieldName, boolean staticOnly
	)
        throws UtilEvalError, ReflectError
    {
		Field field;
        try {
			if ( Capabilities.haveAccessibility() )
				field = findAccessibleField( clas, fieldName );
			else
				// Class getField() finds only public (and in interfaces, etc.)
				field = clas.getField(fieldName);
        }
        catch( NoSuchFieldException e) {
            throw new ReflectError("No such field: " + fieldName );
		} catch ( SecurityException e ) {
			throw new UtilTargetError( 
			"Security Exception while searching fields of: "+clas,
			e );
		}

		if ( staticOnly && !Modifier.isStatic( field.getModifiers() ) )
			throw new UtilEvalError(
				"Can't reach instance field: "+fieldName
				+" from static context: "+clas.getName() );

		return field;
    }

	/**
		Used when accessibility capability is available to locate an occurrance
		of the field in the most derived class or superclass and set its 
		accessibility flag.
		Note that this method is not needed in the simple non accessible
		case because we don't have to hunt for fields.
		Note that classes may declare overlapping private fields, so the 
		distinction about the most derived is important.  Java doesn't normally
		allow this kind of access (super won't show private variables) so 
		there is no real syntax for specifying which class scope to use...

		@return the Field or throws NoSuchFieldException
		@throws NoSuchFieldException if the field is not found
	*/
	/*
		This method should be rewritten to use getFields() and avoid catching
		exceptions during the search.
	*/
	private static Field findAccessibleField( Class clas, String fieldName ) 
		throws UtilEvalError, NoSuchFieldException
	{
		Field field;

		// Quick check catches public fields include those in interfaces
		try {
			field = clas.getField(fieldName);
			ReflectManager.RMSetAccessible( field );
			return field;
		} catch ( NoSuchFieldException e ) { }

		// Now, on with the hunt...
		while ( clas != null )
		{
			try {
				field = clas.getDeclaredField(fieldName);
				ReflectManager.RMSetAccessible( field );
				return field;

				// Not found, fall through to next class

			} catch(NoSuchFieldException e) { }

			clas = clas.getSuperclass();
		}
		throw new NoSuchFieldException( fieldName );
	}

	/**
		This method wraps resolveJavaMethod() and expects a non-null method
	 	result. If the method is not found it throws a descriptive ReflectError.
	*/
    protected static Method resolveExpectedJavaMethod(
		BshClassManager bcm, Class clas, Object object, 
		String name, Object[] args, boolean staticOnly )
        throws ReflectError, UtilEvalError
    {
		if ( object == Primitive.NULL )
			throw new UtilTargetError( new NullPointerException(
				"Attempt to invoke method " +name+" on null value" ) );

		Class [] types = Types.getTypes(args);
		Method method = resolveJavaMethod( bcm, clas, name, types, staticOnly );

		if ( method == null )
			throw new ReflectError(
				( staticOnly ? "Static method " : "Method " )
				+ StringUtil.methodString(name, types) +
				" not found in class'" + clas.getName() + "'");

		return method;
	}

    /**
        The full blown resolver method.  All other method invocation methods
		delegate to this.  The method may be static or dynamic unless
		staticOnly is set (in which case object may be null).
		If staticOnly is set then only static methods will be located.
		<p/>

		This method performs caching (caches discovered methods through the
	 	class manager and utilizes cached methods.)
	 	<p/>

	 	This method determines whether to attempt to use non-public methods
	 	based on Capabilities.haveAccessibility() and will set the accessibilty
	 	flag on the method as necessary.
	 	<p/>

		If, when directed to find a static method, this method locates a more 
		specific matching instance method it will throw a descriptive exception 
		analogous to the error that the Java compiler would produce.
		Note: as of 2.0.x this is a problem because there is no way to work
		around this with a cast. 
		<p/>

		@param staticOnly
			The method located must be static, the object param may be null.
		@return the method or null if no matching method was found.
	*/
    protected static Method resolveJavaMethod(
		BshClassManager bcm, Class clas, String name, 
		Class [] types, boolean staticOnly )
		throws UtilEvalError
    {
		if ( clas == null )
			throw new InterpreterError("null class");

		// Lookup previously cached method
		Method method = null;
		if ( bcm == null ) 
			Interpreter.debug("resolveJavaMethod UNOPTIMIZED lookup");
		else
			method = bcm.getResolvedMethod( clas, name, types, staticOnly );

		if ( method == null )
		{
			boolean publicOnly = !Capabilities.haveAccessibility();
			// Searching for the method may, itself be a priviledged action
			try {
				method = findOverloadedMethod( clas, name, types, publicOnly );
			} catch ( SecurityException e ) {
				throw new UtilTargetError( 
				"Security Exception while searching methods of: "+clas,
				e );
			}

			checkFoundStaticMethod( method, staticOnly, clas );

			// This is the first time we've seen this method, set accessibility
			// Note: even if it's a public method, we may have found it in a
			// non-public class
			if ( method != null && !publicOnly ) {
				try {
					ReflectManager.RMSetAccessible( method );
				} catch ( UtilEvalError e ) { /*ignore*/ }
			}

			// If succeeded cache the resolved method.
			if ( method != null && bcm != null )
				bcm.cacheResolvedMethod( clas, types, method );
		}

		return method;
	}

	/**
		Get the candidate methods by searching the class and interface graph
	 	of baseClass and resolve the most specific.
	 	@return the method or null for not found
	 */
	private static Method findOverloadedMethod(
		Class baseClass, String methodName, Class[] types, boolean publicOnly )
	{
		if ( Interpreter.DEBUG )
			Interpreter.debug( "Searching for method: "+
				StringUtil.methodString(methodName, types)
				+ " in '" + baseClass.getName() + "'" );

		Method [] methods = getCandidateMethods(
			baseClass, methodName, types.length, publicOnly );

		if ( Interpreter.DEBUG )
			Interpreter.debug("Looking for most specific method: "+methodName);
		Method method = findMostSpecificMethod( types, methods );

		return method;
	}

	/**
		Climb the class and interface inheritence graph of the type and collect
		all methods matching the specified name and criterion.  If publicOnly
		is true then only public methods in *public* classes or interfaces will
		be returned.  In the normal (non-accessible) case this addresses the
		problem that arises when a package private class or private inner class
		implements a public interface or derives from a public type.
	 	<p/>

	 	This method primarily just delegates to gatherMethodsRecursive()
	 	@see #gatherMethodsRecursive(
			Class, String, int, boolean, java.util.Vector)
	*/
	static Method[] getCandidateMethods(
		Class baseClass, String methodName, int numArgs,
		boolean publicOnly )
	{
		Vector  candidates = gatherMethodsRecursive(
			baseClass, methodName, numArgs, publicOnly, null/*candidates*/);

		// return the methods in an array
		Method [] ma = new Method[ candidates.size() ];
		candidates.copyInto( ma );
		return ma;
	}

	/**
		Accumulate all methods, optionally including non-public methods,
	 	class and interface, in the inheritence tree of baseClass.

		This method is analogous to Class getMethods() which returns all public
		methods in the inheritence tree.

		In the normal (non-accessible) case this also addresses the problem
		that arises when a package private class or private inner class
		implements a public interface or derives from a public type.  In other
		words, sometimes we'll find public methods that we can't use directly
		and we have to find the same public method in a parent class or
		interface.

		@return the candidate methods vector
	*/
	private static Vector gatherMethodsRecursive(
		Class baseClass, String methodName, int numArgs,
		boolean publicOnly, Vector candidates )
	{
		if ( candidates == null )
			candidates = new Vector();

		// Add methods of the current class to the vector.
		// In public case be careful to only add methods from a public class
		// and to use getMethods() instead of getDeclaredMethods()
		// (This addresses secure environments)
		if ( publicOnly ) {
			if ( isPublic(baseClass) )
				addCandidates( baseClass.getMethods(),
					methodName, numArgs, publicOnly, candidates );
		} else
			addCandidates( baseClass.getDeclaredMethods(),
				methodName, numArgs, publicOnly, candidates );

		// Does the class or interface implement interfaces?
		Class [] intfs = baseClass.getInterfaces();
		for( int i=0; i< intfs.length; i++ )
			gatherMethodsRecursive(  intfs[i],
				methodName, numArgs, publicOnly, candidates );

		// Do we have a superclass? (interfaces don't, etc.)
		Class superclass = baseClass.getSuperclass();
		if ( superclass != null )
			gatherMethodsRecursive( superclass,
				methodName, numArgs, publicOnly, candidates );

		return candidates;
	}

	private static Vector addCandidates(
		Method [] methods, String methodName,
		int numArgs, boolean publicOnly, Vector candidates  )
	{
		for ( int i = 0; i < methods.length; i++ )
		{
			Method m = methods[i];
			if (  m.getName().equals( methodName )
				&& ( m.getParameterTypes().length == numArgs )
				&& ( !publicOnly || isPublic( m ) )
			)
				candidates.add( m );
		}
		return candidates;
	}

	/**
		Primary object constructor
		This method is simpler than those that must resolve general method
		invocation because constructors are not inherited.
	 <p/>
	 This method determines whether to attempt to use non-public constructors
	 based on Capabilities.haveAccessibility() and will set the accessibilty
	 flag on the method as necessary.
	 <p/>
	*/
    static Object constructObject( Class clas, Object[] args )
        throws ReflectError, InvocationTargetException
    {
		if ( clas.isInterface() )
			throw new ReflectError(
				"Can't create instance of an interface: "+clas);

        Object obj = null;
        Class[] types = Types.getTypes(args);
        Constructor con = null;

		// Find the constructor.
		// (there are no inherited constructors to worry about)
		Constructor[] constructors =
			Capabilities.haveAccessibility() ?
				clas.getDeclaredConstructors() : clas.getConstructors() ;

		if ( Interpreter.DEBUG )
			Interpreter.debug("Looking for most specific constructor: "+clas);
		con = findMostSpecificConstructor(types, constructors);
		if ( con == null )
			throw cantFindConstructor( clas, types );

		if ( !isPublic( con ) )
			try {
				ReflectManager.RMSetAccessible( con );
			} catch ( UtilEvalError e ) { /*ignore*/ }

        args=Primitive.unwrap( args );
        try {
            obj = con.newInstance( args );
        } catch(InstantiationException e) {
            throw new ReflectError("The class "+clas+" is abstract ");
        } catch(IllegalAccessException e) {
            throw new ReflectError(
				"We don't have permission to create an instance."
				+"Use setAccessibility(true) to enable access." );
        } catch(IllegalArgumentException e) {
            throw new ReflectError("The number of arguments was wrong");
        }
		if (obj == null)
            throw new ReflectError("Couldn't construct the object");

        return obj;
    }

    /*
        This method should parallel findMostSpecificMethod()
		The only reason it can't be combined is that Method and Constructor
		don't have a common interface for their signatures
    */
    static Constructor findMostSpecificConstructor(
		Class[] idealMatch, Constructor[] constructors)
    {
		int match = findMostSpecificConstructorIndex(idealMatch, constructors );
		return ( match == -1 ) ? null : constructors[ match ];
    }

    static int findMostSpecificConstructorIndex(
		Class[] idealMatch, Constructor[] constructors)
    {
		Class [][] candidates = new Class [ constructors.length ] [];
		for(int i=0; i< candidates.length; i++ )
			candidates[i] = constructors[i].getParameterTypes();

		return findMostSpecificSignature( idealMatch, candidates );
    }

	/**
		Find the best match for signature idealMatch.
		It is assumed that the methods array holds only valid candidates
		(e.g. method name and number of args already matched).
		This method currently does not take into account Java 5 covariant
		return types... which I think will require that we find the most
		derived return type of otherwise identical best matches.

	 	@see #findMostSpecificSignature(Class[], Class[][])
		@param methods the set of candidate method which differ only in the
	 		types of their arguments.
	*/
	static Method findMostSpecificMethod(
		Class[] idealMatch, Method[] methods )
	{
		// copy signatures into array for findMostSpecificMethod()
		Class [][] candidateSigs = new Class [ methods.length ][];
		for(int i=0; i<methods.length; i++)
			candidateSigs[i] = methods[i].getParameterTypes();

		int match = findMostSpecificSignature( idealMatch, candidateSigs );
		return match == -1 ? null : methods[match];
	}

	/**
        Implement JLS 15.11.2
		Return the index of the most specific arguments match or -1 if no
		match is found.
		This method is used by both methods and constructors (which
	 	unfortunately don't share a common interface for signature info).

	 @return the index of the most specific candidate

	 */
	/*
	 Note: Two methods which are equally specific should not be allowed by
	 the Java compiler.  In this case BeanShell currently chooses the first
	 one it finds.  We could add a test for this case here (I believe) by
	 adding another isSignatureAssignable() in the other direction between
	 the target and "best" match.  If the assignment works both ways then
	 neither is more specific and they are ambiguous.  I'll leave this test
	 out for now because I'm not sure how much another test would impact
	 performance.  Method selection is now cached at a high level, so a few
	 friendly extraneous tests shouldn't be a problem.
	*/
	static int findMostSpecificSignature(
		Class [] idealMatch, Class [][] candidates )
	{
		for ( int round = Types.FIRST_ROUND_ASSIGNABLE;
			  round <= Types.LAST_ROUND_ASSIGNABLE; round++ )
		{
			Class [] bestMatch = null;
			int bestMatchIndex = -1;

			for (int i=0; i < candidates.length; i++)
			{
				Class[] targetMatch = candidates[i];

				// If idealMatch fits targetMatch and this is the first match
				// or targetMatch is more specific than the best match, make it
				// the new best match.
				if ( Types.isSignatureAssignable(
						idealMatch, targetMatch, round )
					&& ( (bestMatch == null) ||
						( Types.isSignatureAssignable( targetMatch, bestMatch,
								Types.JAVA_BASE_ASSIGNABLE ) &&
						!Types.areSignaturesEqual(targetMatch, bestMatch) )
						)
				)
				{
					bestMatch = targetMatch;
					bestMatchIndex = i;
				}
			}

			if ( bestMatch != null )
				return bestMatchIndex;
		}

		return -1;
	}

	static String accessorName( String getorset, String propName ) {
        return getorset
			+ String.valueOf(Character.toUpperCase(propName.charAt(0)))
			+ propName.substring(1);
	}

    public static boolean hasObjectPropertyGetter(
		Class clas, String propName )
	{
		String getterName = accessorName("get", propName );
		try {
			clas.getMethod( getterName, new Class [0] );
			return true;
		} catch ( NoSuchMethodException e ) { /* fall through */ }
		getterName = accessorName("is", propName );
		try {
			Method m = clas.getMethod( getterName, new Class [0] );
			return ( m.getReturnType() == Boolean.TYPE );
		} catch ( NoSuchMethodException e ) {
			return false;
		}
	}

    public static boolean hasObjectPropertySetter(
		Class clas, String propName )
	{
		String setterName = accessorName("set", propName );
		Method [] methods = clas.getMethods();

		// we don't know the right hand side of the assignment yet.
		// has at least one setter of the right name?
		for(int i=0; i<methods.length; i++)
			if ( methods[i].getName().equals( setterName ) )
				return true;
		return false;
	}

    public static Object getObjectProperty(
		Object obj, String propName )
        throws UtilEvalError, ReflectError
    {
        Object[] args = new Object[] { };

        Interpreter.debug("property access: ");
		Method method = null;

		Exception e1=null, e2=null;
		try {
			String accessorName = accessorName( "get", propName );
			method = resolveExpectedJavaMethod(
				null/*bcm*/, obj.getClass(), obj, accessorName, args, false );
		} catch ( Exception e ) {
			e1 = e;
		}
		if ( method == null )
			try {
				String accessorName = accessorName( "is", propName );
				method = resolveExpectedJavaMethod(
					null/*bcm*/, obj.getClass(), obj,
					accessorName, args, false );
				if ( method.getReturnType() != Boolean.TYPE )
					method = null;
			} catch ( Exception e ) {
				e2 = e;
			}
		if ( method == null )
			throw new ReflectError("Error in property getter: "
				+e1 + (e2!=null?" : "+e2:"") );

        try {
			return invokeMethod( method, obj, args );
        }
        catch(InvocationTargetException e)
        {
            throw new UtilEvalError("Property accessor threw exception: "
				+e.getTargetException() );
        }
    }

    public static void setObjectProperty(
		Object obj, String propName, Object value)
        throws ReflectError, UtilEvalError
    {    	
        String accessorName = accessorName( "set", propName );
        Object[] args = new Object[] { value };

        Interpreter.debug("property access: ");
        try {
			Method method = resolveExpectedJavaMethod(
				null/*bcm*/, obj.getClass(), obj, accessorName, args, false );
			invokeMethod( method, obj, args );
        }
        catch ( InvocationTargetException e )
        {
            throw new UtilEvalError("Property accessor threw exception: "
				+e.getTargetException() );
        }
    }

    /**
		Return a more human readable version of the type name.
		Specifically, array types are returned with postfix "[]" dimensions.
		e.g. return "int []" for integer array instead of "class [I" as
		would be returned by Class getName() in that case.
	*/
    public static String normalizeClassName(Class type)
    {
        if ( !type.isArray() )
            return type.getName();

        StringBuffer className = new StringBuffer();
        try {
            className.append( getArrayBaseType(type).getName() +" ");
            for(int i = 0; i < getArrayDimensions(type); i++)
                className.append("[]");
        } catch( ReflectError e ) { /*shouldn't happen*/ }

        return className.toString();
    }

	/**
		returns the dimensionality of the Class
		returns 0 if the Class is not an array class
	*/
    public static int getArrayDimensions(Class arrayClass)
    {
        if ( !arrayClass.isArray() )
            return 0;

        return arrayClass.getName().lastIndexOf('[') + 1;  // why so cute?
    }

    /**

		Returns the base type of an array Class.
    	throws ReflectError if the Class is not an array class.
	*/
    public static Class getArrayBaseType(Class arrayClass) throws ReflectError
    {
        if ( !arrayClass.isArray() )
            throw new ReflectError("The class is not an array.");

		return arrayClass.getComponentType();

    }

	/**
		A command may be implemented as a compiled Java class containing one or
		more static invoke() methods of the correct signature.  The invoke()
		methods must accept two additional leading arguments of the interpreter
		and callstack, respectively. e.g. invoke(interpreter, callstack, ... )
		This method adds the arguments and invokes the static method, returning
		the result.
	*/
	public static Object invokeCompiledCommand(
		Class commandClass, Object [] args, Interpreter interpreter,
		CallStack callstack )
		throws UtilEvalError
	{
        // add interpereter and namespace to args list
        Object[] invokeArgs = new Object[args.length + 2];
        invokeArgs[0] = interpreter;
        invokeArgs[1] = callstack;
        System.arraycopy( args, 0, invokeArgs, 2, args.length );
		BshClassManager bcm = interpreter.getClassManager();
		try {
        	return Reflect.invokeStaticMethod(
				bcm, commandClass, "invoke", invokeArgs );
		} catch ( InvocationTargetException e ) {
			throw new UtilEvalError(
				"Error in compiled command: "+e.getTargetException() );
		} catch ( ReflectError e ) {
			throw new UtilEvalError("Error invoking compiled command: "+e );
		}
	}

	private static void logInvokeMethod( 
		String msg, Method method, Object[] args )
	{
		if ( Interpreter.DEBUG )
		{
			Interpreter.debug( msg +method+" with args:" );
			for(int i=0; i<args.length; i++)
				Interpreter.debug(
					"args["+i+"] = "+args[i]
					+" type = "+args[i].getClass() );
		}
	}

	private static void checkFoundStaticMethod(
		Method method, boolean staticOnly, Class clas )
		throws UtilEvalError
	{
		// We're looking for a static method but found an instance method
		if ( method != null && staticOnly && !isStatic( method ) )
			throw new UtilEvalError(
				"Cannot reach instance method: "
				+ StringUtil.methodString(
					method.getName(), method.getParameterTypes() )
				+ " from static context: "+ clas.getName() );
	}

	private static ReflectError cantFindConstructor(
		Class clas, Class [] types )
	{
		if ( types.length == 0 )
			return new ReflectError(
				"Can't find default constructor for: "+clas);
		else
			return new ReflectError(
				"Can't find constructor: "
					+ StringUtil.methodString( clas.getName(), types )
					+" in class: "+ clas.getName() );
	}

	private static boolean isPublic( Class c ) {
		return Modifier.isPublic( c.getModifiers() );
	}
	private static boolean isPublic( Method m ) {
		return Modifier.isPublic( m.getModifiers() );
	}
	private static boolean isPublic( Constructor c ) {
		return Modifier.isPublic( c.getModifiers() );
	}
	private static boolean isStatic( Method m ) {
		return Modifier.isStatic( m.getModifiers() );
	}
}

