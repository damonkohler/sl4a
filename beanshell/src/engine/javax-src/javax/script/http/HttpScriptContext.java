/*
 * HttpScriptContext.java
 *
 * @author Mike Grogan
 * @version 1.0
 * @Created on December 18, 2003, 9:26 PM
 */

package javax.script.http;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.Reader;
import java.io.IOException;
import javax.script.*;

/**
 * Classes implementing the <code>HttpScriptContext</code> interface connect a
 * <code>ScriptEngine</code> with the implicit objects from a Servlet Container.  
 * <br><br>
 * The interface contains methods that allow a <code>HttpScriptServlet</code> to initialize the 
 * <code>HttpScriptContext</code> with the implicit objects for each request, 
 * and methods that allow a <code>ScriptEngine</code> to access them.
 * <p>The objects may be used by internal constructs related to the web environment in
 * the scripting languages, and are also generally exposed in the namespaces 
 * of scripts executing in the engines.
 *
 */

public interface HttpScriptContext extends ScriptContext {
   
    /**
     * RequestScope attributes are visible during the processing
     * of a single request.  
     */
    public static final int REQUEST_SCOPE = 0;
    
    /**
     * SessionScope attributes are visible during the processing
     * of all requests belonging to a single <code>HttpSession</code> 
     * during the lifetime of the session.
     */
    public static final int SESSION_SCOPE = 150;
    
    
    /**
     * ApplicationScope attributes are visible during the processing 
     * of all requests belonging to a single Web Application.
     */
    public static final int APPLICATION_SCOPE = 175;
    
    
    /**
     * Initializes this <code>HttpScriptContext</code> for processing of a single
     * request.  Implementations must initialize attributes in <code>REQUEST_SCOPE</code>, 
     * <code>SESSION_SCOPE</code> and <code>APPLICATION_SCOPE</code>  
     * and store servlet, request and response references for use by the <code>ScriptEngine</code> 
     * executing the request.
     * @param servlet The <code>HttpScriptServlet</code> executing the request
     * @param req The current request.
     * @param res The current response.
     * @throws ServletException If a <code>ScriptExcepton</code> is thrown by the <code>ScriptEngine</code>
     * during the processing of a request.
     */
    public void initialize(Servlet servlet, 
                            HttpServletRequest req, 
                            HttpServletResponse res) throws ServletException;
    
    /**
     * Clears any state stored in this <code>HttpScriptContext</code>, allowing its
     * reuse by other requests.
     */
    public void release();
    
    /**
     * Gets the value of the attribute in the specified scope.  Initially, the
     * <code>REQUEST_SCOPE</code>, <code>SESSION_SCOPE</code> and 
     * <code>APPLICATION_SCOPE</code> should be initialized using the values of the 
     * request attributes, session attributes and context attributes associated 
     * with the current request.  Also, the following mappings should 
     * be included in the initial values of the <code>REQUEST_SCOPE</code> attributes:
     *<br><br>
     * <center>
     * <table border="1">
     * <tr><td><b>Name</b></td><td><b>Value</b></td></tr>
     * <tr><td>Request</td><td>return value of <code>getRequest</code></td></tr>
     * <tr><td>Response</td><td>return value of <code>getResponse</code></td></tr>
     * <tr><td>Servlet</td><td>return value of <code>getServlet</code></td></tr>
     * </table>
     * </center>
     * <br><br>
     * Attempts to access <code>SESSION_SCOPE</code>
     * attributes should return <code>null</code> if <code>useSession</code> 
     * returns <code>false</code> or if the current session is invalid.
     * @param name The name of the attribute to get
     * @param scope The scope used to find the attribute
     * @return the value of the attribute.
     * @throws IllegalArgumentException if scope is invalid.
     */  
    public Object getAttribute(String name, int scope);
    
    /**
     * Returns the value of the attribute in the lowest scope for which
     * the attribute is defined.  Return <code>null</code> if an attribute
     * with the given name is not defined in any scope.
     */  
     
    public Object getAttribute(String name);
    
    /**
     * Sets the value of the named attribute in the specified scope.
     * Calls using <code>REQUEST_SCOPE</code>, <code>SESSION_SCOPE</code>
     * and <code>APPLICATION_SCOPE</code> should set the corresponding
     * request attribute, session attribute or context attribute for the current request.
     * @param name The name of the attribute to set.
     * @param value The value of the attribute to set.
     * @param scope The scope in which to set the attribute.
     * @throws IllegalArgumentException if scope is invalid.
     * @throws IllegalStateException if scope is <code>SESSION_SCOPE</code>
     * and session is either invalid or disabled according to the return
     * value of <code>useSession</code>.
     */
    public void setAttribute(String name, Object value, int scope);
    /**
     * Returns a Reader from which the source of the script used to execute the 
     * current request can be read.  This may be obtained from a resource in the current 
     * Web Application whose name is derived from the URI of the current request, a 
     * script directory in the filesystem specified in the configuration of the 
     * Web Application or in some implementation-defined way.
     */
    public Reader getScriptSource();
    
    /**
     * Returns the <code>HttpServoetRequest</code> for the current request.  If the
     * use of session state is disabled using the  <tt>script-use-session</tt> 
     * initialization parameter, an adapter whose <code>getSession</code> method 
     * returns <code>null</code> must be returned. 
     * 
     * @return The current request
     */
    public HttpServletRequest getRequest();
    
    
    
    /**
     * Returns the <code>HttpServletResponse</code> for the current request.
     *
     * @return The current response
     */
    public HttpServletResponse getResponse();
    
    
    
    /**
     * Returns the <code>HttpScriptServlet</code> using the <code>HttpScriptContext</code>.
     *  
     * @return The current servlet
     */
    public Servlet getServlet();
    
   
    /**
     * Forward the request to the resource identified by the specified 
     * relative path.
     * @param relativePath The URI to process the request.  The path 
     * is resolved according to the rules specified in the <code>forward</code> 
     * method of <code>javax.servlet.jsp.PageContext</code>.
     * @throws ServletException
     */
    public void forward(String relativePath) throws ServletException, IOException;
    
    /**
     * Includes the resource identified by the specified 
     * relative path.
     * @param relativePath The URI to process the request.  The path 
     * is resolved according to the rules specified in the <code>include</code> 
     * method of <code>javax.servlet.jsp.PageContext</code>.
     * @throws ServletException
     */
    public void include(String relativePath) throws ServletException, IOException;
    
    
    
    /**
     * Return value indicates whether script execution has been 
     * disabled in the Web Application.  This is determined by the value 
     * of the application's initialization parameter <tt>script-disable</tt>.
     * @return <code>false</code> unless the value of that parameter is "true".  
     * Returns <code>true</code> in that case.
     */
    public boolean disableScript();
    
    /**
     * Return value indicates whether the <code>HttpSession</code> associated 
     * with the current request is exposed in the SESSION_SCOPE attributes and
     * in the <code>HttpScriptRequest</code> returned by <code>getRequest</code>.
     * This is determined by the value of the <tt>script-use-session</tt> Web Application 
     * initialization parameter.
     * @return <code>true</code> unless the value of the <tt>script-use-session</tt> 
     * parameter is "false".  Returns <code>false</code> in that case. 
     * 
     */
    public boolean useSession();
    
    /**
     * Return value indicates whether a <code>HttpScriptServlet</code> executing in this context 
     * should display the results of script evaluations.<p>  If <code>true</code>, the
     * servlet should display the <code>toString</code> of the value returned by script execution using the 
     * Writer returned by the <code>getWriter</code> method.
     * <p>
     * The value is determined by the <tt>script-display-results</tt> initialization parameter.
     * @return <code>true</code> unless the value of the <tt>script-display-results</tt> 
     * initialization parameter is "false".  Returns <code>false</code> in that case.
     */
    public boolean displayResults();
    
    /**
     * An array of Strings describing the HTTP request methods handled by 
     * <code>HttpScriptServlets</code> executing in this context.  The value is determined
     * by the value of the <tt>script-methods</tt> Web Application initialization parameter.
     * @return An array of (case-insensitive) method names.  The elements of the array are 
     * determined by the <tt>script-methods</tt> initialization parameter, which is a comma-delimited 
     * list of the names.
     */
    public String[] getMethods();
    
    /**
     * An array of Strings naming the languages that may be used by scripts running 
     * in this <code>HttpScriptContext</code>.  The value is obtained from the 
     * <tt>allow-languages</tt> initialization parameter.  A return value of <code>null</code> 
     * indicates that there is no restriction on script language.
     * @return An array of allowed script languages. 
     */
    public String[] getAllowedLanguages();
}
