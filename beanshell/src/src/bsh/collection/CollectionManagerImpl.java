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

package bsh.collection;

import java.util.Iterator;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.lang.reflect.Array;
import bsh.BshIterator;

/**
	Dynamically loaded extension supporting post 1.1 collections iterator.
 	@author Pat Niemeyer
 */
public class CollectionManagerImpl extends bsh.CollectionManager
{
	public BshIterator getBshIterator( Object obj ) 
		throws IllegalArgumentException
	{
		return new CollectionIterator( obj ); 
	}

	public boolean isMap( Object obj ) 
	{
		if ( obj instanceof Map )
			return true;
		else
			return super.isMap( obj );
	}

	public Object getFromMap( Object map, Object key ) 
	{
		// Hashtable implements Map
		return ((Map)map).get(key);
	}

	/*
	  	Place the raw value into the map... should be unwrapped.
	 */
	public Object putInMap( Object map, Object key, Object value ) 
	{
		// Hashtable implements Map
		return ((Map)map).put(key, value);
	}
}
