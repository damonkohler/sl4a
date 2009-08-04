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

import java.net.*;
import java.util.*;
import java.io.*;
import bsh.BshClassManager;

/**
	One of the things BshClassLoader does is to address a deficiency in
	URLClassLoader that prevents us from specifying individual classes
	via URLs.
*/
public class BshClassLoader extends URLClassLoader 
{
	BshClassManager classManager;

	/**
		@param bases URLs JARClassLoader seems to require absolute paths 
	*/
	public BshClassLoader( BshClassManager classManager, URL [] bases ) {
		super( bases );
		this.classManager = classManager;
	}

	/**
		@param bases URLs JARClassLoader seems to require absolute paths 
	*/
	public BshClassLoader( BshClassManager classManager, BshClassPath bcp ) {
		this( classManager, bcp.getPathComponents() );
	}

	/**
		For use by children
		@param bases URLs JARClassLoader seems to require absolute paths 
	*/
	protected BshClassLoader( BshClassManager classManager ) { 
		this( classManager, new URL [] { } );
	}

	// public version of addURL
	public void addURL( URL url ) {
		super.addURL( url );
	}

	/**
		This modification allows us to reload classes which are in the 
		Java VM user classpath.  We search first rather than delegate to
		the parent classloader (or bootstrap path) first.

		An exception is for BeanShell core classes which are always loaded from
		the same classloader as the interpreter.
	*/
	public Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        Class c = null;

		/*
			Check first for classes loaded through this loader.
			The VM will not allow a class to be loaded twice.
		*/
        c = findLoadedClass(name);
		if ( c != null )
			return c;

// This is copied from ClassManagerImpl
// We should refactor this somehow if it sticks around
		if ( name.startsWith( ClassManagerImpl.BSH_PACKAGE ) )
			try {
				return bsh.Interpreter.class.getClassLoader().loadClass( name );
			} catch ( ClassNotFoundException e ) {}

		/*
			Try to find the class using our classloading mechanism.
			Note: I wish we didn't have to catch the exception here... slow
		*/
		try {
			c = findClass( name );
		} catch ( ClassNotFoundException e ) { }

		if ( c == null )
			throw new ClassNotFoundException("here in loaClass");

		if ( resolve )
			resolveClass( c );

		return c;
	}

	/**
		Find the correct source for the class...

		Try designated loader if any
		Try our URLClassLoader paths if any
		Try base loader if any
		Try system ???
	*/
	// add some caching for not found classes?
	protected Class findClass( String name ) 
		throws ClassNotFoundException 
	{
		// Deal with this cast somehow... maybe have this class use 
		// ClassManagerImpl type directly.
		// Don't add the method to BshClassManager... it's really an impl thing
		ClassManagerImpl bcm = (ClassManagerImpl)getClassManager();

		// Should we try to load the class ourselves or delegate?
		// look for overlay loader

		ClassLoader cl = bcm.getLoaderForClass( name );

		Class c;

		// If there is a designated loader and it's not us delegate to it
		if ( cl != null && cl != this )
			try {
				return cl.loadClass( name );
			} catch ( ClassNotFoundException e ) {
				throw new ClassNotFoundException(
					"Designated loader could not find class: "+e );
			}

		// Let URLClassLoader try any paths it may have
		if ( getURLs().length > 0 )
			try {
				return super.findClass(name);
			} catch ( ClassNotFoundException e ) { 
				//System.out.println(
				//	"base loader here caught class not found: "+name );
			}


		// If there is a baseLoader and it's not us delegate to it
		cl = bcm.getBaseLoader();

		if ( cl != null && cl != this )
			try {
				return cl.loadClass( name );
			} catch ( ClassNotFoundException e ) { }

		// Try system loader
		return bcm.plainClassForName( name );
	}

	/*
		The superclass does something like this

        c = findLoadedClass(name);
        if null
            try
                if parent not null
                    c = parent.loadClass(name, false);
                else
                    c = findBootstrapClass(name);
            catch ClassNotFoundException 
                c = findClass(name);
	*/

	BshClassManager getClassManager() { return classManager; }

}
