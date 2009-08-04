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

package bsh.util;

import java.io.*;
import java.util.StringTokenizer;
import java.net.Socket;
import java.net.ServerSocket;

/**
	A very simple httpd that supports the remote server mode.
	Files are loaded relative to the classpath (as resources).

	Warning: this is not secure!  This server can probably be duped into 
	serving any file on your system!  Beware!

	Note: at some point this should be recast as a beanshell script.
*/
public class Httpd extends Thread
{
	ServerSocket ss;

	public static void main(String argv[]) throws IOException
	{
		new Httpd(Integer.parseInt(argv[0])).start();
	}

	public Httpd(int port) throws IOException
	{
		ss = new ServerSocket(port);
	}

	public void run()
	{
//		System.out.println("starting httpd...");
		try
		{
			while(true)
				new HttpdConnection(ss.accept()).start();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}
}

class HttpdConnection extends Thread
{
	Socket client;
	BufferedReader in;
	OutputStream out;
	PrintStream pout;
	boolean isHttp1;

	HttpdConnection(Socket client)
	{
		this.client = client;
		setPriority(NORM_PRIORITY - 1);
	}

	public void run()
	{
		try
		{
			in = new BufferedReader( new InputStreamReader(
				client.getInputStream() ) );
			out = client.getOutputStream();
			pout = new PrintStream(out);

			String request = in.readLine();
			if ( request == null )
				error(400, "Empty Request");

			if(request.toLowerCase().indexOf("http/1.") != -1)
			{
				String s;
				while((!(s = in.readLine()).equals("")) && (s != null))
				{ ; }

				isHttp1 = true;
			}

			StringTokenizer st = new StringTokenizer(request);
			if(st.countTokens() < 2) 
				error(400, "Bad Request");
			else
			{
				String command = st.nextToken();
				if(command.equals("GET"))
					serveFile(st.nextToken());
				else
					error(400, "Bad Request");
			}

			client.close();
		}
		catch(IOException e)
		{
			System.out.println("I/O error " + e); 
			try
			{
				client.close();
			}
			catch(Exception e2) { }
		}
	}

	private void serveFile(String file) 
		throws FileNotFoundException, IOException
	{
		// Do some mappings
		if ( file.equals("/") )
			file = "/remote/remote.html";

		if ( file.startsWith("/remote/") )
			file = "/bsh/util/lib/" + file.substring(8);

	/*
		if(file.startsWith("/"))
			file = file.substring(1);
		if(file.endsWith("/") || file.equals(""))
			file = file + "index.html";

		if(!fileAccessOK(file))
		{
			error(403, "Forbidden");
			return;
		}
	*/

		// don't send java packages over... (e.g. swing)
		if ( file.startsWith("/java" ) )
			error(404, "Object Not Found");
		else
			try {
				System.out.println("sending file: "+file);
				sendFileData(file);
			} catch(FileNotFoundException e) {
				error(404, "Object Not Found");
			}
	}

	private void sendFileData(String file) 
		throws IOException, FileNotFoundException
	{
		/*
			Why aren't resources being found when this runs on Win95?
		*/
		InputStream fis = getClass().getResourceAsStream(file);
		if(fis == null)
			throw new FileNotFoundException(file);
		byte[] data = new byte[fis.available()];

		if(isHttp1)
		{
			pout.println("HTTP/1.0 200 Document follows");

			pout.println("Content-length: " + data.length );

			if ( file.endsWith(".gif") )
				pout.println("Content-type: image/gif");
			else 
				if( file.endsWith(".html") || file.endsWith(".htm") )
					pout.println("Content-Type: text/html");
				else
					pout.println("Content-Type: application/octet-stream");

			pout.println();
		}

		int bytesread =	0;
		// Never, ever trust available()
		do {
			bytesread = fis.read(data);
			if (bytesread > 0)
				pout.write( data, 0, bytesread );
		} while( bytesread != -1 );
		pout.flush();
	}

	private void error(int num, String s)
	{
		s = "<html><h1>" + s + "</h1></html>";
		if(isHttp1)
		{
			pout.println("HTTP/1.0 " + num + " " + s);
			pout.println("Content-type: text/html");
			pout.println("Content-length: " + s.length() + "\n");
		}
		
		pout.println(s);
	}
}

