/*
 * %W% %E% %U%
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTAIL. Use is subject to license terms.
 */

package javax.script;

/**
 * The optional interface implemented by ScriptEngines whose methods allow the invocation of  
 * procedures in scripts that have previously been executed. 
 *
 * @version 1.0
 * @author  Mike Grogan
 * @author  A. Sundararajan
 * @since 1.6
 */
public interface Invocable  {
    /**
     * Calls a procedure compiled during a previous script execution, which is retained in 
     * the state of the <code>ScriptEngine<code>.
     *
     * @param name The name of the procedure to be called.
     *
     * @param thiz If the procedure is a member  of a class
     * defined in the script and thiz is an instance of that class
     * returned by a previous execution or invocation, the named method is 
     * called through that instance.
     * If classes are not supported in the scripting language or 
     * if the procedure is not a member function of any class, the argument must be 
     * <code>null</code>.
     *
     * @param args Arguments to pass to the procedure.  The rules for converting
     * the arguments to scripting variables are implementation-specific.
     *
     * @return The value returned by the procedure.  The rules for converting the scripting variable returned by the procedure to a Java Object are implementation-specific.
     *
     * @throws ScriptException if an error occurrs during invocation of the method.
     * @throws NoSuchMethodException if method with given name or matching argument types cannot be found.
     * @throws NullPointerException if method name is null.
     */
    public Object invoke(Object thiz, String name, Object... args)
        throws ScriptException, NoSuchMethodException;
    
    /**
     * Same as invoke(Object, String, Object...) with <code>null</code> as the first
     * argument.  Used to call top-level procedures defined in scripts.
     *
     * @param args Arguments to pass to the procedure
     * @return The value returned by the procedure
     *
     * @throws ScriptException if an error occurrs during invocation of the method.
     * @throws NoSuchMethodException if method with given name or matching argument types cannot be found.
     * @throws NullPointerException if method name is null.
     */
     public Object invoke(String name, Object... args)
        throws ScriptException, NoSuchMethodException;
     
     
     /**
     * Returns an implementation of an interface using procedures compiled in 
     * the interpreter. The methods of the interface 
     * may be implemented using the <code>invoke</code> method.
     *
     * @param clasz The <code>Class</code> object of the interface to return.
     *
     * @return An instance of requested interface - null if the requested interface is unavailable, 
     * i. e. if compiled methods in the <code>ScriptEngine</code> cannot be found matching 
     * the ones in the requested interface.
     *
     * @throws IllegalArgumentException if the specified <code>Class</code> object
     * does not exist or is not an interface. 
     */
    public <T> T getInterface(Class<T> clasz);
        
    /**
     * Returns an implementation of an interface using member functions of
     * a scripting object compiled in the interpreter. The methods of the
     * interface may be implemented using invoke(Object, String, Object...)
     * method.
     *
     * @param thiz The scripting object whose member functions are used to implement the methods of the interface.
     * @param clasz The <code>Class</code> object of the interface to return.
     *
     * @return An instance of requested interface - null if the requested interface is unavailable, 
     * i. e. if compiled methods in the <code>ScriptEngine</code> cannot be found matching 
     * the ones in the requested interface.
     *
     * @throws IllegalArgumentException if the specified <code>Class</code> object
     * does not exist or is not an interface, or if the specified Object is 
     * null or does not represent a scripting object.
     */
     public <T> T getInterface(Object thiz, Class<T> clasz);
     
}
