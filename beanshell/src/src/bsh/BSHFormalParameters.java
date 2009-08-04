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

class BSHFormalParameters extends SimpleNode
{
	private String [] paramNames;
	/**
		For loose type parameters the paramTypes are null.
	*/
	// unsafe caching of types
	Class [] paramTypes;
	int numArgs;
	String [] typeDescriptors;

	BSHFormalParameters(int id) { super(id); }

	void insureParsed() 
	{
		if ( paramNames != null )
			return;

		this.numArgs = jjtGetNumChildren();
		String [] paramNames = new String[numArgs];

		for(int i=0; i<numArgs; i++)
		{
			BSHFormalParameter param = (BSHFormalParameter)jjtGetChild(i);
			paramNames[i] = param.name;
		}

		this.paramNames = paramNames;
	}

	public String [] getParamNames() { 
		insureParsed();
		return paramNames;
	}

	public String [] getTypeDescriptors( 
		CallStack callstack, Interpreter interpreter, String defaultPackage )
	{
		if ( typeDescriptors != null )
			return typeDescriptors;

		insureParsed();
		String [] typeDesc = new String[numArgs];

		for(int i=0; i<numArgs; i++)
		{
			BSHFormalParameter param = (BSHFormalParameter)jjtGetChild(i);
			typeDesc[i] = param.getTypeDescriptor( 
				callstack, interpreter, defaultPackage );
		}

		this.typeDescriptors = typeDesc;
		return typeDesc;
	}

	/**
		Evaluate the types.  
		Note that type resolution does not require the interpreter instance.
	*/
	public Object eval( CallStack callstack, Interpreter interpreter )  
		throws EvalError
	{
		if ( paramTypes != null )
			return paramTypes;

		insureParsed();
		Class [] paramTypes = new Class[numArgs];

		for(int i=0; i<numArgs; i++)
		{
			BSHFormalParameter param = (BSHFormalParameter)jjtGetChild(i);
			paramTypes[i] = (Class)param.eval( callstack, interpreter );
		}

		this.paramTypes = paramTypes;

		return paramTypes;
	}
}

