/*
 * %W% %E% %U%
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTAIL. Use is subject to license terms.
 */

package javax.script;
import java.util.*;
import java.net.URL;
import java.io.*;
import java.security.*;
import sun.misc.Launcher;
import sun.misc.Resource;
import sun.reflect.Reflection;
import sun.security.util.SecurityConstants;

/**
 * The <code>ScriptEngineManager</code> implements a discovery and instantiation
 * mechanism for <code>ScriptEngine</code> classes and also maintains a
 * collection of key/value pairs storing state shared by all engines created
 * by the Manager.
 * <br><br>
 * The Discovery feature uses the Service Provider mechanism described in the <i>Jar
 * File Specification</i> to enumerate all implementations of
 * <code>ScriptEngineFactory</code> which can be loaded by the thread context
 * ClassLoader. If the current security policy does not allow access to thread context
 * ClassLoader, then bootstrap loader is used. The <code>ScriptEngineManager</code> provides
 * a method to return an array of all these factories as well as utility methods which
 * look up factories on the basis of language name, file extension and
 * mime type.
 * <p>
 * The <code>Bindings</code> of key/value pairs, referred to as the "Global Scope"  maintained
 * by the manager is available to all instances of <code>ScriptEngine</code> created
 * by the <code>ScriptEngineManager</code>.  The values in the <code>Bindings</code> are
 * generally exposed in all scripts.
 *
 * @author Mike Grogan
 * @author A. Sundararajan
 * @since 1.6
 */
public class ScriptEngineManager  {
    private static final boolean DEBUG = false;
    /**
     * The constructor checks for implementors of
     * <code>ScriptEngineFactory</code>  using the mechanism for
     * discovering service providers described in the Jar File Specification.<br><br>
     * Namely, it looks for resources named
     * <i>META-INF/services/javax.script.ScriptEngineFactory</i> in the thread context <code>ClassLoader</code>.  Each line in such
     * a resource names a class implementing <code>ScriptEngineFactory</code>.
     * An instance of each of these classes is created and stored in the <code>engineSpis</code>
     * <code>HashSet</code> field.  Invalid or incorrect entries are ignored. If thread context
     * loader is not accessible by current security policy, then bootstrap loader will be used.
     */
    public ScriptEngineManager() {
        ClassLoader ctxtLoader = Thread.currentThread().getContextClassLoader();
        if (canCallerAccessLoader(ctxtLoader)) {
            if (DEBUG) System.out.println("using " + ctxtLoader);
            init(ctxtLoader);
        } else {
            if (DEBUG) System.out.println("using bootstrap loader");
            init(null);
        }
    }
    
    /**
     * The constructor checks for implementors of
     * <code>ScriptEngineFactory</code>  using the mechanism for
     * discovering service providers described in the Jar File Specification.<br><br>
     * Namely, it looks for resources named
     * <i>META-INF/services/javax.script.ScriptEngineFactory</i> in the given <code>ClassLoader</code>.  Each line in such
     * a resource names a class implementing <code>ScriptEngineFactory</code>.
     * An instance of each of these classes is created and stored in the <code>engineSpis</code>
     * <code>HashSet</code> field.  Invalid or incorrect entries are ignored.
     *
     * @param loader ClassLoader used to discover factory resources.
     */
    public ScriptEngineManager(ClassLoader loader) {
        init(loader);
    }
    
    private void init(final ClassLoader loader) {
        globalScope = new SimpleBindings();
        engineSpis = new HashSet<ScriptEngineFactory>();
        nameAssociations = new HashMap<String, ScriptEngineFactory>();
        extensionAssociations = new HashMap<String, ScriptEngineFactory>();
        mimeTypeAssociations = new HashMap<String, ScriptEngineFactory>();
        
        try {
            final String resourceName =
                    "META-INF/services/javax.script.ScriptEngineFactory";
            final List<URL> urls = new ArrayList<URL>();
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws IOException {
                        Enumeration<URL> enumResources = getResources(loader, resourceName);
                        while (enumResources.hasMoreElements()) {
                            urls.add(enumResources.nextElement());
                        }
                        return null;
                    }
                });
            } catch (PrivilegedActionException pae) {
                if (DEBUG) pae.printStackTrace();
            }
            
            for (final URL url : urls) {
                try {
                    InputStream stream = (InputStream) AccessController.doPrivileged(
                            new PrivilegedExceptionAction() {
                        public Object run() throws IOException {
                            return url.openStream();
                        }
                    });
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(stream));
                    String line = null;
                    while (null != (line = reader.readLine())) {
                        addSpi(line, loader);
                    }
                } catch (Exception e) {
                    // a single resource URL open/read failed, may be others would succed
                    if (DEBUG) e.printStackTrace();
                    // continue the loop...
                }
            }
        } catch (IOException e) {
            // getResources call failed.
            if (DEBUG) e.printStackTrace();
        }
    }
    
    /**
     * <code>setBindings</code> stores the specified <code>Bindings</code>
     * in the <code>globalScope</code> field.
     * @param bindings The specified <code>Bindings</code>
     */
    public void setBindings(Bindings bindings) {
        if (bindings == null) {
            throw new IllegalArgumentException("Global scope cannot be null.");
        }
        
        globalScope = bindings;
    }
    
    /**
     * <code>getBindings</code> returns the value of the <code>globalScope</code> field.
     * @return The globalScope field.
     */
    public Bindings getBindings() {
        return globalScope;
    }
    
    /**
     * Sets the specified key/value pair in the Global Scope.
     * @param key Key to set
     * @param value Value to set.
     * @throws NullPointerException if key is null
     */
    public void put(String key, Object value) {
        globalScope.put(key, value);
    }
    
    /**
     * Gets the value for the specified key in the Global Scope
     * @param key The key whose value is to be returned.
     * @return The value for the specified key.
     */
    public Object get(String key) {
        return globalScope.get(key);
    }
    
    /**
     * Looks up and creates a <code>ScriptEngine</code> for a given  name.
     * The algorithm first searches for a <code>ScriptEngineFactory</code> that has been
     * registered as a handler for the specified name using the <code>registerEngineName</code>
     * method.
     * <br><br> If one is not found, it searches the array of <code>ScriptEngineFactory</code> instances
     * stored by the constructor for one with the specified name.  If a <code>ScriptEngineFactory</code>
     * is found by either method, it is used to create instance of <code>ScriptEngine</code>.
     * @param shortName The short name of the <code>ScriptEngine</code> implementation.
     * returned by the <code>getName</code> method of its <code>ScriptEngineFactory</code>.
     * @return A <code>ScriptEngine</code> created by the factory located in the search.  Returns null
     * if no such factory was found.  The <code>ScriptEngineManager</code> sets its own <code>globalScope</code>
     * <code>Bindings</code> as the <code>GLOBAL_SCOPE</code> <code>Bindings</code> of the newly
     * created <code>ScriptEngine</code>.
     */
    public ScriptEngine getEngineByName(String shortName) {
        if (shortName == null) throw new NullPointerException();
        //look for registered name first
        Object obj;
        if (null != (obj = nameAssociations.get(shortName))) {
            ScriptEngineFactory spi = (ScriptEngineFactory)obj;
            try {
                ScriptEngine engine = spi.getScriptEngine();
                engine.setBindings(getBindings(), ScriptContext.GLOBAL_SCOPE);
                return engine;
            } catch (Exception exp) {
                if (DEBUG) exp.printStackTrace();
            }
        }
        
        for (ScriptEngineFactory spi : engineSpis) {
            List<String> names = null;
            try {
                names = spi.getNames();
            } catch (Exception exp) {
                if (DEBUG) exp.printStackTrace();
            }
            
            if (names != null) {
                for (String name : names) {
                    if (shortName.equals(name)) {
                        try {
                            ScriptEngine engine = spi.getScriptEngine();
                            engine.setBindings(getBindings(), ScriptContext.GLOBAL_SCOPE);
                            return engine;
                        } catch (Exception exp) {
                            if (DEBUG) exp.printStackTrace();
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Look up and create a <code>ScriptEngine</code> for a given extension.  The algorithm
     * used by <code>getEngineByName</code> is used except that the search starts
     * by looking for a <code>ScriptEngineFactory</code> registered to handle the
     * given extension using <code>registerEngineExtension</code>.
     * @param extension The given extension
     * @return The engine to handle scripts with this extension.  Returns <code>null</code>
     * if not found.
     */
    public ScriptEngine getEngineByExtension(String extension) {
        if (extension == null) throw new NullPointerException();
        //look for registered extension first
        Object obj;
        if (null != (obj = extensionAssociations.get(extension))) {
            ScriptEngineFactory spi = (ScriptEngineFactory)obj;
            try {
                ScriptEngine engine = spi.getScriptEngine();
                engine.setBindings(getBindings(), ScriptContext.GLOBAL_SCOPE);
                return engine;
            } catch (Exception exp) {
                if (DEBUG) exp.printStackTrace();
            }
        }
        
        for (ScriptEngineFactory spi : engineSpis) {
            List<String> exts = null;
            try {
                exts = spi.getExtensions();
            } catch (Exception exp) {
                if (DEBUG) exp.printStackTrace();
            }
            if (exts == null) continue;
            for (String ext : exts) {
                if (extension.equals(ext)) {
                    try {
                        ScriptEngine engine = spi.getScriptEngine();
                        engine.setBindings(getBindings(), ScriptContext.GLOBAL_SCOPE);
                        return engine;
                    } catch (Exception exp) {
                        if (DEBUG) exp.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Look up and create a <code>ScriptEngine</code> for a given mime type.  The algorithm
     * used by <code>getEngineByName</code> is used except that the search starts
     * by looking for a <code>ScriptEngineFactory</code> registered to handle the
     * given mime type using <code>registerEngineMimeType</code>.
     * @param mimeType The given mime type
     * @return The engine to handle scripts with this mime type.  Returns <code>null</code>
     * if not found.
     */
    public ScriptEngine getEngineByMimeType(String mimeType) {
        if (mimeType == null) throw new NullPointerException();
        //look for registered types first
        Object obj;
        if (null != (obj = mimeTypeAssociations.get(mimeType))) {
            ScriptEngineFactory spi = (ScriptEngineFactory)obj;
            try {
                ScriptEngine engine = spi.getScriptEngine();
                engine.setBindings(getBindings(), ScriptContext.GLOBAL_SCOPE);
                return engine;
            } catch (Exception exp) {
                if (DEBUG) exp.printStackTrace();
            }
        }
        
        for (ScriptEngineFactory spi : engineSpis) {
            List<String> types = null;
            try {
                types = spi.getMimeTypes();
            } catch (Exception exp) {
                if (DEBUG) exp.printStackTrace();
            }
            if (types == null) continue;
            for (String type : types) {
                if (mimeType.equals(type)) {
                    try {
                        ScriptEngine engine = spi.getScriptEngine();
                        engine.setBindings(getBindings(), ScriptContext.GLOBAL_SCOPE);
                        return engine;
                    } catch (Exception exp) {
                        if (DEBUG) exp.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Returns an array whose elements are instances of all the <code>ScriptEngineFactory</code> classes
     * found by the discovery mechanism.
     * @return List of all discovered <code>ScriptEngineFactory</code>s.
     */
    public List<ScriptEngineFactory> getEngineFactories() {
        List<ScriptEngineFactory> res = new ArrayList<ScriptEngineFactory>(engineSpis.size());
        for (ScriptEngineFactory spi : engineSpis) {
            res.add(spi);
        }
        return Collections.unmodifiableList(res);
    }
    
    /**
     * Registers a <code>ScriptEngineFactory</code> to handle a language
     * name.  Overrides any such association found using the Discovery mechanism.
     * @param name The name to be associated with the <code>ScriptEngineFactory</code>.
     * @param factory The class to associate with the given name.
     */
    public void registerEngineName(String name, ScriptEngineFactory factory) {
        if (name == null || factory == null) throw new NullPointerException();
        nameAssociations.put(name, factory);
    }
    
    /**
     * Registers a <code>ScriptEngineFactory</code> to handle a mime type.
     * Overrides any such association found using the Discovery mechanism.
     *
     * @param type The mime type  to be associated with the
     * <code>ScriptEngineFactory</code>.
     *
     * @param factory The class to associate with the given mime type.
     */
    public void registerEngineMimeType(String type, ScriptEngineFactory factory) {
        if (type == null || factory == null) throw new NullPointerException();
        mimeTypeAssociations.put(type, factory);
    }
    
    /**
     * Registers a <code>ScriptEngineFactory</code> to handle an extension.
     * Overrides any such association found using the Discovery mechanism.
     *
     * @param extension The extension type  to be associated with the
     * <code>ScriptEngineFactory</code>.
     * @param factory The class to associate with the given extension.
     */
    public void registerEngineExtension(String extension, ScriptEngineFactory factory) {
        if (extension == null || factory == null) throw new NullPointerException();
        extensionAssociations.put(extension, factory);
    }
    
    
    
    /**
     * Utility method to remove everything following "#" from string, strip
     * spaces and attempt to instantiate class whose name is what is left over.
     * Fail silently if class cannot be loaded or does not exist
     * @param line containing class name
     * @param loader used to load engine factory classes
     */
    private void addSpi(String line, ClassLoader loader) {
        int hash = line.indexOf('#');
        if (hash != -1) {
            line = line.substring(0, hash);
        }
        
        String trimmedLine = line.trim();
        if (!trimmedLine.equals("")) {
            String mungedName = null;
            try {
                mungedName = trimmedLine.replace('/', '.');
                Class clasz = Class.forName(mungedName, false, loader);
                if (ScriptEngineFactory.class.isAssignableFrom(clasz)) {
                    Object obj = clasz.newInstance();
                    engineSpis.add((ScriptEngineFactory)obj);
                }
            } catch (Exception e) {
                if (DEBUG) e.printStackTrace();
            }
            
        }
    }
    
    /** Set of script engine factories discovered. */
    private HashSet<ScriptEngineFactory> engineSpis;
    
    /** Map of engine name to script engine factory. */
    private HashMap<String, ScriptEngineFactory> nameAssociations;
    
    /** Map of script file extension to script engine factory. */
    private HashMap<String, ScriptEngineFactory> extensionAssociations;
    
    /** Map of script script MIME type to script engine factory. */
    private HashMap<String, ScriptEngineFactory> mimeTypeAssociations;
    
    /** Global bindings associated with script engines created by this manager. */
    private Bindings globalScope;
    
    private boolean canCallerAccessLoader(ClassLoader loader) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ClassLoader callerLoader = getCallerClassLoader();
            if (callerLoader != null) {
                if (loader != callerLoader || !isAncestor(loader, callerLoader)) {
                    try {
                        sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
                    } catch (SecurityException exp) {
                        if (DEBUG) exp.printStackTrace();
                        return false;
                    }
                } // else fallthru..
            } // else fallthru..
        } // else fallthru..
        
        return true;
    }
    
    // Note that this code is same as ClassLoader.getCallerClassLoader().
    // But, that method is package private and hence we can't call here.
    private ClassLoader getCallerClassLoader() {
        Class caller = Reflection.getCallerClass(3);
        if (caller == null) {
            return null;
        }
        return caller.getClassLoader();
    }
    
    // Returns resources of given name visible from given loader. Handles
    // bootstrap loader as well.
    private Enumeration<URL> getResources(final ClassLoader loader, final String name)
    throws IOException {
        if (loader != null) {
            return loader.getResources(name);
        } else {
            return getBootstrapResources(name);
        }
    }
    
    // get resources of given name from bootstrap loader
    private Enumeration<URL> getBootstrapResources(String name) {
        final Enumeration<Resource> e = Launcher.getBootstrapClassPath().getResources(name);
        return  new Enumeration<URL>() {
            public boolean hasMoreElements() {
                return e.hasMoreElements();
            }
            public URL nextElement() {
                return ((Resource)e.nextElement()).getURL();
            }
        };
    }
    
    // is cl1 ancestor of cl2?
    private boolean isAncestor(ClassLoader cl1, ClassLoader cl2) {
        do {
            cl2 = cl2.getParent();
            if (cl1 == cl2) return true;
        } while (cl2 != null);
        return false;
    }
}
