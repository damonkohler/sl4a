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
	UtilEvalError is an error corresponding to an EvalError but thrown by a 
	utility or other class that does not have the caller context (Node) 
	available to it.  A normal EvalError must supply the caller Node in order 
	for error messages to be pinned to the correct line and location in the 
	script.  UtilEvalError is a checked exception that is *not* a subtype of 
	EvalError, but instead must be caught and rethrown as an EvalError by 
	the a nearest location with context.  The method toEvalError( Node ) 
	should be used to throw the EvalError, supplying the node.
	<p>

	To summarize: Utilities throw UtilEvalError.  ASTs throw EvalError.
	ASTs catch UtilEvalError and rethrow it as EvalError using 
	toEvalError( Node ).  
	<p>

	Philosophically, EvalError and UtilEvalError corrospond to 
	RuntimeException.  However they are constrained in this way in order to 
	add the context for error reporting.

	@see UtilTargetError
*/
public class UtilEvalError extends Exception 
{
	protected UtilEvalError() {
	}

	public UtilEvalError( String s ) {
		super(s);
	}

	/**
		Re-throw as an eval error, prefixing msg to the message and specifying
		the node.  If a node already exists the addNode is ignored.
		@see #setNode( bsh.SimpleNode )
		<p>
		@param msg may be null for no additional message.
	*/
	public EvalError toEvalError( 
		String msg, SimpleNode node, CallStack callstack  ) 
	{
		if ( Interpreter.DEBUG )
			printStackTrace();

		if ( msg == null )
			msg = "";
		else
			msg = msg + ": ";
		return new EvalError( msg+getMessage(), node, callstack );
	}

	public EvalError toEvalError ( SimpleNode node, CallStack callstack ) 
	{
		return toEvalError( null, node, callstack );
	}

}

