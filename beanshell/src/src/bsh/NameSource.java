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

/**
	This interface supports name completion, which is used primarily for 
	command line tools, etc.  It provides a flat source of "names" in a 
	space.  For example all of the classes in the classpath or all of the 
	variables in a namespace (or all of those).
	<p>
	NameSource is the lightest weight mechanism for sources which wish to
	support name completion.  In the future it might be better for NameSpace
	to implement NameCompletion directly in a more native and efficient 
	fasion.  However in general name competion is used for human interaction
	and therefore does not require high performance.
	<p>
	@see bsh.util.NameCompletion
	@see bsh.util.NameCompletionTable
*/
public interface NameSource 
{
	public String [] getAllNames();
	public void addNameSourceListener( NameSource.Listener listener );

	public static interface Listener {
		public void nameSourceChanged( NameSource src );
		/**
			Provide feedback on the progress of mapping a namespace
			@param msg is an update about what's happening
			@perc is an integer in the range 0-100 indicating percentage done
		public void nameSourceMapping( 
			NameSource src, String msg, int perc );
		*/
	}
}
