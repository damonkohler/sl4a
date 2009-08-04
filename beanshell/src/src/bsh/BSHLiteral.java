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

class BSHLiteral extends SimpleNode
{
    public Object value;

    BSHLiteral(int id) { super(id); }

    public Object eval( CallStack callstack, Interpreter interpreter )
		throws EvalError
    {
		if ( value == null )
			throw new InterpreterError("Null in bsh literal: "+value);

        return value;
    }

    private char getEscapeChar(char ch)
    {
        switch(ch)
        {
            case 'b':
                ch = '\b';
                break;

            case 't':
                ch = '\t';
                break;

            case 'n':
                ch = '\n';
                break;

            case 'f':
                ch = '\f';
                break;

            case 'r':
                ch = '\r';
                break;

            // do nothing - ch already contains correct character
            case '"':
            case '\'':
            case '\\':
                break;
        }

        return ch;
    }

    public void charSetup(String str)
    {
        char ch = str.charAt(0);
        if(ch == '\\')
        {
            // get next character
            ch = str.charAt(1);

            if(Character.isDigit(ch))
                ch = (char)Integer.parseInt(str.substring(1), 8);
            else
                ch = getEscapeChar(ch);
        }

        value = new Primitive(new Character(ch).charValue());
    }

    void stringSetup(String str)
    {
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < str.length(); i++)
        {
            char ch = str.charAt(i);
            if(ch == '\\')
            {
                // get next character
                ch = str.charAt(++i);

                if(Character.isDigit(ch))
                {
                    int endPos = i;

                    // check the next two characters
                    while(endPos < i + 2)
                    {
                        if(Character.isDigit(str.charAt(endPos + 1)))
                            endPos++;
                        else
                            break;
                    }

                    ch = (char)Integer.parseInt(str.substring(i, endPos + 1), 8);
                    i = endPos;
                }
                else
                    ch = getEscapeChar(ch);
            }

            buffer.append(ch);
        }

        value = buffer.toString().intern();
    }
}
