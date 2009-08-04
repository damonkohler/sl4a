/*
 * SimpleHttpScriptContext.java
 *
 * @version 1.0
 * @author Mike Grogan
 * @Created on March 11, 2004, 10:47 AM
 */

package javax.script.http;
import javax.script.*;
import javax.script.http.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * Generic implementation of <code>HttpScriptContext</code>
 */
public class SimpleHttpScriptContext extends SimpleScriptContext
                                        implements HttpScriptContext {
    
                                            
    protected boolean disableScript;
    protected boolean displayResults;
    protected boolean useSession;
    protected String[] methods;
    protected String[] languages;
    protected String docRoot;
    
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected Servlet servlet;
    
    public static final String[] defaultMethods = {"GET", "POST"};
    
    public SimpleHttpScriptContext() {
        //sets default field values
        release();
    }
    
    public boolean disableScript() {
        return disableScript;
    }
    
    public boolean displayResults() {
        return displayResults;
    }
    
    public void forward(String relativePath) throws ServletException, IOException {
        
        ServletContext context = servlet.getServletConfig().getServletContext();
        String uri = ((HttpServletRequest) request).getServletPath();
        String baseURI = uri.substring(0, uri.lastIndexOf('/'));
        String path = baseURI + '/'+ relativePath;
        context.getRequestDispatcher(path).forward(request, response); 
    }
    
    public String[] getMethods() {
        return methods;
    }
    
    public String[] getAllowedLanguages() {
        return languages;
    }
    
    public HttpServletRequest getRequest() {
        return request;
    }
    
    public HttpServletResponse getResponse() {
        return response;
    }
    
    public java.io.Reader getScriptSource() {
	//we may be included from another page.  The call to request.getRequestURI()
 	//returns the URI for the original request
	String requestURI = (String)request.getAttribute("javax.servlet.include.request_uri");
	if (requestURI == null || requestURI.equals("")) {
		
        	requestURI = request.getRequestURI();
	}

        String resourcePath = 
		requestURI.substring(request.getContextPath().length());
        try {
            if (docRoot != null) {
                String separator = 
                    docRoot.endsWith(File.separator) || 
                    resourcePath.startsWith(File.separator) ?
                    "" :
                    File.separator;
                    
                String fullPath = docRoot + separator + resourcePath;
                return new InputStreamReader(new FileInputStream(fullPath));
            } else {
                 InputStream stream = 
                    servlet.getServletConfig().getServletContext().getResourceAsStream(resourcePath);
                return new BufferedReader(new InputStreamReader(stream));
            }
        } catch (Exception e) {
            return null;
        }
        
    }
    
    public javax.servlet.Servlet getServlet() {
        return servlet;
    }
   

    private void includeOnNewThread(final String path, final ServletContext context)  {
	try {
	 	Runnable r = 	
			 new Runnable() {
				public void run() {
					try {
						context.getRequestDispatcher(path).include(request, response);
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			};

		Thread t = new Thread(r);
		t.start();
		t.join();
		
	} catch (Exception e) {
		e.printStackTrace();
	}
    }

    public void include(String relativePath) throws ServletException, IOException {
        ServletContext context = servlet.getServletConfig().getServletContext();
        String uri = ((HttpServletRequest) request).getServletPath();
        String baseURI = uri.substring(0, uri.lastIndexOf('/'));
        String path = baseURI + '/'+ relativePath;
	
	
        context.getRequestDispatcher(path).include(request, response);
	
	//includeOnNewThread(path, context);
    }
    
    public void initialize(javax.servlet.Servlet servlet, 
                            javax.servlet.http.HttpServletRequest req, 
                            javax.servlet.http.HttpServletResponse res) throws javax.servlet.ServletException {
         request = new RequestWrapper(req);
         response = new ResponseWrapper(res);
         this.servlet = servlet;
         
         ServletContext context = servlet.getServletConfig().getServletContext();
         docRoot = context.getInitParameter("script-directory");
         String disable = context.getInitParameter("script-disable");
         String session = context.getInitParameter("script-use-session");
         String display = context.getInitParameter("script-display-results");
         String methodstring = context.getInitParameter("script-methods");
         String languagesstring = context.getInitParameter("allow-languages");
         
         if (docRoot != null && !(new File(docRoot)).isDirectory()) {
             throw new ServletException("Specified document root ," + docRoot + 
                " either does not exist or is not a directory.");
         }
         
         if (disable != null && disable.equals("true")) {
             disableScript = true;
             return;
         }
         
         if (session != null && session.equals("false")) {
             useSession = false;
         }
         
         if (display != null && display.equals("false")) {
             displayResults = false;
         }
         
         if (methodstring != null) {
             StringTokenizer tokenizer = new StringTokenizer(methodstring, ",");
             ArrayList methodList = new ArrayList();
             while (tokenizer.hasMoreTokens()) {
                 methodList.add(tokenizer.nextToken());
             }
             methods = (String[])methodList.toArray(new String[methodList.size()]);
         }
         
         if (languagesstring != null && !(languagesstring.equals(""))) {
             StringTokenizer tokenizer = new StringTokenizer(languagesstring, ",");
             ArrayList languagesList = new ArrayList();
             while (tokenizer.hasMoreTokens()) {
                 languagesList.add(tokenizer.nextToken());
             }
             languages = (String[])languagesList.toArray(new String[languagesList.size()]);
         } else {
             languages = null;
         }
    }
    
    
    public void release() {
        //restore defaults
        disableScript = false;
        displayResults = true;
        useSession = true;
        methods = defaultMethods;
        request = null;
        response = null;
        servlet = null;
    }
    
    
    private void testScopeValue(int scope) {
        if (scope != REQUEST_SCOPE && 
            scope != SESSION_SCOPE && 
            scope != APPLICATION_SCOPE) {
                throw new IllegalArgumentException("Invalid scope value.");
        } else if (scope == SESSION_SCOPE) {
            HttpSession sess = request.getSession();
            if (!useSession || sess == null) {
                throw new IllegalStateException("Session state disabled.");
            }
        }
    }
    
    public void setAttribute(String key, Object value, int scope) {
        testScopeValue(scope);
        if (scope == REQUEST_SCOPE) {
            request.setAttribute(key,  value);
        } else if (scope == SESSION_SCOPE) {
            request.getSession().setAttribute(key,value);
        } else if (scope == APPLICATION_SCOPE) {
            System.out.println("setting key = " + key + " value = " + value);
            servlet.getServletConfig().getServletContext().setAttribute(key, value);
        }
    }

    //overwrite that in GenericScriptContext
    public Object getAttribute(String key) {
        
        Object ret;
        if (null != (ret = getAttribute(key, REQUEST_SCOPE))) {
            return ret;
        } else if (null != (ret = getAttribute(key, SESSION_SCOPE))) {
            return ret;   
        } else if (null != (ret = getAttribute(key, APPLICATION_SCOPE))) {
            return ret;   
        }
        
        return null;
    }    
    
    public Object getAttribute(String key, int scope) {
        testScopeValue(scope);
        if (scope == REQUEST_SCOPE) {
            return request.getAttribute(key);
        } else if (scope == SESSION_SCOPE) {
            return request.getSession().getAttribute(key);
        } else if (scope == APPLICATION_SCOPE) {
            Object obj = servlet.getServletConfig().getServletContext().getAttribute(key);
            System.out.println("value = " + obj);
            return obj;
        }
        return null;
    }   
    
    public boolean useSession() {
        return useSession;
    }
    
    public Writer getWriter() {
        try {
            return response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public class RequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {
        public RequestWrapper(HttpServletRequest req) {
            super(req);
        }
        
        public HttpSession getSession() {
            if (useSession()) {
                return super.getSession();
            } else {
                return null;
            }
        }
    }
                
  
     
    public class ResponseWrapper extends javax.servlet.http.HttpServletResponseWrapper {
        public ResponseWrapper(HttpServletResponse res) {
            super(res);
        }
       
       //need to use trivial wrapper for cases where we are included from another page using same
       //script engine.  In this case, since RequestDispatcher.include calls response.getOutputStream,
       //outer page fails when it trys to call getWriter.  ServletAPI allow wrapper to be used in
       //which case the IllegalStateException is not thrown.  Need to research what this breaks. 
       public javax.servlet.ServletOutputStream getOutputStream() throws java.io.IOException {
		return super.getOutputStream();
       }
    }

    public List<Integer> getScopes() {
        return scopes;
    }

    private static List<Integer> scopes;
    static {
        scopes = new ArrayList<Integer>();
        scopes.add(REQUEST_SCOPE);
        scopes.add(SESSION_SCOPE);
        scopes.add(APPLICATION_SCOPE);
        scopes.add(ENGINE_SCOPE);
        scopes.add(GLOBAL_SCOPE);
        scopes = Collections.unmodifiableList(scopes);
    }
}
