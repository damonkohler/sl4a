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
	A formal parameter declaration.
	For loose variable declaration type is null.
*/
class BSHFormalParameter extends SimpleNode
{
	public static final Class UNTYPED = null;
	public String name;
	// unsafe caching of type here
	public Class type;

	BSHFormalParameter(int id) { super(id); }

	public String getTypeDescriptor( 
		CallStack callstack, Interpreter interpreter, String defaultPackage ) 
	{
		if ( jjtGetNumChildren() > 0 )
			return ((BSHType)jjtGetChild(0)).getTypeDescriptor( 
				callstack, interpreter, defaultPackage );
		else
			// this will probably not get used
			return "Ljava/lang/Object;";  // Object type
	}

	/**
		Evaluate the type.
	*/
	public Object eval( CallStack callstack, Interpreter interpreter) 
		throws EvalError
	{
		if ( jjtGetNumChildren() > 0 )
			type = ((BSHType)jjtGetChild(0)).getType( callstack, interpreter );
		else
			type = UNTYPED;

		return type;
	}
}

