/**
	Display the contents of the current working directory.  
	The format is similar to the Unix ls -l
	<em>This is an example of a bsh command written in Java for speed.</em>
	
	@method void dir( [ String dirname ] )
*/
package bsh.commands;

import java.io.*;
import bsh.*;
import java.util.Date;
import java.util.Vector;
import java.util.GregorianCalendar;
import java.util.Calendar;

public class dir 
{
	static final String [] months = { "Jan", "Feb", "Mar", "Apr", 
		"May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	public static String usage() {
		return "usage: dir( String dir )\n       dir()";
	}

	/**
		Implement dir() command.
	*/
	public static void invoke( Interpreter env, CallStack callstack ) 
	{
		String dir = ".";
		invoke( env, callstack, dir );
	}

	/**
		Implement dir( String directory ) command.
	*/
	public static void invoke( 
		Interpreter env, CallStack callstack, String dir ) 
	{
		File file;
		String path;
		try {
			path = env.pathToFile( dir ).getAbsolutePath();
			file =  env.pathToFile( dir );
		} catch (IOException e ) {
			env.println("error reading path: "+e);
			return;
		}

		if ( !file.exists() || !file.canRead() ) {
			env.println( "Can't read " + file );
			return;
		}
		if ( !file.isDirectory() )  {
			env.println("'"+dir+"' is not a directory");
		}

		String [] files = file.list();
		files = StringUtil.bubbleSort(files);

		for( int i=0; i< files.length; i++ ) {
			File f = new File( path + File.separator + files[i] );
			StringBuffer sb = new StringBuffer();
			sb.append( f.canRead() ? "r": "-" );
			sb.append( f.canWrite() ? "w": "-" );
			sb.append( "_" );
			sb.append( " ");

			Date d = new Date(f.lastModified());
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(d);
			int day	= c.get(Calendar.DAY_OF_MONTH);
			sb.append( months[ c.get(Calendar.MONTH) ] + " " + day );
			if ( day < 10 ) 
				sb.append(" ");

			sb.append(" ");

			// hack to get fixed length 'length' field
			int fieldlen = 8;
			StringBuffer len = new StringBuffer();
			for(int j=0; j<fieldlen; j++)
				len.append(" ");
			len.insert(0, f.length());
			len.setLength(fieldlen);
			// hack to move the spaces to the front
			int si = len.toString().indexOf(" ");
			if ( si != -1 ) {
				String pad = len.toString().substring(si);
				len.setLength(si);
				len.insert(0, pad);
			}
			
			sb.append( len.toString() );

			sb.append( " " + f.getName() );
			if ( f.isDirectory() ) 
				sb.append("/");

			env.println( sb.toString() );
		}
	}
}

