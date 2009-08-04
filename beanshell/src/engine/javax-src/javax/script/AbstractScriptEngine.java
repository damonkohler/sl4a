/*
 * %W% %E% %U%
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTAIL. Use is subject to license terms.
 */

package javax.script;
import java.io.Reader;
import java.util.Map;
import java.util.Iterator;

/**
 * Provides a standard implementation for several of the variants of the <code>eval</code>
 * method.
 * <br><br>
 * <code><b>eval(Reader)</b></code><p><code><b>eval(String)</b></code><p>
 * <code><b>eval(String, Bindings)</b></code><p><code><b>eval(Reader, Bindings)</b></code>
 * <br><br> are implemented using the abstract methods
 * <br><br>
 * <code><b>eval(Reader,ScriptContext)</b></code> or
 * <code><b>eval(String, ScriptContext)</b></code>
 * <br><br>
 * with a <code>SimpleScriptContext</code>.
 * <br><br>
 * A <code>SimpleScriptContext</code> is used as the default <code>ScriptContext</code>
 * of the <code>AbstractScriptEngine</code>..
 *
 * @author Mike Grogan
 * @version 1.0
 * @since 1.6
 */
public abstract class AbstractScriptEngine  implements ScriptEngine {
    
    /**
     * The default <code>ScriptContext</code> of this <code>AbstractScriptEngine</code>.
     */
    
    protected ScriptContext context;
    
    /**
     * Creates a new instance of AbstractScriptEngine using a <code>SimpleScriptContext</code>
     * as its default <code>ScriptContext</code>.
     */
    public AbstractScriptEngine() {
        
        context = new SimpleScriptContext();
        
    }
    
    /**
     * Creates a new instance using the specified <code>Bindings</code> as the
     * <code>ENGINE_SCOPE</code> <code>Bindings</code> in the protected <code>ScriptContext</code> field.
     *
     * @param n The specified <code>Bindings</code>.
     */
    public AbstractScriptEngine(Bindings n) {
        
        this();
        context.setBindings(n, ScriptContext.ENGINE_SCOPE);
    }
    
    /**
     * Sets the value of the protected <code>ScriptContext</code> field to the specified
     * <code>ScriptContext</code>.
     *
     * @param ctxt The specified <code>ScriptContext</code>.
     */
    public void setContext(ScriptContext ctxt) {
        context = ctxt;
    }
    
    /**
     * Returns the value of the protected <code>ScriptContext</code>> field.
     *
     * @return The value of the protected <code>ScriptContext</code> field.
     */
    public ScriptContext getContext() {
        return context;
    }
    
    /**
     * Returns the <code>Bindings</code> with the specified scope value in
     * the protected <code>ScriptContext</code> field.
     *
     * @param scope The specified scope
     *
     * @return The corresponding <code>Bindings</code>.
     *
     **@throws <code>IllegalArgumentException</code> if the value of scope is
     * invalid for the type the protected <code>ScriptContext</code> field.
     */
    
    public Bindings getBindings(int scope) {
        
        if (scope == ScriptContext.GLOBAL_SCOPE) {
            return context.getBindings(ScriptContext.GLOBAL_SCOPE);
        } else if (scope == ScriptContext.ENGINE_SCOPE) {
            return context.getBindings(ScriptContext.ENGINE_SCOPE);
        } else {
            throw new IllegalArgumentException("Invalid scope value.");
        }
    }
    
    /**
     * Sets the <code>Bindings</code> with the corresponding scope value in the
     * <code>context</code> field.
     *
     * @param bindings The specified <code>Bindings</code>.
     * @param scope The specified scope.
     *
     * @throws <code>IllegalArgumentException</code> if the value of scope is
     * invalid for the type the <code>context</code> field.
     */
    public void setBindings(Bindings bindings, int scope) {
        
        if (scope == ScriptContext.GLOBAL_SCOPE) {
            context.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);;
        } else if (scope == ScriptContext.ENGINE_SCOPE) {
            context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);;
        } else {
            throw new IllegalArgumentException("Invalid scope value.");
        }
    }
    
    /**
     * Sets the specified value with the specified key in the <code>ENGINE_SCOPE</code>
     * <code>Bindings</code> of the protected <code>ScriptContext</code> field.
     *
     * @param key The specified key.
     * @param value The specified value.
     */
    public void put(String key, Object value) {
        
        Bindings nn = getBindings(ScriptContext.ENGINE_SCOPE);
        if (nn != null) {
            nn.put(key, value);
        }
        
    }
    
    /**
     * Gets the value for the specified key in the <code>ENGINE_SCOPE</code> of the
     * protected <code>ScriptContext</code> field.
     *
     * @return The value for the specified key.
     */
    public Object get(String key) {
        
        Bindings nn = getBindings(ScriptContext.ENGINE_SCOPE);
        if (nn != null) {
            return nn.get(key);
        }
        
        return null;
    }
    
    
    /**
     * <code>eval(Reader, Bindings)</code> calls the abstract
     * <code>eval(Reader, ScriptContext)</code> method, passing it a <code>ScriptContext</code>
     * whose Reader, Writers and Bindings for scopes other that <code>ENGINE_SCOPE</code>
     * are identical to those members of the protected <code>ScriptContext</code> field.  The specified
     * <code>Bindings</code> is used instead of the <code>ENGINE_SCOPE</code>
     *
     * <code>Bindings</code> of the <code>context</code> field.
     *
     * @param reader A <code>Reader</code> containing the source of the script.
     * @param bindings A <code>Bindings</code> to use for the <code>ENGINE_SCOPE</code>
     * while the script executes.
     *
     * @return The return value from <code>eval(Reader, ScriptContext)</code>
     */
    public Object eval(Reader reader, Bindings bindings ) throws ScriptException {
        
        ScriptContext ctxt = getScriptContext(bindings);
        
        return eval(reader, ctxt);
    }
    
    
    /**
     * Same as <code>eval(Reader, Bindings)</code> except that the abstract
     * <code>eval(String, ScriptContext)</code> is used.
     *
     * @param script A <code>String</code> containing the source of the script.
     *
     * @param bindings A <code>Bindings</code> to use as the <code>ENGINE_SCOPE</code>
     * while the script executes.
     *
     * @return The return value from <code>eval(String, ScriptContext)</code>
     */
    public Object eval(String script, Bindings bindings) throws ScriptException {
        
        ScriptContext ctxt = getScriptContext(bindings);
        
        return eval(script , ctxt);
    }
    
    /**
     * <code>eval(Reader)</code> calls the abstract
     * <code>eval(Reader, ScriptContext)</code> passing the value of the <code>context</code>
     * field.
     *
     * @param reader A <code>Reader</code> containing the source of the script.
     * @return The return value from <code>eval(Reader, ScriptContext)</code>
     */
    public Object eval(Reader reader) throws ScriptException {
        
        
        return eval(reader, context);
    }
    
    /**
     * Same as <code>eval(Reader)</code> except that the abstract
     * <code>eval(String, ScriptContext)</code> is used.
     *
     * @param script A <code>String</code> containing the source of the script.
     * @return The return value from <code>eval(String, ScriptContext)</code>
     */
    public Object eval(String script) throws ScriptException {
        
        
        return eval(script, context);
    }
    
    /**
     * Returns a <code>SimpleScriptContext</code>.  The <code>SimpleScriptContext</code>:
     *<br><br>
     * <ul>
     * <li>Uses the specified <code>Bindings</code> for its <code>ENGINE_SCOPE</code>
     * </li>
     * <li>Uses the <code>Bindings</code> returned by the abstract <code>getGlobalScope</code>
     * method as its <code>GLOBAL_SCOPE</code>
     * </li>
     * <li>Uses the Reader and Writer in the default <code>ScriptContext</code> of this
     * <code>ScriptEngine</code>
     * </li>
     * </ul>
     * <br><br>
     * A <code>SimpleScriptContext</code> returned by this method is used to implement eval methods
     * using the abstract <code>eval(Reader,Bindings)</code> and <code>eval(String,Bindings)</code>
     * versions.
     *
     * @param nn Bindings to use for the <code>ENGINE_SCOPE</code>
     * @return The <code>SimpleScriptContext</code>
     */
    protected ScriptContext getScriptContext(Bindings nn) {
        
        SimpleScriptContext ctxt = new SimpleScriptContext();
        Bindings gs = getBindings(ScriptContext.GLOBAL_SCOPE);
        
        if (gs != null) {
            ctxt.setBindings(gs, ScriptContext.GLOBAL_SCOPE);
        }
        
        if (nn != null) {
            ctxt.setBindings(nn,
                    ScriptContext.ENGINE_SCOPE);
        } else {
            throw new NullPointerException("Engine scope Bindings may not be null.");
        }
        
        ctxt.setReader(context.getReader());
        ctxt.setWriter(context.getWriter());
        ctxt.setErrorWriter(context.getErrorWriter());
        
        return ctxt;
        
    }
}
