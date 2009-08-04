/*
 * %W% %E% %U%
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTAIL. Use is subject to license terms.
 */

package javax.script;

import java.util.*;
import java.io.*;

/**
 * Simple implementation of ScriptContext.
 *
 * @author Mike Grogan
 * @version 1.0
 * @since 1.6
 */
public class SimpleScriptContext  implements ScriptContext {
    
    /**
     * By default, a <code>PrintWriter</code> based on <code>System.out</code>
     * is used.
     */
    protected Writer writer;
    
    /**
     * By default, a <code>PrintWriter</code> based on <code>System.err</code> is
     * used.
     */
    protected Writer errorWriter;
    
    /**
     * By default, a <code>InputStreamReader</code> based on <code>System.in</code>
     * is used.
     */
    protected Reader reader;
    
    
    /**
     * By default, a <code>SimpleBindings</code> is used.
     */
    protected Bindings engineScope;
    
    /**
     * By default, a <code>SimpleBindings</code> is used.
     */
    protected Bindings globalScope;
    
    
    public SimpleScriptContext() {
        
        engineScope = new SimpleBindings();
        globalScope = null;
        reader = new InputStreamReader(System.in);
        writer = new PrintWriter(System.out , true);
        errorWriter = new PrintWriter(System.err, true);
        
    }
    
    /**
     * Sets a <code>Bindings</code> of attributes for the given scope.  If the value
     * of scope is <code>ENGINE_SCOPE</code> the given <code>Bindings</code> replaces the
     * <code>engineScope</code> field.  If the value
     * of scope is <code>GLOBAL_SCOPE</code> the given <code>Bindings</code> replaces the
     * <code>globalScope</code> field.
     *
     * @param bindings The <code>Bindings</code> of attributes to set.
     * @param scope The value of the scope in which the attributes are set.
     *
     * @throws <code>IllegalArgumentException</code> if scope is invalid.
     * @throws <code>NullPointerException</code> is the value of scope is <code>ENGINE_SCOPE</code> and
     * the specified <code>Bindings</code> is null.
     */
    public void setBindings(Bindings bindings, int scope) {
        
        switch (scope) {
            
            case ENGINE_SCOPE:
                if (bindings == null) {
                    throw new NullPointerException("Engine scope cannot be null.");
                }
                engineScope = bindings;
                break;
            case GLOBAL_SCOPE:
                globalScope = bindings;
                break;
            default:
                throw new IllegalArgumentException("Invalid scope value.");
        }
    }
    
    
    
    public Object getAttribute(String name) {
        
        Object ret;
        if (null != (ret = getAttribute(name, ENGINE_SCOPE))) {
            return ret;
        } else if (null != (ret = getAttribute(name, GLOBAL_SCOPE))) {
            return ret;
        }
        
        return null;
    }
    
    public Object getAttribute(String name, int scope) {
        
        switch (scope) {
            
            case ENGINE_SCOPE:
                return engineScope.get(name);
                
            case GLOBAL_SCOPE:
                if (globalScope != null) {
                    return globalScope.get(name);
                }
                return null;
                
            default:
                throw new IllegalArgumentException("Illegal scope value.");
        }
    }
    
    public Object removeAttribute(String name, int scope) {
        
        switch (scope) {
            
            case ENGINE_SCOPE:
                if (getBindings(ENGINE_SCOPE) != null) {
                    return getBindings(ENGINE_SCOPE).remove(name);
                }
                return null;
                
            case GLOBAL_SCOPE:
                if (getBindings(GLOBAL_SCOPE) != null) {
                    return getBindings(GLOBAL_SCOPE).remove(name);
                }
                return null;
                
            default:
                throw new IllegalArgumentException("Illegal scope value.");
        }
    }
    
    public void setAttribute(String name, Object value, int scope) {
        
        
        switch (scope) {
            
            case ENGINE_SCOPE:
                engineScope.put(name, value);
                return;
                
            case GLOBAL_SCOPE:
                if (globalScope != null) {
                    globalScope.put(name, value);
                }
                return;
                
            default:
                throw new IllegalArgumentException("Illegal scope value.");
        }
    }
    
    public Writer getWriter() {
        return writer;
    }
    public Reader getReader() {
        return reader;
    }
    
    public void setReader(Reader reader) {
        this.reader = reader;
    }
    
    public void setWriter(Writer writer) {
        this.writer = writer;
    }
    
    public Writer getErrorWriter() {
        return errorWriter;
    }
    
    public void setErrorWriter(Writer writer) {
        this.errorWriter = writer;
    }
    
    public int getAttributesScope(String name) {
        
        if (getAttribute(name, ENGINE_SCOPE) != null) {
            return ENGINE_SCOPE;
        } else if (globalScope != null && getAttribute(name, GLOBAL_SCOPE) != null) {
            return GLOBAL_SCOPE;
        } else {
            return 0;
        }
    }
    
    /**
     * Returns the value of the <code>engineScope</code> field if specified scope is
     * <code>ENGINE_SCOPE</code>.  Returns the value of the <code>globalScope</code> field if the specified scope is
     * <code>GLOBAL_SCOPE</code>.
     *
     * @param scope The specified scope
     * @return The value of either the  <code>engineScope</code> or <code>globalScope</code> field.
     * @throws <code>IllegalArgumentException</code> if the value of scope is invalid.
     */
    public Bindings getBindings(int scope) {
        if (scope == ENGINE_SCOPE) {
            return engineScope;
        } else if (scope == GLOBAL_SCOPE) {
            return globalScope;
        } else {
            throw new IllegalArgumentException("Illegal scope value.");
        }
        
    }
    
    public List<Integer> getScopes() {
        return scopes;
    }
    
    private static List<Integer> scopes;
    static {
        scopes = new ArrayList<Integer>(2);
        scopes.add(ENGINE_SCOPE);
        scopes.add(GLOBAL_SCOPE);
        scopes = Collections.unmodifiableList(scopes);
    }
}
