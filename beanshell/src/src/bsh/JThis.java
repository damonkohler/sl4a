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

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.beans.*;

/**
	JThis is a dynamically loaded extension which extends This and adds 
	explicit support for AWT and JFC events, etc.  This is a backwards 
	compatability measure for JDK 1.2.  With 1.3+ there is a general 
	reflection proxy mechanism that allows the base This to implement 
	arbitrary interfaces.

	The NameSpace getThis() method will produce instances of JThis if 
	the java version is prior to 1.3 and swing is available...  (e.g. 1.2
	or 1.1 + swing installed)  

	Users of 1.1 without swing will have minimal interface support (just run()).
	
	Bsh doesn't run on 1.02 and below because there is no reflection! 

	Note: This module relies on features of Swing and will only compile
	with JDK1.2 or JDK1.1 + the swing package.  For other environments simply 
	do not compile this class.
*/
class JThis extends This implements
	// All core AWT listeners
	ActionListener, AdjustmentListener, ComponentListener,
	ContainerListener, FocusListener, ItemListener, KeyListener,
	MouseListener, MouseMotionListener, TextListener, WindowListener,
	PropertyChangeListener, 
	// All listeners in javax.swing.event as of Swing 1.1
	AncestorListener, CaretListener, CellEditorListener, ChangeListener,
	DocumentListener, HyperlinkListener, 
	InternalFrameListener, ListDataListener, ListSelectionListener, 
	MenuDragMouseListener, MenuKeyListener, MenuListener, MouseInputListener, 
	PopupMenuListener, TableColumnModelListener, TableModelListener, 
	TreeExpansionListener, TreeModelListener, TreeSelectionListener, 
	TreeWillExpandListener, UndoableEditListener
{

	JThis( NameSpace namespace, Interpreter declaringInterp ) { 
		super( namespace, declaringInterp );
	}

	public String toString() {
		return "'this' reference (JThis) to Bsh object: " + namespace.getName();
	}

	void event(String name, Object event)
	{
		CallStack callstack = new CallStack( namespace );
		BshMethod method = null;

		// handleEvent gets all events
		try {
			method = namespace.getMethod( 
				"handleEvent", new Class [] { null } );
		} catch ( UtilEvalError e ) {/*squeltch*/  }

		if (method != null)
			try {
				method.invoke( 
					new Object[] { event }, declaringInterpreter, callstack, null );
			} catch(EvalError e) {
				declaringInterpreter.error(
					"local event hander method invocation error:" + e );
			}

		// send to specific event handler
		try {
			method = namespace.getMethod( name, new Class [] { null } );
		} catch ( UtilEvalError e ) { /*squeltch*/ }
		if (method != null)
			try {
				method.invoke( 
					new Object[] { event }, declaringInterpreter, callstack, null );
			} catch(EvalError e) {
				declaringInterpreter.error(
					"local event hander method invocation error:" + e );
			}
	}

	// Listener interfaces

    public void ancestorAdded(AncestorEvent e) { event("ancestorAdded", e); }
    public void ancestorRemoved(AncestorEvent e) { event("ancestorRemoved", e); }
    public void ancestorMoved(AncestorEvent e) { event("ancestorMoved", e); }
    public void caretUpdate(CaretEvent e) { event("caretUpdate", e); }
    public void editingStopped(ChangeEvent e) { event("editingStopped", e); }
    public void editingCanceled(ChangeEvent e) { event("editingCanceled", e); }
    public void stateChanged(ChangeEvent e) { event("stateChanged", e); }
    public void insertUpdate(DocumentEvent e) { event("insertUpdate", e); }
    public void removeUpdate(DocumentEvent e) { event("removeUpdate", e); }
    public void changedUpdate(DocumentEvent e) { event("changedUpdate", e); }
    public void hyperlinkUpdate(HyperlinkEvent e) { event("internalFrameOpened", e); }
    public void internalFrameOpened(InternalFrameEvent e) { event("internalFrameOpened", e); }
    public void internalFrameClosing(InternalFrameEvent e) { event("internalFrameClosing", e); }
    public void internalFrameClosed(InternalFrameEvent e) { event("internalFrameClosed", e); }
    public void internalFrameIconified(InternalFrameEvent e) { event("internalFrameIconified", e); }
    public void internalFrameDeiconified(InternalFrameEvent e) { event("internalFrameDeiconified", e); }
    public void internalFrameActivated(InternalFrameEvent e) { event("internalFrameActivated", e); }
    public void internalFrameDeactivated(InternalFrameEvent e) { event("internalFrameDeactivated", e); }
    public void intervalAdded(ListDataEvent e) { event("intervalAdded", e); }
    public void intervalRemoved(ListDataEvent e) { event("intervalRemoved", e); }
    public void contentsChanged(ListDataEvent e) { event("contentsChanged", e); }
  	public void valueChanged(ListSelectionEvent e) { event("valueChanged", e); }
    public void menuDragMouseEntered(MenuDragMouseEvent e) { event("menuDragMouseEntered", e); }
    public void menuDragMouseExited(MenuDragMouseEvent e) { event("menuDragMouseExited", e); }
    public void menuDragMouseDragged(MenuDragMouseEvent e) { event("menuDragMouseDragged", e); }
    public void menuDragMouseReleased(MenuDragMouseEvent e) { event("menuDragMouseReleased", e); }
    public void menuKeyTyped(MenuKeyEvent e) { event("menuKeyTyped", e); }
    public void menuKeyPressed(MenuKeyEvent e) { event("menuKeyPressed", e); }
    public void menuKeyReleased(MenuKeyEvent e) { event("menuKeyReleased", e); }
    public void menuSelected(MenuEvent e) { event("menuSelected", e); }
    public void menuDeselected(MenuEvent e) { event("menuDeselected", e); }
    public void menuCanceled(MenuEvent e) { event("menuCanceled", e); }
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) { event("popupMenuWillBecomeVisible", e); }
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { event("popupMenuWillBecomeInvisible", e); }
    public void popupMenuCanceled(PopupMenuEvent e) { event("popupMenuCanceled", e); }
    public void columnAdded(TableColumnModelEvent e) { event("columnAdded", e); }
    public void columnRemoved(TableColumnModelEvent e) { event("columnRemoved", e); }
    public void columnMoved(TableColumnModelEvent e) { event("columnMoved", e); }
    public void columnMarginChanged(ChangeEvent e) { event("columnMarginChanged", e); }
    public void columnSelectionChanged(ListSelectionEvent e) { event("columnSelectionChanged", e); }
    public void tableChanged(TableModelEvent e) { event("tableChanged", e); }
    public void treeExpanded(TreeExpansionEvent e) { event("treeExpanded", e); }
    public void treeCollapsed(TreeExpansionEvent e) { event("treeCollapsed", e); }
    public void treeNodesChanged(TreeModelEvent e) { event("treeNodesChanged", e); }
    public void treeNodesInserted(TreeModelEvent e) { event("treeNodesInserted", e); }
    public void treeNodesRemoved(TreeModelEvent e) { event("treeNodesRemoved", e); }
    public void treeStructureChanged(TreeModelEvent e) { event("treeStructureChanged", e); }
    public void valueChanged(TreeSelectionEvent e) { event("valueChanged", e); }
    public void treeWillExpand(TreeExpansionEvent e) { event("treeWillExpand", e); }
    public void treeWillCollapse(TreeExpansionEvent e) { event("treeWillCollapse", e); }
    public void undoableEditHappened(UndoableEditEvent e) { event("undoableEditHappened", e); }

	// Listener interfaces
	public void actionPerformed(ActionEvent e) { event("actionPerformed", e); }
	public void adjustmentValueChanged(AdjustmentEvent e) { event("adjustmentValueChanged", e); }
	public void componentResized(ComponentEvent e) { event("componentResized", e); }
	public void componentMoved(ComponentEvent e) { event("componentMoved", e); }
	public void componentShown(ComponentEvent e) { event("componentShown", e); }
	public void componentHidden(ComponentEvent e) { event("componentHidden", e); }
	public void componentAdded(ContainerEvent e) { event("componentAdded", e); }
	public void componentRemoved(ContainerEvent e) { event("componentRemoved", e); }
	public void focusGained(FocusEvent e) { event("focusGained", e); }
	public void focusLost(FocusEvent e) { event("focusLost", e); }
	public void itemStateChanged(ItemEvent e) { event("itemStateChanged", e); }
	public void keyTyped(KeyEvent e) { event("keyTyped", e); }
	public void keyPressed(KeyEvent e) { event("keyPressed", e); }
	public void keyReleased(KeyEvent e) { event("keyReleased", e); }
	public void mouseClicked(MouseEvent e) { event("mouseClicked", e); }
	public void mousePressed(MouseEvent e) { event("mousePressed", e); }
	public void mouseReleased(MouseEvent e) { event("mouseReleased", e); }
	public void mouseEntered(MouseEvent e) { event("mouseEntered", e); }
	public void mouseExited(MouseEvent e) { event("mouseExited", e); }
	public void mouseDragged(MouseEvent e) { event("mouseDragged", e); }
	public void mouseMoved(MouseEvent e) { event("mouseMoved", e); }
	public void textValueChanged(TextEvent e) { event("textValueChanged", e); }
	public void windowOpened(WindowEvent e) { event("windowOpened", e); }
	public void windowClosing(WindowEvent e) { event("windowClosing", e); }
	public void windowClosed(WindowEvent e) { event("windowClosed", e); }
	public void windowIconified(WindowEvent e) { event("windowIconified", e); }
	public void windowDeiconified(WindowEvent e) { event("windowDeiconified", e); }
	public void windowActivated(WindowEvent e) { event("windowActivated", e); }
	public void windowDeactivated(WindowEvent e) { event("windowDeactivated", e); }

	public void propertyChange(PropertyChangeEvent e) { 
		event("propertyChange", e ); }
    public void vetoableChange(PropertyChangeEvent e) {
		event("vetoableChange", e ); }

    public boolean imageUpdate(java.awt.Image img, int infoflags,
                               int x, int y, int width, int height) {

		BshMethod method = null;
		try {
			method = namespace.getMethod( "imageUpdate",
				new Class [] { null, null, null, null, null, null } );
		} catch ( UtilEvalError e ) {/*squeltch*/ }

		if(method != null)
			try {
				CallStack callstack = new CallStack( namespace );
				method.invoke( 
					new Object[] { 
						img, new Primitive(infoflags), new Primitive(x), 
						new Primitive(y), new Primitive(width), 
						new Primitive(height) }, 
					declaringInterpreter, callstack, null
				);
			} catch(EvalError e) {
				declaringInterpreter.error(
					"local event handler imageUpdate: method invocation error:" + e );
			}
		return true;
	}

}

