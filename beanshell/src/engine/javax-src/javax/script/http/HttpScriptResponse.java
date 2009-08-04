/*
 * HttpScriptResponse.java
 *
 * @version 1.0
 * @author Mike Grogan
 * @created on January 13, 2004, 2:17 PM
 */

package javax.script.http;
import javax.servlet.http.*;
/**
 *
 * Provides an abstraction of the current response object to scripts executing 
 * in an HttpScriptEngine.  Its methods are intended to be called directly by scripts.
 */
public interface HttpScriptResponse extends HttpServletResponse /*for now*/{
    
}
