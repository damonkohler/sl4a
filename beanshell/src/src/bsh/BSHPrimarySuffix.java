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
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

class BSHPrimarySuffix extends SimpleNode
{
	public static final int
		CLASS = 0,
		INDEX = 1,
		NAME = 2,
		PROPERTY = 3;

	public int operation;
	Object index;
	public String field;

	BSHPrimarySuffix(int id) { super(id); }

	/*
		Perform a suffix operation on the given object and return the 
		new value.
		<p>

		obj will be a Node when suffix evaluation begins, allowing us to
		interpret it contextually. (e.g. for .class) Thereafter it will be 
		an value object or LHS (as determined by toLHS).
		<p>
		
		We must handle the toLHS case at each point here.
		<p>
	*/
	public Object doSuffix(
		Object obj, boolean toLHS, 
		CallStack callstack, Interpreter interpreter) 
		throws EvalError
	{
		// Handle ".class" suffix operation
		// Prefix must be a BSHType
		if ( operation == CLASS )
			if ( obj instanceof BSHType ) {
				if ( toLHS )
					throw new EvalError("Can't assign .class", 
						this, callstack );
				NameSpace namespace = callstack.top();
				return ((BSHType)obj).getType( callstack, interpreter );
			} else
				throw new EvalError(
					"Attempt to use .class suffix on non class.", 
					this, callstack );

		/*
			Evaluate our prefix if it needs evaluating first.
			If this is the first evaluation our prefix mayb be a Node 
			(directly from the PrimaryPrefix) - eval() it to an object.  
			If it's an LHS, resolve to a value.

			Note: The ambiguous name construct is now necessary where the node 
			may be an ambiguous name.  If this becomes common we might want to 
			make a static method nodeToObject() or something.  The point is 
			that we can't just eval() - we need to direct the evaluation to 
			the context sensitive type of result; namely object, class, etc.
		*/
		if ( obj instanceof SimpleNode )
			if ( obj instanceof BSHAmbiguousName )
				obj = ((BSHAmbiguousName)obj).toObject(callstack, interpreter);
			else
				obj = ((SimpleNode)obj).eval(callstack, interpreter);	
		else
			if ( obj instanceof LHS )
				try {
					obj = ((LHS)obj).getValue();
				} catch ( UtilEvalError e ) {
					throw e.toEvalError( this, callstack );
				}

		try
		{
			switch(operation)
			{
				case INDEX:
					return doIndex( obj, toLHS, callstack, interpreter );

				case NAME:
					return doName( obj, toLHS, callstack, interpreter );

				case PROPERTY:
					return doProperty( toLHS, obj, callstack, interpreter );

				default:
					throw new InterpreterError( "Unknown suffix type" );
			} 
		}
		catch(ReflectError e)
		{
			throw new EvalError("reflection error: " + e, this, callstack );
		}
		catch(InvocationTargetException e)
		{
			throw new TargetError( "target exception", e.getTargetException(), 
				this, callstack, true);
		}
	}

	/*
		Field access, .length on array, or a method invocation
		Must handle toLHS case for each.
	*/
	private Object doName(
		Object obj, boolean toLHS, 
		CallStack callstack, Interpreter interpreter) 
		throws EvalError, ReflectError, InvocationTargetException
	{
		try {
			// .length on array
			if ( field.equals("length") && obj.getClass().isArray() )
				if ( toLHS )
					throw new EvalError(
						"Can't assign array length", this, callstack );
				else
					return new Primitive(Array.getLength(obj));
			
			// field access
			if ( jjtGetNumChildren() == 0 ) 
				if ( toLHS )
					return Reflect.getLHSObjectField(obj, field);
				else
					return Reflect.getObjectFieldValue( obj, field );

			// Method invocation
			// (LHS or non LHS evaluation can both encounter method calls)
			Object[] oa = ((BSHArguments)jjtGetChild(0)).getArguments(
				callstack, interpreter);

		// TODO:
		// Note: this try/catch block is copied from BSHMethodInvocation
		// we need to factor out this common functionality and make sure
		// we handle all cases ... (e.g. property style access, etc.)
		// maybe move this to Reflect ?
			try {
				return Reflect.invokeObjectMethod( 
					obj, field, oa, interpreter, callstack, this );
			} catch ( ReflectError e ) {
				throw new EvalError(
					"Error in method invocation: " + e.getMessage(), 
					this, callstack );
			} catch ( InvocationTargetException e ) 
			{
				String msg = "Method Invocation "+field;
				Throwable te = e.getTargetException();

				/*
					Try to squeltch the native code stack trace if the exception
					was caused by a reflective call back into the bsh interpreter
					(e.g. eval() or source()
				*/
				boolean isNative = true;
				if ( te instanceof EvalError ) 
					if ( te instanceof TargetError )
						isNative = ((TargetError)te).inNativeCode();
					else
						isNative = false;
				
				throw new TargetError( msg, te, this, callstack, isNative );
			} 

		} catch ( UtilEvalError e ) {
			throw e.toEvalError( this, callstack );
		}
	}

	/**
	*/
	static int getIndexAux(
		Object obj, CallStack callstack, Interpreter interpreter, 
		SimpleNode callerInfo ) 
		throws EvalError
	{
		if ( !obj.getClass().isArray() )
			throw new EvalError("Not an array", callerInfo, callstack );

		int index;
		try {
			Object indexVal = 
				((SimpleNode)callerInfo.jjtGetChild(0)).eval( 
					callstack, interpreter );
			if ( !(indexVal instanceof Primitive) )
				indexVal = Types.castObject(
					indexVal, Integer.TYPE, Types.ASSIGNMENT );
			index = ((Primitive)indexVal).intValue();
		} catch( UtilEvalError e ) {
			Interpreter.debug("doIndex: "+e);
			throw e.toEvalError( 
				"Arrays may only be indexed by integer types.", 
				callerInfo, callstack );
		}

		return index;
	}

	/**
		array index.
		Must handle toLHS case.
	*/
	private Object doIndex( 
		Object obj, boolean toLHS, 
		CallStack callstack, Interpreter interpreter ) 
		throws EvalError, ReflectError
	{
		int index = getIndexAux( obj, callstack, interpreter, this );
		if ( toLHS )
			return new LHS(obj, index);
		else
			try {
				return Reflect.getIndex(obj, index);
			} catch ( UtilEvalError e ) {
				throw e.toEvalError( this, callstack );
			}
	}

	/**
		Property access.
		Must handle toLHS case.
	*/
	private Object doProperty( boolean toLHS,
		Object obj, CallStack callstack, Interpreter interpreter ) 
		throws EvalError
	{
		if(obj == Primitive.VOID)
			throw new EvalError( 
			"Attempt to access property on undefined variable or class name", 
				this, callstack );

		if ( obj instanceof Primitive )
			throw new EvalError("Attempt to access property on a primitive", 
				this, callstack );

		Object value = ((SimpleNode)jjtGetChild(0)).eval(
			callstack, interpreter);

		if ( !( value instanceof String ) )
			throw new EvalError(
				"Property expression must be a String or identifier.", 
				this, callstack );

		if ( toLHS )
			return new LHS(obj, (String)value);

		// Property style access to Hashtable or Map
		CollectionManager cm = CollectionManager.getCollectionManager();
		if ( cm.isMap( obj ) )
		{
			Object val = cm.getFromMap( obj, value/*key*/ );
			return ( val == null ?  val = Primitive.NULL : val );
		}

		try {
			return Reflect.getObjectProperty( obj, (String)value );
		}
		catch ( UtilEvalError e)  
		{
			throw e.toEvalError( "Property: "+value, this, callstack );
		}
		catch (ReflectError e) 
		{
			throw new EvalError("No such property: " + value, this, callstack );
		}
	}
}

