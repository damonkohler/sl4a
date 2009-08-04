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

/*
	This should go away eventually...  Native AWT sucks.
	Use JConsole and the desktop() environment.

	Notes: todo -
	clean up the watcher thread, set daemon status
*/

/**
	An old AWT based console for BeanShell.

	I looked everwhere for one, and couldn't find anything that worked.
	I've tried to keep this as small as possible, no frills.
	(Well, one frill - a simple history with the up/down arrows)
	My hope is that this can be moved to a lightweight (portable) component
	with JFC soon... but Swing is still very slow and buggy.

	Done: see JConsole.java

	The big Hack:

	The heinous, disguisting hack in here is to keep the caret (cursor)
	at the bottom of the text (without the user having to constantly click
	at the bottom).  It wouldn't be so bad if the damned setCaretPostition()
	worked as expected.  But the AWT TextArea for some insane reason treats
	NLs as characters... oh, and it refuses to let you set a caret position
	greater than the text length - for which it counts NLs as *one* character.
	The glorious hack to fix this is to go the TextComponent peer.  I really
	hate this.

	Out of date:
	
	This class is out of date.  It does not use the special blocking piped
	input stream that the jconsole uses.

	Deprecation:

	This file uses two deprecate APIs.  We want to be a PrintStream so
	that we can redirect stdout to our console... I don't see a way around
	this.  Also we have to use getPeer() for the big hack above.
*/
public class AWTConsole extends TextArea 
	implements ConsoleInterface, Runnable, KeyListener {

	private OutputStream outPipe;
	private InputStream inPipe;

	// formerly public
	private InputStream in;
	private PrintStream out;

	public Reader getIn() { return new InputStreamReader(in); }
	public PrintStream getOut() { return out; }
	public PrintStream getErr() { return out; }

	private StringBuffer line = new StringBuffer();
	private String startedLine;
	private int textLength = 0;
	private Vector history = new Vector();
	private int histLine = 0;

	public AWTConsole( int rows, int cols, InputStream cin, OutputStream cout ) {
		super(rows, cols);
		setFont( new Font("Monospaced",Font.PLAIN,14) );
		setEditable(false);
		addKeyListener ( this );

		outPipe = cout;
		if ( outPipe == null ) {
			outPipe = new PipedOutputStream();
			try {
				in = new PipedInputStream((PipedOutputStream)outPipe);
			} catch ( IOException e ) {
				print("Console internal error...");
			}
		}

		// start the inpipe watcher
		inPipe = cin;
		new Thread( this ).start();

		requestFocus();
	}

	public void keyPressed( KeyEvent e ) {
		type( e.getKeyCode(), e.getKeyChar(), e.getModifiers() );
		e.consume();
	}

	public AWTConsole() {
		this(12, 80, null, null);
	}
	public AWTConsole( InputStream in, OutputStream out ) {
		this(12, 80, in, out);
	}

	public void type(int code, char ch, int modifiers ) {
		switch ( code ) {
			case ( KeyEvent.VK_BACK_SPACE ):	
				if (line.length() > 0) {
					line.setLength( line.length() - 1 );
					replaceRange( "", textLength-1, textLength );
					textLength--;
				}
				break;
			case ( KeyEvent.VK_ENTER ):	
				enter();
				break;
			case ( KeyEvent.VK_U ):
				if ( (modifiers & InputEvent.CTRL_MASK) > 0 ) {
					int len = line.length();
					replaceRange( "", textLength-len, textLength );
					line.setLength( 0 );
					histLine = 0;
					textLength = getText().length(); 
				} else
					doChar( ch );
				break;
			case ( KeyEvent.VK_UP ):
				historyUp();
				break;
			case ( KeyEvent.VK_DOWN ):
				historyDown();
				break;
			case ( KeyEvent.VK_TAB ):	
				line.append("    ");
				append("    ");
				textLength +=4;
				break;
/*
			case ( KeyEvent.VK_LEFT ):	
				if (line.length() > 0) {
				break;
*/
			// Control-C
			case ( KeyEvent.VK_C ):
				if ( (modifiers & InputEvent.CTRL_MASK) > 0 ) {
					line.append("^C");
					append("^C");
					textLength += 2;
				} else
					doChar( ch );
				break;
			default:
				doChar( ch );
		}
	}

	private void doChar( char ch ) {
		if ( (ch >= ' ') && (ch <= '~') ) {
			line.append( ch );
			append( String.valueOf(ch) );
			textLength++;
		}
	}

	private void enter() {
		String s;
		if ( line.length() == 0 )  // special hack for empty return!
			s = ";\n";
		else {
			s = line +"\n";
			history.addElement( line.toString() );
		}
		line.setLength( 0 );
		histLine = 0;
		append("\n");
		textLength = getText().length(); // sync for safety
		acceptLine( s );

		setCaretPosition( textLength );
	}

	/* 
		Here's the really disguisting hack.
		We have to get to the peer because TextComponent will refuse to
		let us set us set a caret position greater than the text length.
		Great.  What a piece of crap.
	*/
	public void setCaretPosition( int pos ) {
		((java.awt.peer.TextComponentPeer)getPeer()).setCaretPosition( 
			pos + countNLs() );
	}

	/*
		This is part of a hack to fix the setCaretPosition() bug
		Count the newlines in the text
	*/
	private int countNLs() { 
		String s = getText();
		int c = 0;
		for(int i=0; i< s.length(); i++)
			if ( s.charAt(i) == '\n' )
				c++;
		return c;
	}

	private void historyUp() {
		if ( history.size() == 0 )
			return;
		if ( histLine == 0 )  // save current line
			startedLine = line.toString();
		if ( histLine < history.size() ) {
			histLine++;
			showHistoryLine();
		}
	}
	private void historyDown() {
		if ( histLine == 0 ) 
			return;

		histLine--;
		showHistoryLine();
	}

	private void showHistoryLine() {
		String showline;
		if ( histLine == 0 )
			showline = startedLine;
		else
			showline = (String)history.elementAt( history.size() - histLine );

		replaceRange( showline, textLength-line.length(), textLength );
		line = new StringBuffer(showline);
		textLength = getText().length();
	}

	private void acceptLine( String line ) {
		if (outPipe == null )
			print("Console internal error...");
		else
			try {
				outPipe.write( line.getBytes() );
				outPipe.flush();
			} catch ( IOException e ) {
				outPipe = null;
				throw new RuntimeException("Console pipe broken...");
			}
	}

	public void println( Object o ) {
		print( String.valueOf(o)+"\n" );
	}

	public void error( Object o ) {
		print( o, Color.red );
	}

	// No color
	public void print( Object o, Color c ) {
		print( "*** " + String.valueOf(o));
	}

	synchronized public void print( Object o ) {
		append(String.valueOf(o));
		textLength = getText().length(); // sync for safety
	}

	private void inPipeWatcher() throws IOException {
		if ( inPipe == null ) {
			PipedOutputStream pout = new PipedOutputStream();
			out = new PrintStream( pout );
			inPipe = new PipedInputStream(pout);
		}
		byte [] ba = new byte [256]; // arbitrary blocking factor
		int read;
		while ( (read = inPipe.read(ba)) != -1 )
			print( new String(ba, 0, read) );

		println("Console: Input closed...");
	}

	public void run() {
		try {
			inPipeWatcher();
		} catch ( IOException e ) {
			println("Console: I/O Error...");
		}
	}

	public static void main( String args[] ) {
		AWTConsole console = new AWTConsole();
		final Frame f = new Frame("Bsh Console");
		f.add(console, "Center");
		f.pack();
		f.show();
		f.addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent e ) {
				f.dispose();
			}
		} );
			
		Interpreter interpreter = new Interpreter( console );
		interpreter.run();
	}

	public String toString() {
		return "BeanShell AWTConsole";
	}

	// unused
	public void keyTyped(KeyEvent e) { }
    public void keyReleased(KeyEvent e) { }
}
