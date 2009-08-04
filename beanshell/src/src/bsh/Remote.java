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

import java.io.*;
import java.net.*;
import java.text.*;
/**
	Remote executor class. Posts a script from the command line to a BshServlet
 	or embedded  interpreter using (respectively) HTTP or the bsh telnet
	service. Output is printed to stdout and a numeric return value is scraped
	from the result.
*/
public class Remote
{
    public static void main( String args[] )
		throws Exception
	{
		if ( args.length < 2 ) {
			System.out.println(
				"usage: Remote URL(http|bsh) file [ file ] ... ");
			System.exit(1);
		}
		String url = args[0];
		String text = getFile(args[1]);
		int ret = eval( url, text );
		System.exit( ret );
		}

	/**
		Evaluate text in the interpreter at url, returning a possible integer
	 	return value.
	*/
	public static int eval( String url, String text )
		throws IOException
	{
		String returnValue = null;
		if ( url.startsWith( "http:" ) ) {
			returnValue = doHttp( url, text );
		} else if ( url.startsWith( "bsh:" ) ) {
			returnValue = doBsh( url, text );
		} else
			throw new IOException( "Unrecognized URL type."
				+"Scheme must be http:// or bsh://");

		try {
			return Integer.parseInt( returnValue );
		} catch ( Exception e ) {
			// this convention may change...
			return 0;
		}
	}

	static String doBsh( String url, String text ) 
	{ 
	    OutputStream out;
	    InputStream in;
	    String host = "";
	    String port = "";
	    String returnValue = "-1";
		String orgURL = url;
	    
		// Need some format checking here
	    try {
			url = url.substring(6); // remove the bsh://
			// get the index of the : between the host and the port is located
			int index = url.indexOf(":");
			host = url.substring(0,index);
			port = url.substring(index+1,url.length());
		} catch ( Exception ex ) {
			System.err.println("Bad URL: "+orgURL+": "+ex  );
			return returnValue;
	    }

	    try {
			System.out.println("Connecting to host : " 
				+ host + " at port : " + port);
			Socket s = new Socket(host, Integer.parseInt(port) + 1);
			
			out = s.getOutputStream();
			in = s.getInputStream();
			
			sendLine( text, out );

			BufferedReader bin = new BufferedReader( 
				new InputStreamReader(in));
			  String line;
			  while ( (line=bin.readLine()) != null )
				System.out.println( line );

			// Need to scrape a value from the last line?
			returnValue="1";
			return returnValue;
	    } catch(Exception ex) {
			System.err.println("Error communicating with server: "+ex);
			return returnValue;
	    }
	}

    private static void sendLine( String line, OutputStream outPipe )
		throws IOException
	{
		outPipe.write( line.getBytes() );
		outPipe.flush();
    }


	/*
		TODO: this is not unicode friendly, nor is getFile()
		The output is urlencoded 8859_1 text.
		should probably be urlencoded UTF-8... how does the servlet determine
		the encoded charset?  I guess we're supposed to add a ";charset" clause
		to the content type?
	*/
	static String doHttp( String postURL, String text )
	{
		String returnValue = null;
		StringBuffer sb = new StringBuffer();
		sb.append( "bsh.client=Remote" );
		sb.append( "&bsh.script=" );
		sb.append( URLEncoder.encode( text ) );
		/*
		// This requires Java 1.3
		try {
			sb.append( URLEncoder.encode( text, "8859_1" ) );
		} catch ( UnsupportedEncodingException e ) {
			e.printStackTrace();
		}
		*/
		String formData = sb.toString(  );

		try {
		  URL url = new URL( postURL );
		  HttpURLConnection urlcon =
			  (HttpURLConnection) url.openConnection(  );
		  urlcon.setRequestMethod("POST");
		  urlcon.setRequestProperty("Content-type",
			  "application/x-www-form-urlencoded");
		  urlcon.setDoOutput(true);
		  urlcon.setDoInput(true);
		  PrintWriter pout = new PrintWriter( new OutputStreamWriter(
			  urlcon.getOutputStream(), "8859_1"), true );
		  pout.print( formData );
		  pout.flush();

		  // read results...
		  int rc = urlcon.getResponseCode();
		  if ( rc != HttpURLConnection.HTTP_OK )
			System.out.println("Error, HTTP response: "+rc );

		  returnValue = urlcon.getHeaderField("Bsh-Return");

		  BufferedReader bin = new BufferedReader(
			new InputStreamReader( urlcon.getInputStream() ) );
		  String line;
		  while ( (line=bin.readLine()) != null )
			System.out.println( line );

		  System.out.println( "Return Value: "+returnValue );

		} catch (MalformedURLException e) {
		  System.out.println(e);     // bad postURL
		} catch (IOException e2) {
		  System.out.println(e2);    // I/O error
		}

		return returnValue;
	}

	/*
		Note: assumes default character encoding
	*/
	static String getFile( String name )
		throws FileNotFoundException, IOException
	{
		StringBuffer sb = new StringBuffer();
		BufferedReader bin = new BufferedReader( new FileReader( name ) );
		String line;
		while ( (line=bin.readLine()) != null )
			sb.append( line ).append( "\n" );
		return sb.toString();
	}

}
