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

/**
	UtilTargetError is an error corresponding to a TargetError but thrown by a 
	utility or other class that does not have the caller context (Node) 
	available to it.  See UtilEvalError for an explanation of the difference
	between UtilEvalError and EvalError.
	<p>

	@see UtilEvalError
*/
public class UtilTargetError extends UtilEvalError
{
	public Throwable t;

	public UtilTargetError( String message, Throwable t ) {
		super( message );
		this.t = t;
	}

	public UtilTargetError( Throwable t ) {
		this( null, t );
	}

	/**
		Override toEvalError to throw TargetError type.
	*/
	public EvalError toEvalError( 
		String msg, SimpleNode node, CallStack callstack  ) 
	{
		if ( msg == null )
			msg = getMessage();
		else
			msg = msg + ": " + getMessage();

		return new TargetError( msg, t, node, callstack, false );
	}
}

