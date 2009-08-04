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

import bsh.Capabilities.Unavailable;

/**
	ReflectManager is a dynamically loaded extension that supports extended
	reflection features supported by JDK1.2 and greater.

	In particular it currently supports accessible method and field access 
	supported by JDK1.2 and greater.
*/
public abstract class ReflectManager
{
	private static ReflectManager rfm;

	/**
		Return the singleton bsh ReflectManager.
		@throws Unavailable
	*/
	public static ReflectManager getReflectManager() 
		throws Unavailable
	{
		if ( rfm == null ) 
		{
			Class clas;
			try {
				clas = Class.forName( "bsh.reflect.ReflectManagerImpl" );
				rfm = (ReflectManager)clas.newInstance();
			} catch ( Exception e ) {
				throw new Unavailable("Reflect Manager unavailable: "+e);
			}
		}
	
		return rfm;
	}

	/**
		Reflect Manager Set Accessible.
		Convenience method to invoke the reflect manager.
		@throws Unavailable
	*/
	public static boolean RMSetAccessible( Object obj ) 
		throws Unavailable
	{
		return getReflectManager().setAccessible( obj );
	}

	/**
		Set a java.lang.reflect Field, Method, Constructor, or Array of
		accessible objects to accessible mode.
		@return true if the object was accessible or false if it was not.
	*/
	public abstract boolean setAccessible( Object o );
}

