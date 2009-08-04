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

import java.lang.reflect.Array;

class BSHType extends SimpleNode 
	implements BshClassManager.Listener
{
	/**
		baseType is used during evaluation of full type and retained for the
		case where we are an array type.
		In the case where we are not an array this will be the same as type.
	*/
	private Class baseType;
	/** 
		If we are an array type this will be non zero and indicate the 
		dimensionality of the array.  e.g. 2 for String[][];
	*/
    private int arrayDims;

	/** 
		Internal cache of the type.  Cleared on classloader change.
	*/
    private Class type;

	String descriptor;

    BSHType(int id) { 
		super(id); 
	}

	/**
		Used by the grammar to indicate dimensions of array types 
		during parsing.
	*/
    public void addArrayDimension() { 
		arrayDims++; 
	}

	SimpleNode getTypeNode() {
        return (SimpleNode)jjtGetChild(0);
	}

    /**
		 Returns a class descriptor for this type.
		 If the type is an ambiguous name (object type) evaluation is 
		 attempted through the namespace in order to resolve imports.
		 If it is not found and the name is non-compound we assume the default
		 package for the name.
	*/
    public String getTypeDescriptor( 
		CallStack callstack, Interpreter interpreter, String defaultPackage ) 
    {
        // return cached type if available
		if ( descriptor != null )
			return descriptor;

		String descriptor;
        //  first node will either be PrimitiveType or AmbiguousName
        SimpleNode node = getTypeNode();
        if ( node instanceof BSHPrimitiveType )
            descriptor = getTypeDescriptor( ((BSHPrimitiveType)node).type );
        else 
		{
            String clasName = ((BSHAmbiguousName)node).text;
			BshClassManager bcm = interpreter.getClassManager();
			// Note: incorrect here - we are using the hack in bsh class
			// manager that allows lookup by base name.  We need to eliminate
			// this limitation by working through imports.  See notes in class
			// manager.
			String definingClass = bcm.getClassBeingDefined( clasName );

            Class clas = null;
			if ( definingClass == null )
			{
				try {
					clas = ((BSHAmbiguousName)node).toClass( 
						callstack, interpreter );
				} catch ( EvalError e ) {
					//throw new InterpreterError("unable to resolve type: "+e);
					// ignore and try default package
					//System.out.println("BSHType: "+node+" class not found");
				}
			} else
				clasName = definingClass;

			if ( clas != null )
			{
				//System.out.println("found clas: "+clas);
            	descriptor = getTypeDescriptor( clas );
			}else
			{
				if ( defaultPackage == null || Name.isCompound( clasName ) )
            		descriptor = "L" + clasName.replace('.','/') + ";";
				else
            		descriptor = 
						"L"+defaultPackage.replace('.','/')+"/"+clasName + ";";
			}
		}

		for(int i=0; i<arrayDims; i++)
			descriptor = "["+descriptor;

		this.descriptor = descriptor;
	//System.out.println("BSHType: returning descriptor: "+descriptor);
        return descriptor;
    }

    public Class getType( CallStack callstack, Interpreter interpreter ) 
		throws EvalError
    {
        // return cached type if available
		if ( type != null )
			return type;

        //  first node will either be PrimitiveType or AmbiguousName
        SimpleNode node = getTypeNode();
        if ( node instanceof BSHPrimitiveType )
            baseType = ((BSHPrimitiveType)node).getType();
        else 
            baseType = ((BSHAmbiguousName)node).toClass( 
				callstack, interpreter );

        if ( arrayDims > 0 ) {
            try {
                // Get the type by constructing a prototype array with
				// arbitrary (zero) length in each dimension.
                int[] dims = new int[arrayDims]; // int array default zeros
                Object obj = Array.newInstance(baseType, dims);
                type = obj.getClass(); 
            } catch(Exception e) {
                throw new EvalError("Couldn't construct array type", 
					this, callstack );
            }
        } else
            type = baseType;

		// hack... sticking to first interpreter that resolves this
		// see comments on type instance variable
		interpreter.getClassManager().addListener(this);

        return type;
    }

	/**
		baseType is used during evaluation of full type and retained for the
		case where we are an array type.
		In the case where we are not an array this will be the same as type.
	*/
	public Class getBaseType() {
		return baseType;
	}
	/** 
		If we are an array type this will be non zero and indicate the 
		dimensionality of the array.  e.g. 2 for String[][];
	*/
	public int getArrayDims() {
		return arrayDims;
	}

	public void classLoaderChanged() {
		type = null;
		baseType = null;
	}

	public static String getTypeDescriptor( Class clas ) 
	{
		if ( clas == Boolean.TYPE ) return "Z";
		if ( clas == Character.TYPE ) return "C"; 
		if ( clas == Byte.TYPE ) return "B";
		if ( clas == Short.TYPE ) return "S";
		if ( clas == Integer.TYPE ) return "I";
		if ( clas == Long.TYPE ) return "J";
		if ( clas == Float.TYPE ) return "F";
		if ( clas == Double.TYPE ) return "D";
		if ( clas == Void.TYPE ) return "V";
	// Is getName() ok?  test with 1.1
		String name = clas.getName().replace('.','/');

		if ( name.startsWith("[") || name.endsWith(";") )
			return name;
		else
			return "L"+ name.replace('.','/') +";";
	}
}
