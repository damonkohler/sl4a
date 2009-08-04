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
import java.awt.event.*;
import java.io.*;
import java.util.Vector;
import bsh.*;


/**
	Misc utilities for the bsh.util package.
	Nothing in the core language (bsh package) should depend on this.
	Note: that promise is currently broken... fix it.
*/
public class Util 
{
	/*
	public static ConsoleInterface makeConsole() {
		if ( bsh.Capabilities.haveSwing() )
			return new JConsole();
		else
			return new AWTConsole();
	}
	*/

	static Window splashScreen;
	/*
		This could live in the desktop script.
		However we'd like to get it on the screen as quickly as possible.
	*/
	public static void startSplashScreen() 
	{
		int width=275,height=148;
		Window win=new Window( new Frame() );
        win.pack();
        BshCanvas can=new BshCanvas();
        can.setSize( width, height ); // why is this necessary?
        Toolkit tk=Toolkit.getDefaultToolkit();
        Dimension dim=tk.getScreenSize();
        win.setBounds( 
			dim.width/2-width/2, dim.height/2-height/2, width, height );
        win.add("Center", can);
        Image img=tk.getImage( 
			Interpreter.class.getResource("/bsh/util/lib/splash.gif") );
        MediaTracker mt=new MediaTracker(can);
        mt.addImage(img,0);
        try { mt.waitForAll(); } catch ( Exception e ) { }
        Graphics gr=can.getBufferedGraphics();
        gr.drawImage(img, 0, 0, can);
        win.setVisible(true);
        win.toFront();
		splashScreen = win;
	}

	public static void endSplashScreen() {
		if ( splashScreen != null )
			splashScreen.dispose();
	}

}
