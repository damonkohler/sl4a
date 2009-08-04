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

import java.net.Socket;
import java.net.ServerSocket;
import bsh.*;

/**
	BeanShell remote session server.
	Starts instances of bsh for client connections.
	Note: the sessiond effectively maps all connections to the same interpreter
	(shared namespace).
*/
public class Sessiond extends Thread
{
	private ServerSocket ss;
	NameSpace globalNameSpace;

	/*
	public static void main(String argv[]) throws IOException
	{
		new Sessiond( Integer.parseInt(argv[0])).start();
	}
	*/

	public Sessiond(NameSpace globalNameSpace, int port) throws IOException
	{
		ss = new ServerSocket(port);
		this.globalNameSpace = globalNameSpace;
	}

	public void run()
	{
		try
		{
			while(true)
				new SessiondConnection(globalNameSpace, ss.accept()).start();
		}
		catch(IOException e) { System.out.println(e); }
	}
}

class SessiondConnection extends Thread
{
	NameSpace globalNameSpace;
	Socket client;

	SessiondConnection(NameSpace globalNameSpace, Socket client)
	{
		this.client = client;
		this.globalNameSpace = globalNameSpace;
	}

	public void run()
	{
		try
		{
			InputStream in = client.getInputStream();
			PrintStream out = new PrintStream(client.getOutputStream());
			Interpreter i = new Interpreter( 
				new InputStreamReader(in), out, out, true, globalNameSpace);
			i.setExitOnEOF( false ); // don't exit interp
			i.run();
		}
		catch(IOException e) { System.out.println(e); }
	}
}

