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

class BSHArguments extends SimpleNode
{
    BSHArguments(int id) { super(id); }

	/**
		This node holds a set of arguments for a method invocation or
		constructor call.

		Note: arguments are not currently allowed to be VOID.
	*/
	/*
		Disallowing VOIDs here was an easy way to support the throwing of a 
		more descriptive error message on use of an undefined argument to a 
		method call (very common).  If it ever turns out that we need to 
		support that for some reason we'll have to re-evaluate how we get 
		"meta-information" about the arguments in the various invoke() methods 
		that take Object [].  We could either pass BSHArguments down to 
		overloaded forms of the methods or throw an exception subtype 
		including the argument position back up, where the error message would
		be compounded.
	*/
    public Object[] getArguments( CallStack callstack, Interpreter interpreter)
		throws EvalError
    {
        // evaluate each child
        Object[] args = new Object[jjtGetNumChildren()];
        for(int i = 0; i < args.length; i++)
		{
            args[i] = ((SimpleNode)jjtGetChild(i)).eval(callstack, interpreter);
			if ( args[i] == Primitive.VOID )
				throw new EvalError( "Undefined argument: " + 
					((SimpleNode)jjtGetChild(i)).getText(), this, callstack );
		}

        return args;
    }
}

