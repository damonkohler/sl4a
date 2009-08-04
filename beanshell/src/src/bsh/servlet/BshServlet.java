package bsh.servlet;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import bsh.*;

/**
	This file is part of BeanShell - www.beanshell.org
	
	@author Pat Niemeyer
*/
public class BshServlet extends HttpServlet 
{
	static String bshVersion;
	static String exampleScript = "print(\"hello!\");";

	static String getBshVersion() 
	{
		if ( bshVersion != null )
			return bshVersion;

		/*
			We have included a getVersion() command to detect the version
			of bsh.  If bsh is packaged in the WAR file it could access it
			directly as a bsh command.  But if bsh is in the app server's
			classpath it won't see it here, so we will source it directly.

			This command works around the lack of a coherent version number
			in the early versions.
		*/
		Interpreter bsh = new Interpreter();
		try {
			bsh.eval( new InputStreamReader( BshServlet.class.getResource(
				"getVersion.bsh").openStream() ) );
			bshVersion = (String)bsh.eval( "getVersion()" ); 
		} catch ( Exception e ) {
			bshVersion = "BeanShell: unknown version";
		}

		return bshVersion;
	}

    public void doGet(
		HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException 
	{
		String script = request.getParameter("bsh.script");
		String client = request.getParameter("bsh.client");
		String output = request.getParameter("bsh.servlet.output");
		String captureOutErr = 
			request.getParameter("bsh.servlet.captureOutErr");
		boolean capture = false;
		if ( captureOutErr != null && captureOutErr.equalsIgnoreCase("true") )
			capture = true;

		Object scriptResult = null;
		Exception scriptError = null;
		StringBuffer scriptOutput = new StringBuffer();
		if ( script != null ) {
			try {
				scriptResult = evalScript( 
					script, scriptOutput, capture, request, response );
			} catch ( Exception e ) {
				scriptError = e;
			}
		}

		response.setHeader( "Bsh-Return", String.valueOf(scriptResult) );

		if ( (output != null && output.equalsIgnoreCase("raw"))
				|| ( client != null && client.equals("Remote") ) )
			sendRaw( 
				request, response, scriptError, scriptResult, scriptOutput );
		else
			sendHTML( request, response, script, scriptError, 
				scriptResult, scriptOutput, capture );
	}

	void sendHTML( 
		HttpServletRequest request, HttpServletResponse response,
		String script, Exception scriptError, Object scriptResult, 
		StringBuffer scriptOutput, boolean capture )
		throws IOException
	{
		// Format the output using a simple templating utility
		SimpleTemplate st = new SimpleTemplate( 
			BshServlet.class.getResource("page.template") );
		st.replace( "version", getBshVersion() );

		//String requestURI = HttpUtils.getRequestURL( request ).toString() 
		// I was told this should work
		String requestURI = request.getRequestURI();

		st.replace( "servletURL", requestURI );
		if ( script != null )
			st.replace( "script", script );
		else
			st.replace( "script", exampleScript );
		if ( capture )
			st.replace( "captureOutErr", "CHECKED" );
		else
			st.replace( "captureOutErr", "" );
		if ( script != null )
			st.replace( "scriptResult", 
				formatScriptResultHTML( 
					script, scriptResult, scriptError, scriptOutput ) );

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
		st.write(out);
		out.flush();
    }

	void sendRaw( 
		HttpServletRequest request, HttpServletResponse response,
		Exception scriptError, Object scriptResult, StringBuffer scriptOutput )
		throws IOException
	{
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
		if ( scriptError != null )
			out.println( "Script Error:\n"+scriptError );
		else
			out.println( scriptOutput.toString() );
		out.flush();
	}

	/**
	*/
	String formatScriptResultHTML( 
		String script, Object result, Exception error, 
		StringBuffer scriptOutput ) 
		throws IOException
	{
		SimpleTemplate tmplt;

		if ( error != null ) 
		{
			tmplt = new SimpleTemplate( 
				getClass().getResource("error.template") );

			String errString;

			if ( error instanceof bsh.EvalError )
			{
				int lineNo = ((EvalError)error).getErrorLineNumber();
				String msg = error.getMessage();
				int contextLines = 4;
				errString = escape(msg);
				if ( lineNo > -1 )
					errString += "<hr>" 
						+ showScriptContextHTML( script, lineNo, contextLines );
			} else
				errString = escape( error.toString() );
		
			tmplt.replace("error", errString );
		} else {
			tmplt = new SimpleTemplate( 
				getClass().getResource("result.template") );
			tmplt.replace( "value", escape(String.valueOf(result)) );
			tmplt.replace( "output", escape(scriptOutput.toString()) );
		}

		return tmplt.toString();
	}

	/*
		Show context number lines of string before and after target line.
		Add HTML formatting to bold the target line.
	*/
	String showScriptContextHTML( String s, int lineNo, int context ) 
	{
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader( new StringReader(s) );

		int beginLine = Math.max( 1, lineNo-context );
		int endLine = lineNo + context;
		for( int i=1; i<=lineNo+context+1; i++ )
		{
			if ( i < beginLine )
			{
				try { 
					br.readLine();
				} catch ( IOException e ) { 
					throw new RuntimeException( e.toString() );
				}
				continue;
			}
			if ( i > endLine )
				break;

			String line;
			try { 
				line = br.readLine();
			} catch ( IOException e ) { 
				throw new RuntimeException( e.toString() );
			}

			if ( line == null ) 
				break;
			if ( i == lineNo )
				sb.append( "<font color=\"red\">"+i+": "+line +"</font><br/>" );
			else
				sb.append( i+": " +line +"<br/>" );
		}

		return sb.toString();
	}
    public void doPost(
		HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException 
	{	
		doGet( request, response );
	}

	Object evalScript( 
		String script, StringBuffer scriptOutput, boolean captureOutErr,
		HttpServletRequest request, HttpServletResponse response )
		throws EvalError
	{
		// Create a PrintStream to capture output
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream pout = new PrintStream( baos );

		// Create an interpreter instance with a null inputstream,
		// the capture out/err stream, non-interactive 
		Interpreter bsh = new Interpreter( null, pout, pout, false );

		// set up interpreter
		bsh.set( "bsh.httpServletRequest", request );
		bsh.set( "bsh.httpServletResponse", response );
		
		// Eval the text, gathering the return value or any error.
		Object result = null;
		String error = null;
		PrintStream sout = System.out;
		PrintStream serr = System.err;
		if ( captureOutErr ) {
			System.setOut( pout );
			System.setErr( pout );
		}
		try { 
			// Eval the user text
			result = bsh.eval( script );
		} finally {
			if ( captureOutErr ) {
				System.setOut( sout );
				System.setErr( serr );
			}
		}
		pout.flush();
		scriptOutput.append( baos.toString() );
		return result;
	}

    /**
    * Convert special characters to entities for XML output
    */
    public static String escape(String value) 
	{
		String search =	"&<>";
		String[] replace = {"&amp;", "&lt;", "&gt;"};

		StringBuffer buf = new StringBuffer();
	
		for (int i = 0;	i < value.length(); i++) 
		{
	    	char c = value.charAt(i);
	    	int	pos = search.indexOf(c);
	    	if (pos < 0)
				buf.append(c);
	    	else
				buf.append(replace[pos]);
		}

		return buf.toString();
    }

}


