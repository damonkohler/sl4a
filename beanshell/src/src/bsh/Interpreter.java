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

import java.util.Vector;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
	The BeanShell script interpreter.

	An instance of Interpreter can be used to source scripts and evaluate 
	statements or expressions.  
	<p>
	Here are some examples:

	<p><blockquote><pre>
		Interpeter bsh = new Interpreter();

		// Evaluate statements and expressions
		bsh.eval("foo=Math.sin(0.5)");
		bsh.eval("bar=foo*5; bar=Math.cos(bar);");
		bsh.eval("for(i=0; i<10; i++) { print(\"hello\"); }");
		// same as above using java syntax and apis only
		bsh.eval("for(int i=0; i<10; i++) { System.out.println(\"hello\"); }");

		// Source from files or streams
		bsh.source("myscript.bsh");  // or bsh.eval("source(\"myscript.bsh\")");

		// Use set() and get() to pass objects in and out of variables
		bsh.set( "date", new Date() );
		Date date = (Date)bsh.get( "date" );
		// This would also work:
		Date date = (Date)bsh.eval( "date" );

		bsh.eval("year = date.getYear()");
		Integer year = (Integer)bsh.get("year");  // primitives use wrappers

		// With Java1.3+ scripts can implement arbitrary interfaces...
		// Script an awt event handler (or source it from a file, more likely)
		bsh.eval( "actionPerformed( e ) { print( e ); }");
		// Get a reference to the script object (implementing the interface)
		ActionListener scriptedHandler = 
			(ActionListener)bsh.eval("return (ActionListener)this");
		// Use the scripted event handler normally...
		new JButton.addActionListener( script );
	</pre></blockquote>
	<p>

	In the above examples we showed a single interpreter instance, however 
	you may wish to use many instances, depending on the application and how
	you structure your scripts.  Interpreter instances are very light weight
	to create, however if you are going to execute the same script repeatedly
	and require maximum performance you should consider scripting the code as 
	a method and invoking the scripted method each time on the same interpreter
	instance (using eval()). 
	<p>

	See the BeanShell User's Manual for more information.
*/
public class Interpreter 
	implements Runnable, ConsoleInterface,Serializable
{
	/* --- Begin static members --- */

	public static final String VERSION = "2.0b5";
	/*
		Debug utils are static so that they are reachable by code that doesn't
		necessarily have an interpreter reference (e.g. tracing in utils).
		In the future we may want to allow debug/trace to be turned on on
		a per interpreter basis, in which case we'll need to use the parent 
		reference in some way to determine the scope of the command that 
		turns it on or off.
	*/
    public static boolean DEBUG, TRACE, LOCALSCOPING;

	// This should be per instance
    transient static PrintStream debug;
	static String systemLineSeparator = "\n"; // default

	static { 
		staticInit();
	}

	/** Shared system object visible under bsh.system */
	static This sharedObject;

	/** 
		Strict Java mode 
		@see #setStrictJava( boolean )
	*/
	private boolean strictJava = false;

	/* --- End static members --- */

	/* --- Instance data --- */

	transient Parser parser;
    NameSpace globalNameSpace;
    transient Reader in;
    transient PrintStream out;
    transient PrintStream err;
    ConsoleInterface console; 

	/** If this interpeter is a child of another, the parent */
	Interpreter parent;

	/** The name of the file or other source that this interpreter is reading */
	String sourceFileInfo;

	/** by default in interactive mode System.exit() on EOF */
	private boolean exitOnEOF = true;

    protected boolean 
		evalOnly, 		// Interpreter has no input stream, use eval() only
		interactive;	// Interpreter has a user, print prompts, etc.

	/** Control the verbose printing of results for the show() command. */
	private boolean showResults;

	/* --- End instance data --- */

	/**
		The main constructor.
		All constructors should now pass through here.

		@param namespace If namespace is non-null then this interpreter's 
		root namespace will be set to the one provided.  If it is null a new 
		one will be created for it.
		@param parent The parent interpreter if this interpreter is a child 
			of another.  May be null.  Children share a BshClassManager with
			their parent instance.
		@param sourceFileInfo An informative string holding the filename 
		or other description of the source from which this interpreter is
		reading... used for debugging.  May be null.
	*/
    public Interpreter(
		Reader in, PrintStream out, PrintStream err, 
		boolean interactive, NameSpace namespace,
		Interpreter parent, String sourceFileInfo )
    {
		//System.out.println("New Interpreter: "+this +", sourcefile = "+sourceFileInfo );
		parser = new Parser( in );
		long t1=System.currentTimeMillis();
        this.in = in;
        this.out = out;
        this.err = err;
        this.interactive = interactive;
		debug = err;
		this.parent = parent;
		if ( parent != null )
			setStrictJava( parent.getStrictJava() );
		this.sourceFileInfo = sourceFileInfo;

		BshClassManager bcm = BshClassManager.createClassManager( this );
		if ( namespace == null )
        	this.globalNameSpace = new NameSpace( bcm, "global");
		else
			this.globalNameSpace = namespace;

		// now done in NameSpace automatically when root
		// The classes which are imported by default
		//globalNameSpace.loadDefaultImports();

		/* 
			Create the root "bsh" system object if it doesn't exist.
		*/
		if ( ! ( getu("bsh") instanceof bsh.This ) )
			initRootSystemObject();

		if ( interactive )
			loadRCFiles();

		long t2=System.currentTimeMillis();
		if ( Interpreter.DEBUG ) 
			Interpreter.debug("Time to initialize interpreter: "+(t2-t1));
    }

    public Interpreter(
		Reader in, PrintStream out, PrintStream err, 
		boolean interactive, NameSpace namespace)
    {
		this( in, out, err, interactive, namespace, null, null );
	}

    public Interpreter(
		Reader in, PrintStream out, PrintStream err, boolean interactive)
    {
        this(in, out, err, interactive, null);
    }

	/**
		Construct a new interactive interpreter attached to the specified 
		console using the specified parent namespace.
	*/
    public Interpreter(ConsoleInterface console, NameSpace globalNameSpace) {

        this( console.getIn(), console.getOut(), console.getErr(), 
			true, globalNameSpace );

		setConsole( console );
    }

	/**
		Construct a new interactive interpreter attached to the specified 
		console.
	*/
    public Interpreter(ConsoleInterface console) {
        this(console, null);
    }

	/**
		Create an interpreter for evaluation only.
	*/
    public Interpreter()
    {
		this( new StringReader(""), 
			System.out, System.err, false, null );
        evalOnly = true;
		setu( "bsh.evalOnly", Primitive.TRUE );
    }

	// End constructors

	/**
		Attach a console
		Note: this method is incomplete.
	*/
	public void setConsole( ConsoleInterface console ) {
		this.console = console;
		setu( "bsh.console", console );
		// redundant with constructor
		setOut( console.getOut() );
		setErr( console.getErr() );
		// need to set the input stream - reinit the parser?
	}

	private void initRootSystemObject() 
	{
		BshClassManager bcm = getClassManager();
		// bsh
		setu("bsh", new NameSpace( bcm, "Bsh Object" ).getThis( this ) );

		// init the static shared sharedObject if it's not there yet
		if ( sharedObject == null )
			sharedObject = new NameSpace( 
				bcm, "Bsh Shared System Object" ).getThis( this );
		// bsh.system
		setu( "bsh.system", sharedObject );
		setu( "bsh.shared", sharedObject ); // alias

		// bsh.help
		This helpText = new NameSpace( 
			bcm, "Bsh Command Help Text" ).getThis( this );
		setu( "bsh.help", helpText );

		// bsh.cwd
		try {
			setu( "bsh.cwd", System.getProperty("user.dir") );
		} catch ( SecurityException e ) { 
			// applets can't see sys props
			setu( "bsh.cwd", "." );
		}

		// bsh.interactive
		setu( "bsh.interactive", interactive ? Primitive.TRUE : Primitive.FALSE );
		// bsh.evalOnly
		setu( "bsh.evalOnly", evalOnly ? Primitive.TRUE : Primitive.FALSE );
	}

	/**
		Set the global namespace for this interpreter.
		<p>

		Note: This is here for completeness.  If you're using this a lot 
		it may be an indication that you are doing more work than you have 
		to.  For example, caching the interpreter instance rather than the 
		namespace should not add a significant overhead.  No state other 
		than the debug status is stored in the interpreter.
		<p>

		All features of the namespace can also be accessed using the 
		interpreter via eval() and the script variable 'this.namespace'
		(or global.namespace as necessary).
	*/
	public void setNameSpace( NameSpace globalNameSpace ) {
		this.globalNameSpace = globalNameSpace;
	}

	/**
		Get the global namespace of this interpreter.
		<p>

		Note: This is here for completeness.  If you're using this a lot 
		it may be an indication that you are doing more work than you have 
		to.  For example, caching the interpreter instance rather than the 
		namespace should not add a significant overhead.  No state other than 
		the debug status is stored in the interpreter.  
		<p>

		All features of the namespace can also be accessed using the 
		interpreter via eval() and the script variable 'this.namespace'
		(or global.namespace as necessary).
	*/
	public NameSpace getNameSpace() {
		return globalNameSpace;
	}

	/**
		Run the text only interpreter on the command line or specify a file.
	*/
    public static void main( String [] args ) 
	{
        if ( args.length > 0 ) {
			String filename = args[0];

			String [] bshArgs;
			if ( args.length > 1 ) {
				bshArgs = new String [ args.length -1 ];
				System.arraycopy( args, 1, bshArgs, 0, args.length-1 );
			} else
				bshArgs = new String [0];

            Interpreter interpreter = new Interpreter();
			//System.out.println("run i = "+interpreter);
			interpreter.setu( "bsh.args", bshArgs );
			try {
				Object result = 
					interpreter.source( filename, interpreter.globalNameSpace );
				if ( result instanceof Class )
					try {
						invokeMain( (Class)result, bshArgs );
					} catch ( Exception e ) 
					{
						Object o = e;
						if ( e instanceof InvocationTargetException )
							o = ((InvocationTargetException)e)
								.getTargetException();
						System.err.println(
							"Class: "+result+" main method threw exception:"+o);
					}
			} catch ( FileNotFoundException e ) {
				System.out.println("File not found: "+e);
			} catch ( TargetError e ) {
				System.out.println("Script threw exception: "+e);
				if ( e.inNativeCode() )
					e.printStackTrace( DEBUG, System.err );
			} catch ( EvalError e ) {
				System.out.println("Evaluation Error: "+e);
			} catch ( IOException e ) {
				System.out.println("I/O Error: "+e);
			}
        } else 
		{
			// Workaround for JDK bug 4071281, where system.in.available() 
			// returns too large a value. This bug has been fixed in JDK 1.2.
			InputStream src;
			if ( System.getProperty("os.name").startsWith("Windows") 
				&& System.getProperty("java.version").startsWith("1.1."))
			{
				src = new FilterInputStream(System.in) {
					public int available() throws IOException {
						return 0;
					}
				};
			}
			else
				src = System.in;

            Reader in = new CommandLineReader( new InputStreamReader(src));
            Interpreter interpreter = 
				new Interpreter( in, System.out, System.err, true );
        	interpreter.run();
        }
    }

	public static void invokeMain( Class clas, String [] args ) 
		throws Exception
	{
    	Method main = Reflect.resolveJavaMethod(
			null/*BshClassManager*/, clas, "main", 
			new Class [] { String [].class }, true/*onlyStatic*/ );
		if ( main != null )
			main.invoke( null, new Object [] { args } );
	}

	/**
		Run interactively.  (printing prompts, etc.)
	*/
    public void run() 
	{
        if(evalOnly)
            throw new RuntimeException("bsh Interpreter: No stream");

        /*
          We'll print our banner using eval(String) in order to
          exercise the parser and get the basic expression classes loaded...
          This ameliorates the delay after typing the first statement.
        */
        if ( interactive )
			try { 
				eval("printBanner();"); 
			} catch ( EvalError e ) {
				println(
					"BeanShell "+VERSION+" - by Pat Niemeyer (pat@pat.net)");
			}

		// init the callstack.  
		CallStack callstack = new CallStack( globalNameSpace );

        boolean eof = false;
        while( !eof )
        {
            try
            {
                // try to sync up the console
                System.out.flush();
                System.err.flush();
                Thread.yield();  // this helps a little

                if ( interactive )
                    print( getBshPrompt() );

                eof = Line();

                if( get_jjtree().nodeArity() > 0 )  // number of child nodes 
                {
                    SimpleNode node = (SimpleNode)(get_jjtree().rootNode());

                    if(DEBUG)
                        node.dump(">");

                    Object ret = node.eval( callstack, this );
				
					// sanity check during development
					if ( callstack.depth() > 1 )
						throw new InterpreterError(
							"Callstack growing: "+callstack);

                    if(ret instanceof ReturnControl)
                        ret = ((ReturnControl)ret).value;

                    if( ret != Primitive.VOID )
                    {
                        setu("$_", ret);
						if ( showResults )
                            println("<" + ret + ">");
                    }
                }
            }
            catch(ParseException e)
            {
                error("Parser Error: " + e.getMessage(DEBUG));
				if ( DEBUG )
                	e.printStackTrace();
                if(!interactive)
                    eof = true;

                parser.reInitInput(in);
            }
            catch(InterpreterError e)
            {
                error("Internal Error: " + e.getMessage());
                e.printStackTrace();
                if(!interactive)
                    eof = true;
            }
            catch(TargetError e)
            {
                error("// Uncaught Exception: " + e );
				if ( e.inNativeCode() )
					e.printStackTrace( DEBUG, err );
                if(!interactive)
                    eof = true;
				setu("$_e", e.getTarget());
            }
            catch (EvalError e)
            {
				if ( interactive )
					error( "EvalError: "+e.toString() );
				else
					error( "EvalError: "+e.getMessage() );

                if(DEBUG)
                    e.printStackTrace();

                if(!interactive)
                    eof = true;
            }
            catch(Exception e)
            {
                error("Unknown error: " + e);
				if ( DEBUG )
                	e.printStackTrace();
                if(!interactive)
                    eof = true;
            }
            catch(TokenMgrError e)
            {
				error("Error parsing input: " + e);

				/*
					We get stuck in infinite loops here when unicode escapes
					fail.  Must re-init the char stream reader 
					(ASCII_UCodeESC_CharStream.java)
				*/
				parser.reInitTokenInput( in );

                if(!interactive)
                    eof = true;
            }
            finally
            {
                get_jjtree().reset();
				// reinit the callstack
				if ( callstack.depth() > 1 ) {
					callstack.clear();
					callstack.push( globalNameSpace );
				}
            }
        }

		if ( interactive && exitOnEOF )
			System.exit(0);
    }

	// begin source and eval

	/**
		Read text from fileName and eval it.
	*/
    public Object source( String filename, NameSpace nameSpace ) 
		throws FileNotFoundException, IOException, EvalError 
	{
		File file = pathToFile( filename );
		if ( Interpreter.DEBUG ) debug("Sourcing file: "+file);
		Reader sourceIn = new BufferedReader( new FileReader(file) );
		try {
			return eval( sourceIn, nameSpace, filename );
		} finally {
			sourceIn.close();
		}
	}

	/**
		Read text from fileName and eval it.
		Convenience method.  Use the global namespace.
	*/
    public Object source( String filename ) 
		throws FileNotFoundException, IOException, EvalError 
	{
		return source( filename, globalNameSpace );
	}

    /**
        Spawn a non-interactive local interpreter to evaluate text in the 
		specified namespace.  

		Return value is the evaluated object (or corresponding primitive 
		wrapper).

		@param sourceFileInfo is for information purposes only.  It is used to
		display error messages (and in the future may be made available to
		the script).
		@throws EvalError on script problems
		@throws TargetError on unhandled exceptions from the script
    */
	/*
		Note: we need a form of eval that passes the callstack through...
	*/
	/*
	Can't this be combined with run() ?
	run seems to have stuff in it for interactive vs. non-interactive...
	compare them side by side and see what they do differently, aside from the
	exception handling.
	*/

    public Object eval( 
		Reader in, NameSpace nameSpace, String sourceFileInfo
			/*, CallStack callstack */ ) 
		throws EvalError 
	{
		Object retVal = null;
		if ( Interpreter.DEBUG ) debug("eval: nameSpace = "+nameSpace);

		/* 
			Create non-interactive local interpreter for this namespace
			with source from the input stream and out/err same as 
			this interpreter.
		*/
        Interpreter localInterpreter = 
			new Interpreter( 
				in, out, err, false, nameSpace, this, sourceFileInfo  );

		CallStack callstack = new CallStack( nameSpace );

        boolean eof = false;
        while(!eof)
        {
			SimpleNode node = null;
            try
            {
                eof = localInterpreter.Line();
                if (localInterpreter.get_jjtree().nodeArity() > 0)
                {
                    node = (SimpleNode)localInterpreter.get_jjtree().rootNode();
					// quick filter for when we're running as a compiler only
					if ( getSaveClasses()
						&& !(node instanceof BSHClassDeclaration)
						&& !(node instanceof BSHImportDeclaration )
						&& !(node instanceof BSHPackageDeclaration )
					)
						continue;

					// nodes remember from where they were sourced
					node.setSourceFile( sourceFileInfo );

					if ( TRACE )
						println( "// " +node.getText() );

                    retVal = node.eval( callstack, localInterpreter );

					// sanity check during development
					if ( callstack.depth() > 1 )
						throw new InterpreterError(
							"Callstack growing: "+callstack);

                    if ( retVal instanceof ReturnControl ) {
                        retVal = ((ReturnControl)retVal).value;
						break; // non-interactive, return control now
					}

					if ( localInterpreter.showResults 
						&& retVal != Primitive.VOID )
						println("<" + retVal + ">");
                }
            } catch(ParseException e) {
				/*
                throw new EvalError(
					"Sourced file: "+sourceFileInfo+" parser Error: " 
					+ e.getMessage( DEBUG ), node, callstack );
				*/
				if ( DEBUG )
					// show extra "expecting..." info
					error( e.getMessage(DEBUG) );

				// add the source file info and throw again
				e.setErrorSourceFile( sourceFileInfo );
				throw e;

            } catch ( InterpreterError e ) {
                e.printStackTrace();
                throw new EvalError(
					"Sourced file: "+sourceFileInfo+" internal Error: " 
					+ e.getMessage(), node, callstack);
            } catch ( TargetError e ) {
				// failsafe, set the Line as the origin of the error.
				if ( e.getNode()==null )
					e.setNode( node );
				e.reThrow("Sourced file: "+sourceFileInfo);
            } catch ( EvalError e) {
                if ( DEBUG)
                    e.printStackTrace();
				// failsafe, set the Line as the origin of the error.
				if ( e.getNode()==null )
					e.setNode( node );
				e.reThrow( "Sourced file: "+sourceFileInfo );
            } catch ( Exception e) {
                if ( DEBUG)
                	e.printStackTrace();
                throw new EvalError(
					"Sourced file: "+sourceFileInfo+" unknown error: " 
					+ e.getMessage(), node, callstack);
            } catch(TokenMgrError e) {
                throw new EvalError(
					"Sourced file: "+sourceFileInfo+" Token Parsing Error: " 
					+ e.getMessage(), node, callstack );
            } finally {
                localInterpreter.get_jjtree().reset();

				// reinit the callstack
				if ( callstack.depth() > 1 ) {
					callstack.clear();
					callstack.push( nameSpace );
				}
            }
        }
		return Primitive.unwrap( retVal );
    }

	/**
		Evaluate the inputstream in this interpreter's global namespace.
	*/
    public Object eval( Reader in ) throws EvalError 
	{
		return eval( in, globalNameSpace, "eval stream" );
	}

	/**
		Evaluate the string in this interpreter's global namespace.
	*/
    public Object eval( String statements ) throws EvalError {
		if ( Interpreter.DEBUG ) debug("eval(String): "+statements);
		return eval(statements, globalNameSpace);
	}

	/**
		Evaluate the string in the specified namespace.
	*/
    public Object eval( String statements, NameSpace nameSpace ) 
		throws EvalError 
	{

		String s = ( statements.endsWith(";") ? statements : statements+";" );
        return eval( 
			new StringReader(s), nameSpace, 
			"inline evaluation of: ``"+ showEvalString(s)+"''" );
    }

	private String showEvalString( String s ) {
		s = s.replace('\n', ' ');
		s = s.replace('\r', ' ');
		if ( s.length() > 80 )
			s = s.substring( 0, 80 ) + " . . . ";
		return s;
	}

	// end source and eval

	/**
		Print an error message in a standard format on the output stream
		associated with this interpreter. On the GUI console this will appear 
		in red, etc.
	*/
    public final void error( Object o ) {
		if ( console != null )
				console.error( "// Error: " + o +"\n" );
		else {
			err.println("// Error: " + o );
			err.flush();
		}
    }

	// ConsoleInterface
	// The interpreter reflexively implements the console interface that it 
	// uses.  Should clean this up by using an inner class to implement the
	// console for us.

	/** 
		Get the input stream associated with this interpreter.
		This may be be stdin or the GUI console.
	*/
	public Reader getIn() { return in; }

	/** 
		Get the outptut stream associated with this interpreter.
		This may be be stdout or the GUI console.
	*/
	public PrintStream getOut() { return out; }

	/** 
		Get the error output stream associated with this interpreter.
		This may be be stderr or the GUI console.
	*/
	public PrintStream getErr() { return err; }

    public final void println( Object o )
    {
        print( String.valueOf(o) + systemLineSeparator );
    }

    public final void print( Object o )
    {
		if (console != null) {
            console.print(o);
        } else {
            out.print(o);
            out.flush();
        }
    }

	// End ConsoleInterface

	/**
		Print a debug message on debug stream associated with this interpreter
		only if debugging is turned on.
	*/
    public final static void debug(String s)
    {
        if ( DEBUG )
            debug.println("// Debug: " + s);
    }

	/* 
		Primary interpreter set and get variable methods
		Note: These are squeltching errors... should they?
	*/

	/**
		Get the value of the name.
		name may be any value. e.g. a variable or field
	*/
    public Object get( String name ) throws EvalError
	{
		try {
			Object ret = globalNameSpace.get( name, this );
			return Primitive.unwrap( ret );
		} catch ( UtilEvalError e ) {
			throw e.toEvalError( SimpleNode.JAVACODE, new CallStack() );
		}
	}

	/**
		Unchecked get for internal use
	*/
    Object getu( String name ) {
		try { 
			return get( name );
		} catch ( EvalError e ) { 
			throw new InterpreterError("set: "+e);
		}
	}

	/**
		Assign the value to the name.	
		name may evaluate to anything assignable. e.g. a variable or field.
	*/
    public void set( String name, Object value )
		throws EvalError 
	{
		// map null to Primtive.NULL coming in...
		if ( value == null )
			value = Primitive.NULL;

		CallStack callstack = new CallStack();
		try {
			if ( Name.isCompound( name ) ) 
			{
				LHS lhs = globalNameSpace.getNameResolver( name ).toLHS( 
					callstack, this );
				lhs.assign( value, false );
			} else // optimization for common case
				globalNameSpace.setVariable( name, value, false );
		} catch ( UtilEvalError e ) { 
			throw e.toEvalError( SimpleNode.JAVACODE, callstack ); 
		}
	}

	/**
		Unchecked set for internal use
	*/
    void setu(String name, Object value) {
		try { 
			set(name, value);
		} catch ( EvalError e ) { 
			throw new InterpreterError("set: "+e);
		}
	}

    public void set(String name, long value) throws EvalError {
        set(name, new Primitive(value));
	}
    public void set(String name, int value) throws EvalError {
        set(name, new Primitive(value));
	}
    public void set(String name, double value) throws EvalError {
        set(name, new Primitive(value));
	}
    public void set(String name, float value) throws EvalError {
        set(name, new Primitive(value));
	}
    public void set(String name, boolean value) throws EvalError {
        set(name, value ? Primitive.TRUE : Primitive.FALSE);
	}

	/**
		Unassign the variable name.	
		Name should evaluate to a variable.
	*/
    public void unset( String name ) 
		throws EvalError 
	{
		/*
			We jump through some hoops here to handle arbitrary cases like
			unset("bsh.foo");
		*/
		CallStack callstack = new CallStack();
		try {
			LHS lhs = globalNameSpace.getNameResolver( name ).toLHS( 
				callstack, this );

			if ( lhs.type != LHS.VARIABLE )
				throw new EvalError("Can't unset, not a variable: "+name, 
					SimpleNode.JAVACODE, new CallStack() );

			//lhs.assign( null, false );
			lhs.nameSpace.unsetVariable( name );
		} catch ( UtilEvalError e ) {
			throw new EvalError( e.getMessage(), 
				SimpleNode.JAVACODE, new CallStack() );
		}
	}

	// end primary set and get methods

	/**
		Get a reference to the interpreter (global namespace), cast 
		to the specified interface type.  Assuming the appropriate 
		methods of the interface are defined in the interpreter, then you may 
		use this interface from Java, just like any other Java object.
		<p>

		For example:
		<pre>
			Interpreter interpreter = new Interpreter();
			// define a method called run()
			interpreter.eval("run() { ... }");
		
			// Fetch a reference to the interpreter as a Runnable
			Runnable runnable = 
				(Runnable)interpreter.getInterface( Runnable.class );
		</pre>
		<p>

		Note that the interpreter does *not* require that any or all of the
		methods of the interface be defined at the time the interface is
		generated.  However if you attempt to invoke one that is not defined
		you will get a runtime exception.
		<p>

		Note also that this convenience method has exactly the same effect as 
		evaluating the script:
		<pre>
			(Type)this;
		</pre>
		<p>

		For example, the following is identical to the previous example:
		<p>

		<pre>
			// Fetch a reference to the interpreter as a Runnable
			Runnable runnable = 
				(Runnable)interpreter.eval( "(Runnable)this" );
		</pre>
		<p>

		<em>Version requirement</em> Although standard Java interface types 
		are always available, to be used with arbitrary interfaces this 
		feature requires that you are using Java 1.3 or greater.
		<p>

		@throws EvalError if the interface cannot be generated because the
		version of Java does not support the proxy mechanism. 
	*/
	public Object getInterface( Class interf ) throws EvalError
	{
		try {
			return globalNameSpace.getThis( this ).getInterface( interf );
		} catch ( UtilEvalError e ) {
			throw e.toEvalError( SimpleNode.JAVACODE, new CallStack() );
		}
	}

	/*	Methods for interacting with Parser */

	private JJTParserState get_jjtree() {
		return parser.jjtree;
	}

  	private JavaCharStream get_jj_input_stream() {
		return parser.jj_input_stream;
	}

  	private boolean Line() throws ParseException {
		return parser.Line();
	}

	/*	End methods for interacting with Parser */

	void loadRCFiles() {
		try {
			String rcfile = 
				// Default is c:\windows under win98, $HOME under Unix
				System.getProperty("user.home") + File.separator + ".bshrc";
			source( rcfile, globalNameSpace );
		} catch ( Exception e ) { 
			// squeltch security exception, filenotfoundexception
			if ( Interpreter.DEBUG ) debug("Could not find rc file: "+e);
		}
	}

	/**
		Localize a path to the file name based on the bsh.cwd interpreter 
		working directory.
	*/
    public File pathToFile( String fileName ) 
		throws IOException
	{
		File file = new File( fileName );

		// if relative, fix up to bsh.cwd
		if ( !file.isAbsolute() ) {
			String cwd = (String)getu("bsh.cwd");
			file = new File( cwd + File.separator + fileName );
		}

		// The canonical file name is also absolute.
		// No need for getAbsolutePath() here...
		return new File( file.getCanonicalPath() );
	}

	public static void redirectOutputToFile( String filename ) 
	{
		try {
			PrintStream pout = new PrintStream( 
				new FileOutputStream( filename ) );
			System.setOut( pout );
			System.setErr( pout );
		} catch ( IOException e ) {
			System.err.println("Can't redirect output to file: "+filename );
		}
	}

	/**
		Set an external class loader to be used as the base classloader
		for BeanShell.  The base classloader is used for all classloading 
		unless/until the addClasspath()/setClasspath()/reloadClasses()  
		commands are called to modify the interpreter's classpath.  At that 
		time the new paths /updated paths are added on top of the base 
		classloader.
		<p>

		BeanShell will use this at the same point it would otherwise use the 
		plain Class.forName().
		i.e. if no explicit classpath management is done from the script
		(addClassPath(), setClassPath(), reloadClasses()) then BeanShell will
		only use the supplied classloader.  If additional classpath management
		is done then BeanShell will perform that in addition to the supplied
		external classloader.  
		However BeanShell is not currently able to reload
		classes supplied through the external classloader.
		<p>

		@see BshClassManager#setClassLoader( ClassLoader )
	*/
	public void setClassLoader( ClassLoader externalCL ) {
		getClassManager().setClassLoader( externalCL );
	}

	/**
		Get the class manager associated with this interpreter
		(the BshClassManager of this interpreter's global namespace).
		This is primarily a convenience method.
	*/
	public BshClassManager getClassManager() 
	{
		return getNameSpace().getClassManager();
	}
	
	/**
		Set strict Java mode on or off.  
		This mode attempts to make BeanShell syntax behave as Java
		syntax, eliminating conveniences like loose variables, etc.
		When enabled, variables are required to be declared or initialized 
		before use and method arguments are reqired to have types. 
		<p>

		This mode will become more strict in a future release when 
		classes are interpreted and there is an alternative to scripting
		objects as method closures.
	*/
	public void setStrictJava( boolean b ) { 
		this.strictJava = b; 
	}

	/**
		@see #setStrictJava( boolean )
	*/
	public boolean getStrictJava() { 
		return this.strictJava;
	}

	static void staticInit() 
	{
	/* 
		Apparently in some environments you can't catch the security exception
		at all...  e.g. as an applet in IE  ... will probably have to work 
		around 
	*/
		try {
			systemLineSeparator = System.getProperty("line.separator");
    		debug = System.err;
    		DEBUG = Boolean.getBoolean("debug");
    		TRACE = Boolean.getBoolean("trace");
    		LOCALSCOPING = Boolean.getBoolean("localscoping");
			String outfilename = System.getProperty("outfile");
			if ( outfilename != null )
				redirectOutputToFile( outfilename );
		} catch ( SecurityException e ) { 
			System.err.println("Could not init static:"+e);
		} catch ( Exception e ) {
			System.err.println("Could not init static(2):"+e);
		} catch ( Throwable e ) { 
			System.err.println("Could not init static(3):"+e);
		}
	}

	/**
		Specify the source of the text from which this interpreter is reading.
		Note: there is a difference between what file the interrpeter is 
		sourcing and from what file a method was originally parsed.  One
		file may call a method sourced from another file.  See SimpleNode
		for origination file info.
		@see bsh.SimpleNode#getSourceFile()
	*/
	public String getSourceFileInfo() { 
		if ( sourceFileInfo != null )
			return sourceFileInfo;
		else
			return "<unknown source>";
	}

	/**
		Get the parent Interpreter of this interpreter, if any.
		Currently this relationship implies the following:
			1) Parent and child share a BshClassManager
			2) Children indicate the parent's source file information in error
			reporting.
		When created as part of a source() / eval() the child also shares
		the parent's namespace.  But that is not necessary in general.
	*/
	public Interpreter getParent() {
		return parent;
	}

	public void setOut( PrintStream out ) {
		this.out = out;
	}
	public void setErr( PrintStream err ) {
		this.err = err;
	}

	/**
		De-serialization setup.
		Default out and err streams to stdout, stderr if they are null.
	*/
	private void readObject(ObjectInputStream stream) 
		throws java.io.IOException, ClassNotFoundException
	{
		stream.defaultReadObject();

		// set transient fields
		if ( console != null ) {
			setOut( console.getOut() );
			setErr( console.getErr() );
		} else {
			setOut( System.out );
			setErr( System.err );
		}
	}

	/**
		Get the prompt string defined by the getBshPrompt() method in the
		global namespace.  This may be from the getBshPrompt() command or may
		be defined by the user as with any other method.
		Defaults to "bsh % " if the method is not defined or there is an error.
	*/
	private String getBshPrompt() 
	{
		try {
			return (String)eval("getBshPrompt()");
		} catch ( Exception e ) {
			return "bsh % ";
		}
	}

	/**
		Specify whether, in interactive mode, the interpreter exits Java upon
		end of input.  If true, when in interactive mode the interpreter will
		issue a System.exit(0) upon eof.  If false the interpreter no
		System.exit() will be done.
		<p/>
		Note: if you wish to cause an EOF externally you can try closing the
		input stream.  This is not guaranteed to work in older versions of Java
		due to Java limitations, but should work in newer JDK/JREs.  (That was
		the motivation for the Java NIO package).
	*/
	public void setExitOnEOF( boolean value ) {
		exitOnEOF = value; // ug
	}

	/**
		Turn on/off the verbose printing of results as for the show()
		 command.
	 	If this interpreter has a parent the call is delegated.
	 	See the BeanShell show() command.
	*/
	public void setShowResults( boolean showResults ) {
		this.showResults = showResults;
	}
	/**
	 Show on/off verbose printing status for the show() command.
	 See the BeanShell show() command.
	 If this interpreter has a parent the call is delegated.
	 */
	public boolean getShowResults()  {
		return showResults;
	}

	public static String getSaveClassesDir() {
		return System.getProperty("saveClasses");
	}

	public static boolean getSaveClasses()  {
		return getSaveClassesDir() != null;
	}
}

