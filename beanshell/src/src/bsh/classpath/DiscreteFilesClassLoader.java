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

package bsh.classpath;

import java.io.*;
import java.io.File;
import java.util.*;
import java.awt.*;
import bsh.BshClassManager;
import bsh.classpath.BshClassPath.ClassSource;
import bsh.classpath.BshClassPath.DirClassSource;
import bsh.classpath.BshClassPath.GeneratedClassSource;

/**
	A classloader which can load one or more classes from specified sources.
	Because the classes are loaded via a single classloader they change as a
	group and any versioning cross dependencies can be managed.
*/
public class DiscreteFilesClassLoader extends BshClassLoader 
{
	/**
		Map of class sources which also implies our coverage space.
	*/
	ClassSourceMap map;

	public static class ClassSourceMap extends HashMap 
	{
		public void put( String name, ClassSource source ) {
			super.put( name, source );
		}
		public ClassSource get( String name ) {
			return (ClassSource)super.get( name );
		}
	}
	
	public DiscreteFilesClassLoader( 
		BshClassManager classManager, ClassSourceMap map ) 
	{
		super( classManager );
		this.map = map;
	}

	/**
	*/
	public Class findClass( String name ) throws ClassNotFoundException 
	{
		// Load it if it's one of our classes
		ClassSource source = map.get( name );

		if ( source != null )
		{
			byte [] code = source.getCode( name );
			return defineClass( name, code, 0, code.length );
		} else
			// Let superclass BshClassLoader (URLClassLoader) findClass try 
			// to find the class...
			return super.findClass( name );
	}

	public String toString() {
		return super.toString() + "for files: "+map;
	}

}
