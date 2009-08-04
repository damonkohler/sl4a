package bsh.util;

/*
	This file is associated with the BeanShell Java Scripting language 
	distribution (http://www.beanshell.org/).

	This file is hereby placed into the public domain...  You may copy,
	modify, and redistribute it without restriction.
*/

import java.util.Vector;
import org.apache.bsf.*;
import org.apache.bsf.util.*;
import bsh.Interpreter;
import bsh.InterpreterError;
import bsh.EvalError;
import bsh.TargetError;
import bsh.Primitive;

/**
	This is the BeanShell adapter for IBM's Bean Scripting Famework.
	It is an implementation of the BSFEngine class, allowing BSF aware
	applications to use BeanShell as a scripting language.
	<p>

	I believe this implementation is complete (with some hesitation about the
	the usefullness of the compileXXX() style methods - provided by the base
	utility class).
	<p/>

	@author Pat Niemeyer
*/
public class BeanShellBSFEngine extends BSFEngineImpl
{
	Interpreter interpreter;
	boolean installedApplyMethod;

	public void initialize ( BSFManager mgr, String lang, Vector declaredBeans) 
	throws BSFException
	{
		super.initialize( mgr, lang, declaredBeans );

		interpreter = new Interpreter();

		// declare the bsf manager for callbacks, etc.
		try {
			interpreter.set( "bsf", mgr );
		} catch ( EvalError e ) {
			throw new BSFException("bsh internal error: "+e.toString()); 
		}

		for(int i=0; i<declaredBeans.size(); i++) 
		{
			BSFDeclaredBean bean = (BSFDeclaredBean)declaredBeans.get(i);
			declareBean( bean );
		}
	}

	public void setDebug (boolean debug) 
	{
		interpreter.DEBUG=debug;
	}

	/**
		Invoke method name on the specified bsh scripted object.
		The object may be null to indicate the global namespace of the 
		interpreter.
		@param object may be null for the global namespace.
	*/
	public Object call( Object object, String name, Object[] args )
		throws BSFException
	{
		/*
			If object is null use the interpreter's global scope.
		*/
		if ( object == null )
			try {
				object = interpreter.get("global");
			} catch ( EvalError e ) { 
				throw new BSFException("bsh internal error: "+e.toString()); 
			}

		if ( object instanceof bsh.This )
			try 
			{
				Object value = ((bsh.This)object).invokeMethod( name, args );
				return Primitive.unwrap( value );
			} catch ( InterpreterError e ) 
			{
				throw new BSFException(
					"BeanShell interpreter internal error: "+e );
			} catch ( TargetError e2 ) 
			{
				throw new BSFException(
					"The application script threw an exception: "
					+ e2.getTarget() );
			} catch ( EvalError e3 ) 
			{
				throw new BSFException( "BeanShell script error: "+e3 );
			}
		else
			throw new BSFException(
				"Cannot invoke method: "+name
				+". Object: "+object +" is not a BeanShell scripted object.");
	}


	/**
		A helper BeanShell method that implements the anonymous method apply
		proposed by BSF.  Note that the script below could use the standard
		bsh eval() method to set the variables and apply the text, however 
		then I'd have to escape quotes, etc.
	*/
	final static String bsfApplyMethod =
		"_bsfApply( _bsfNames, _bsfArgs, _bsfText ) {"
			+"for(i=0;i<_bsfNames.length;i++)"
				+"this.namespace.setVariable(_bsfNames[i], _bsfArgs[i],false);"
			+"return this.interpreter.eval(_bsfText, this.namespace);"
		+"}";

	/**
		This is an implementation of the BSF apply() method.
		It exectutes the funcBody text in an "anonymous" method call with
		arguments.
	*/
	/*
		Note: the apply() method may be supported directly in BeanShell in an
		upcoming release and would not require special support here.
	*/
	public Object apply (
		String source, int lineNo, int columnNo, Object funcBody, 
		Vector namesVec, Vector argsVec )
       throws BSFException 
	{
		if ( namesVec.size() != argsVec.size() )
			throw new BSFException("number of params/names mismatch");
		if ( !(funcBody instanceof String) )
			throw new BSFException("apply: functino body must be a string");

		String [] names = new String [ namesVec.size() ];
		namesVec.copyInto(names);
		Object [] args = new Object [ argsVec.size() ];
		argsVec.copyInto(args);

		try 
		{
			if ( !installedApplyMethod ) 
			{
				interpreter.eval( bsfApplyMethod );
				installedApplyMethod = true;
			}

			bsh.This global = (bsh.This)interpreter.get("global");
			Object value = global.invokeMethod( 
				"_bsfApply", new Object [] { names, args, (String)funcBody } );
			return Primitive.unwrap( value );

		} catch ( InterpreterError e ) 
		{
			throw new BSFException(
				"BeanShell interpreter internal error: "+e
				+ sourceInfo(source,lineNo,columnNo) );
		} catch ( TargetError e2 ) 
		{
			throw new BSFException(
				"The application script threw an exception: "
				+ e2.getTarget()
				+ sourceInfo(source,lineNo,columnNo) );
		} catch ( EvalError e3 ) 
		{
			throw new BSFException(
				"BeanShell script error: "+e3
				+ sourceInfo(source,lineNo,columnNo) );
		}
	}

	public Object eval (
		String source, int lineNo, int columnNo, Object expr) 
		throws BSFException 
	{
		if ( ! (expr instanceof String) )
			throw new BSFException("BeanShell expression must be a string");

		try {
			return interpreter.eval( ((String)expr) );
		} catch ( InterpreterError e ) 
		{
			throw new BSFException(
				"BeanShell interpreter internal error: "+e
				+ sourceInfo(source,lineNo,columnNo) );
		} catch ( TargetError e2 ) 
		{
			throw new BSFException(
				"The application script threw an exception: "
				+ e2.getTarget()
				+ sourceInfo(source,lineNo,columnNo) );
		} catch ( EvalError e3 ) 
		{
			throw new BSFException(
				"BeanShell script error: "+e3
				+ sourceInfo(source,lineNo,columnNo) );
		}
	}


	public void exec (String source, int lineNo, int columnNo, Object script) 
		throws BSFException
	{
		eval( source, lineNo, columnNo, script );
	}


/*
	I don't quite understand these compile methods.  The default impl
	will use the CodeBuffer utility to produce an example (Test) class that 
	turns around and invokes the BSF Manager to call the script again.
	
	I assume a statically compiled language would return a real implementation
	class adapter here?  But in source code form?  Would't it be more likely
	to generate bytecode?
	
	And shouldn't a non-compiled language simply return a standard
	precompiled adapter to itself?  The indirection of building a source
	class to call the scripting engine (possibly through the interpreter)
	seems kind of silly.
*/
/* 
	public void compileApply (String source, int lineNo, int columnNo,
		Object funcBody, Vector paramNames, Vector arguments, CodeBuffer cb)
		throws BSFException;

	public void compileExpr (String source, int lineNo, int columnNo,
		Object expr, CodeBuffer cb) throws BSFException;

	public void compileScript (String source, int	lineNo,	int columnNo,
		Object script, CodeBuffer cb) throws BSFException;
*/

	public void declareBean (BSFDeclaredBean bean) 
		throws BSFException 
	{
		try {
			interpreter.set( bean.name, bean.bean);
		} catch ( EvalError e ) { 
			throw new BSFException( "error declaring bean: "+bean.name
			+" : "+e.toString() ); 
		}
	}

	public void undeclareBean (BSFDeclaredBean bean) 
		throws BSFException
	{
		try {
			interpreter.unset( bean.name );
		} catch ( EvalError e ) {
			throw new BSFException("bsh internal error: "+e.toString()); 
		}
	}

	public void terminate () { }


	private String sourceInfo( String source, int lineNo, int columnNo ) 
	{
		return 	" BSF info: "+source+" at line: "+lineNo +" column: columnNo";
	}

}
