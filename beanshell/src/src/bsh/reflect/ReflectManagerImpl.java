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

package bsh.reflect;

import bsh.ReflectManager;
import java.lang.reflect.AccessibleObject;

/**
	This is the implementation of:
	ReflectManager - a dynamically loaded extension that supports extended
	reflection features supported by JDK1.2 and greater.

	In particular it currently supports accessible method and field access 
	supported by JDK1.2 and greater.
*/
public class ReflectManagerImpl extends ReflectManager
{
	/**
		Set a java.lang.reflect Field, Method, Constructor, or Array of
		accessible objects to accessible mode.
		If the object is not an AccessibleObject then do nothing.
		@return true if the object was accessible or false if it was not.
	*/
// Arrays incomplete... need to use the array setter
	public boolean setAccessible( Object obj ) 
	{
		if ( obj instanceof AccessibleObject ) {
			((AccessibleObject)obj).setAccessible(true);
			return true;
		} else
			return false;
	}
}

