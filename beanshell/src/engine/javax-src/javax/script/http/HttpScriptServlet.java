/*
 * HttpScriptServlet.java
 *
 * @version 1.0
 * @author Mike Grogan
 * @reated on March 2, 2004, 3:50 PM
 */

package javax.script.http;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import javax.script.*;
import java.io.Reader;
import java.io.Writer;
import java.util.*;


/**
 * A <code>HttpScriptServlet</code> uses a <code>ScriptEngine</code> supplied by 
 * calling its <code>getEngine</code> method to execute a script in a 
 * <code>HttpScriptContext</code> returned by its <code>getContext</code> method.
 */
public abstract class  HttpScriptServlet extends GenericServlet {
    
    /** 
     * Returns a <code>HttpScriptContext</code> initialized using the specified
     * <code>HttpServletRequest</code>, <code>HttpServletResponse</code> and a
     * reference to this <code>HttpScriptServlet</code>
     * @param req The specified <code>HttpServletRequest</code>.
     * @param res The specified <code>HttpServletResponse</code>.
     * @return the initialized <code>HttpScriptContext</code>.
     * @throws ServletException if error occurrs
     */
    public abstract HttpScriptContext getContext(HttpServletRequest req,
                                                 HttpServletResponse res)
                                                 throws ServletException;

    /**
     * Returns a <code>ScriptEngine</code> that is used by the <code>HttpScriptServlet</code> 
     * to execute a single request.<p> 
     * The implementation must ensure that if the same engine is used to service 
     * requests concurrently on multiple threads that side-effects of script execution 
     * on any thread will not be visible in the engine scopes of other threads.  This
     * will be the case if the returned <code>ScriptEngine</code> implements a class 
     * associated with a <code>ScriptEngineInfo</code> where either  
     * <code>getParameter("THREAD-ISOLATED")</code> or <code>getParameter("STATELESS")</code> returns
     * <code>java.lang.Boolean.TRUE</code>.
     * @param request The current request.
     * @return The <code>ScriptEngine</code> used by this <code>HttpScriptServlet</code> to
     * execute requests.
     */
    public abstract ScriptEngine getEngine(HttpServletRequest request);
    
    /**
     * Called to indicate that a <code>ScriptEngine</code> retruned by a call to
     * <code>getEngine</code> is no longer in use.
     * @param engine The <code>ScriptEngine</code>
     */
    public abstract void releaseEngine(ScriptEngine engine);
    
    /**
     * Executes a request using the <code>HttpScriptContext</code> returned by
     * <code>getContext</code> and the <code>ScriptEngine</code> returned by 
     * <code>getEngine</code>.
     * <p>
     * A default implementation is provided:
     * <p>
     * @param req The current request.  Must be an instance of <code>HttpServletRequest</code>.
     * @param res The current response.  Must be an instance of <code>HttpServletResponse</code>.
     * @throws IllegalArgumentException If either req is not an instance of <code>HttpServletRequest</code>
     * or res is not an instance of <code>HttpServletResponse</code>
     * @throws ServletException
     * @throws IOException
     */
     
    public void service(ServletRequest req, ServletResponse res)
        throws ServletException, IOException {
            if (!(req instanceof HttpServletRequest) || 
                !(res instanceof HttpServletResponse)) {
                throw new IllegalArgumentException();
            }
           
            HttpServletRequest httpreq = (HttpServletRequest)req;
            HttpServletResponse httpres = (HttpServletResponse)res;
	    Writer httpwriter =  httpres.getWriter();
            
            HttpScriptContext ctxt = getContext( httpreq, httpres);
            
            //if script-disable init parameter is set return "403 Forbidden"
            if (ctxt.disableScript()) {
                httpres.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            //if request method is not among those listed in script-method
            //init parameter, return "405 Method Not Allowed"
            String[] methods = ctxt.getMethods();
            int numMethods = methods.length;
            String method = httpreq.getMethod();
            
            int i = 0;
            for (i = 0; i < numMethods; i++) {
                if (method.compareToIgnoreCase(methods[i]) == 0) {
                    break;
                }
            }
            
            if (i == numMethods) {
                httpres.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
            
            
            ScriptEngine engine = null;
            
            try {
                
                engine = getEngine(httpreq);
                
                //if allowable languages are constrained in the configuration
                //check that the engine handles one of the allowed ones.
                String[] languages = ctxt.getAllowedLanguages();
                
                
                if (languages != null) {
                    
                    checkLanguages: {
                        List<String> names = engine.getFactory().getNames();
                        int lenLanguages = languages.length;

                        for (String name : names) {
                            for (int j = 0; j < lenLanguages; j++) {
                                if (name.equals(languages[j])) {
                                    break checkLanguages;
                                }
                            }
                        }
                        httpres.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                }
                
                Reader reader = ctxt.getScriptSource();
                
                ctxt.getBindings(ScriptContext.ENGINE_SCOPE).put(ScriptEngine.FILENAME, 
                                                            httpreq.getRequestURI());
                
                if (reader == null) {
                    httpres.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                         
                //add required objects to script bindings
                
                ctxt.getBindings(ScriptContext.ENGINE_SCOPE).put("request", ctxt.getRequest());
                ctxt.getBindings(ScriptContext.ENGINE_SCOPE).put("response", ctxt.getResponse());
                ctxt.getBindings(ScriptContext.ENGINE_SCOPE).put("context", ctxt);
                ctxt.getBindings(ScriptContext.ENGINE_SCOPE).put("servlet", this);

                res.setContentType("text/html");
                
                //eval the script
                Object ret = engine.eval(ctxt.getScriptSource(), ctxt);
                
                //display the toString value of the return value unless 
                //the script-display-results InitParameter is "false"
                if (ret != null && ctxt.displayResults()) {
                    httpwriter.write(ret.toString());
                }
                
                
                httpwriter.flush();
		reader.close();
                
            } catch (ScriptException e) {
                httpres.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                throw new ServletException(e);
            } finally {
                
                if (engine != null) {
                    releaseEngine(engine);
                }
                
                ctxt.release();
            }
    }

}
