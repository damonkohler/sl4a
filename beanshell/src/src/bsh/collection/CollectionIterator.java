package bsh.collection;

import bsh.Capabilities;
import bsh.CollectionManager;
import bsh.UtilTargetError;
import bsh.InterpreterError;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.Iterator;
import java.util.Collection;
import java.util.Enumeration;

import java.lang.reflect.Array;

/**
 *	This is the implementation of:
 *	BshIterator - a dynamically loaded extension that supports the collections
 *	API supported by JDK1.2 and greater.
 *
 *	@author Daniel Leuck
 *	@author Pat Niemeyer
 */
public class CollectionIterator implements bsh.BshIterator
{
	private Iterator iterator;

	/**
	 * Construct a basic CollectionIterator
	 *
	 * @param The object over which we are iterating
	 *
	 * @throws java.lang.IllegalArgumentException If the argument is not a
	 * supported (i.e. iterable) type.
	 *
	 * @throws java.lang.NullPointerException If the argument is null
	 */	
	public CollectionIterator( Object iterateOverMe ) {
		iterator = createIterator( iterateOverMe );	
	}

	/**
	 * Create an iterator over the given object
	 *
	 * @param iterateOverMe Object of type Iterator, Collection,
	 *     Iterable, or types supported by
	 *     CollectionManager.BasicBshIterator
	 *
	 * @return an Iterator
	 *
	 * @throws java.lang.IllegalArgumentException If the argument is not a
	 *     supported (i.e. iterable) type.
	 *
	 * @throws java.lang.NullPointerException If the argument is null
	 */
	protected Iterator createIterator( Object iterateOverMe )
		throws java.lang.IllegalArgumentException 
	{
		
		if ( iterateOverMe == null )
			throw new NullPointerException("Object arguments passed to " +
				"the CollectionIterator constructor cannot be null.");

		if ( iterateOverMe instanceof Iterator )
			return (Iterator)iterateOverMe;

		if ( iterateOverMe instanceof Collection ) 
			return ((Collection)iterateOverMe).iterator();
		
		Iterator it = getIteratorForIterable( iterateOverMe );
		if ( it != null )
			return it;
		
		final CollectionManager.BasicBshIterator bbi = new
			CollectionManager.BasicBshIterator(iterateOverMe);

		return new Iterator() {
			public boolean hasNext() { return bbi.hasNext(); } 
			public Object next() { return bbi.next(); }
			public void remove() {
				throw new UnsupportedOperationException( 
					"remove() is not supported");
			}
		};
	}

	/**
		Get an Iterator for a Java 5 Iterable object.
		Rather than resorting to another loadable module for Java 5 we'll
		use reflection here to invoke the Iterable iterator() method.

		@return the Iterator or null if the object is not an Iterable.
		@author Daniel Leuck
	*/
	Iterator getIteratorForIterable( Object iterateOverMe ) 
	{
		Iterator it = null;
		try {
			Class c = Class.forName("java.lang.Iterable");
			if ( c.isInstance(iterateOverMe) ) 
			{
				try {
					Method m = c.getMethod("iterator", new Class[0]);
					it = (Iterator)m.invoke(iterateOverMe, new Object[0]);
				} catch( Exception e ) 
				{
					throw new InterpreterError("Unexpected problem calling " +
						"\"iterator()\" on instance of java.lang.Iterable."+ e);
				}
			}
		} catch( ClassNotFoundException cnfe ) {
			// we are pre-Java 5 and should continue without an exception
		} 
		return it;
	}

	/**
	 * Fetch the next object in the iteration
	 *
	 * @return The next object
	 */	
	public Object next() {
		return iterator.next();
	}
	
	/**
	 * Returns true if and only if there are more objects available
	 * via the <code>next()</code> method
	 *
	 * @return The next object
	 */	
	public boolean hasNext() {
		return iterator.hasNext();	
	}
}
