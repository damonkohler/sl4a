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

import java.util.*;

public class StringUtil
{

	public static String [] split( String s, String delim) {
		Vector v = new Vector();
		StringTokenizer st = new StringTokenizer(s, delim);
		while ( st.hasMoreTokens() )
			v.addElement( st.nextToken() );
		String [] sa = new String [ v.size() ];
		v.copyInto( sa );
		return sa;
	}

	public static String [] bubbleSort( String [] in ) {
		Vector v = new Vector();
		for(int i=0; i<in.length; i++)
			v.addElement(in[i]);

		int n = v.size();
		boolean swap = true;
		while ( swap ) {
			swap = false;
			for(int i=0; i<(n-1); i++)
				if ( ((String)v.elementAt(i)).compareTo(
						((String)v.elementAt(i+1)) ) > 0 ) {
					String tmp = (String)v.elementAt(i+1);
					v.removeElementAt( i+1 );
					v.insertElementAt( tmp, i );
					swap = true;
				}
		}

		String [] out = new String [ n ];
		v.copyInto(out);
		return out;
	}


	public static String maxCommonPrefix( String one, String two ) {
		int i=0;
		while( one.regionMatches( 0, two, 0, i ) )
			i++;
		return one.substring(0, i-1);
	}

    public static String methodString(String name, Class[] types)
    {
        StringBuffer sb = new StringBuffer(name + "(");
        if ( types.length > 0 )
			sb.append(" ");
        for( int i=0; i<types.length; i++ )
        {
            Class c = types[i];
            sb.append( ( (c == null) ? "null" : c.getName() ) 
				+ ( i < (types.length-1) ? ", " : " " ) );
        }
        sb.append(")");
        return sb.toString();
    }

	/**
		Split a filename into dirName, baseName
		@return String [] { dirName, baseName }
    public String [] splitFileName( String fileName ) 
	{ 
		String dirName, baseName;
		int i = fileName.lastIndexOf( File.separator );
		if ( i != -1 ) {
			dirName = fileName.substring(0, i);
			baseName = fileName.substring(i+1);
		} else
			baseName = fileName;

		return new String[] { dirName, baseName };
	}

	*/

	/**
		Hack - The real method is in Reflect.java which is not public.
	*/
    public static String normalizeClassName( Class type )
	{
		return Reflect.normalizeClassName( type );
	}
}
