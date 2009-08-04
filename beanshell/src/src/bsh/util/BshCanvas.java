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

package bsh.util;

import java.awt.*;
import javax.swing.*;
import bsh.*;

/**
	Scriptable Canvas with buffered graphics.

	Provides a Component that:
	1) delegates calls to paint() to a bsh method called paint() 
		in a specific NameSpace.
	2) provides a simple buffered image maintained by built in paint() that 
		is useful for simple immediate procedural rendering from scripts...  

*/
public class BshCanvas extends JComponent {
	This ths;
	Image imageBuffer;

	public BshCanvas () { }

	public BshCanvas ( This ths ) {
		this.ths = ths;
	}

	public void paintComponent( Graphics g ) {
		// copy buffered image
		if ( imageBuffer != null )
			g.drawImage(imageBuffer, 0,0, this);

		// Delegate call to scripted paint() method
		if ( ths != null ) {
			try {
				ths.invokeMethod( "paint", new Object[] { g } );
			} catch(EvalError e) {
				if ( Interpreter.DEBUG ) Interpreter.debug(
					"BshCanvas: method invocation error:" + e);
			}
		}
	}

	/**
		Get a buffered (persistent) image for drawing on this component
	*/
	public Graphics getBufferedGraphics() {
		Dimension dim = getSize();
		imageBuffer = createImage( dim.width, dim.height );
		return imageBuffer.getGraphics();
	}

	public void setBounds( int x, int y, int width, int height ) {
		setPreferredSize( new Dimension(width, height) );
		setMinimumSize( new Dimension(width, height) );
		super.setBounds( x, y, width, height );
	}

}

