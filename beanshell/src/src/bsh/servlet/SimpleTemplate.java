package bsh.servlet;

/**

	This file is derived from Pat Niemeyer's free utilities package.  
	Now part of BeanShell.

	@see http://www.pat.net/javautil/
	@version 1.0
	@author Pat Niemeyer (pat@pat.net)
*/

import java.io.*;
import java.util.*;
import java.net.URL;

/**
	This is	a simple template engine.  An instance of SimpleTemplate wraps
	a StringBuffer and performs replace operations on one or more parameters
	embedded as HMTL style comments. The value can then be retrieved as a 
	String or written to a stream.

	Template values	in the text are	of the form:

		<!-- TEMPLATE-NAME -->

	Substitutions then take	the form of:

		template.replace( "NAME", value	);

	Two static util	methods	are provided to	help read the text of a	template
	from a stream (perhaps a URL or	resource).  e.g.

	@author	Pat Niemeyer
*/
public class SimpleTemplate 
{
	StringBuffer buff;
	static String NO_TEMPLATE = "NO_TEMPLATE";	// Flag	for non-existent
	static Map templateData	= new HashMap();
	static boolean cacheTemplates =	true;

	/**
		Get a template by name, with caching.
		Note: this should be updated to search the resource path first, etc.

		Create a new instance of a template from the specified file.

		The file text is cached	so lookup is fast.  Failure to find the
		file is	also cached so the read	will not happen	twice.

	*/
	public static SimpleTemplate getTemplate( String file )
	{
		String templateText = (String)templateData.get(	file );

		if ( templateText == null || !cacheTemplates ) {
			try {
				FileReader fr =	new FileReader(	file );
				templateText = SimpleTemplate.getStringFromStream( fr );
				templateData.put( file,	templateText );
			} catch	( IOException e	) {
				// Not found
				templateData.put( file,	NO_TEMPLATE );
			}
		} else
			// Quick check prevents	trying each time
			if ( templateText.equals( NO_TEMPLATE )	)
				return null;

		if ( templateText == null )
			return null;
		else
			return new SimpleTemplate( templateText	);
	}

	public static String getStringFromStream( InputStream ins )
		throws IOException
	{
		return getStringFromStream( new	InputStreamReader( ins ) );
	}

	public static String getStringFromStream( Reader reader	) throws IOException {
		StringBuffer sb	= new StringBuffer();
		BufferedReader br = new	BufferedReader(	reader );
		String line;
		while (	( line = br.readLine() ) != null )
			sb.append( line	+"\n");

		return sb.toString();
	}

	public SimpleTemplate( String template ) {
		init(template);
	}

	public SimpleTemplate( Reader reader ) throws IOException {
		String template	= getStringFromStream( reader );
		init(template);
	}

	public SimpleTemplate( URL url ) throws IOException 
	{
		String template	= getStringFromStream( url.openStream() );
		init(template);
	}

	private void init( String s ) {
		buff = new StringBuffer( s );
	}

	/**
		Substitute the specified text for the parameter
	*/
	public void replace( String param, String value	) {
		int [] range;
		while (	(range = findTemplate( param ))	!= null	)
			buff.replace( range[0],	range[1], value	);
	}

	/**
		Find the starting (inclusive) and ending (exclusive) index of the
		named template and return them as a two	element	int [].
		Or return null if the param is not found.
	*/
	int [] findTemplate( String name ) {
		String text = buff.toString();
		int len	= text.length();

		int start = 0;

		while (	start <	len ) {

			// Find	begin and end comment
			int cstart = text.indexOf( "<!--", start );
			if ( cstart == -1 )
				return null;  // no more comments
			int cend = text.indexOf( "-->",	cstart );
			if ( cend == -1	)
				return null;  // no more complete comments
			cend +=	"-->".length();

			// Find	template tag
			int tstart = text.indexOf( "TEMPLATE-",	cstart );
			if ( tstart == -1 ) {
				start =	cend; // try the next comment
				continue;
			}

			// Is the tag inside the comment we found?
			if ( tstart > cend ) {
				start =	cend; // try the next comment
				continue;
			}

			// find	begin and end of param name
			int pstart = tstart + "TEMPLATE-".length();

			int pend = len;
			for ( pend = pstart; pend < len; pend++) {
				char c = text.charAt( pend );
				if ( c == ' ' || c == '\t' || c	== '-' )
					break;
			}
			if ( pend >= len )
				return null;

			String param = text.substring( pstart, pend );

			// If it's the correct one, return the comment extent
			if ( param.equals( name	) )
				return new int [] { cstart, cend };

//System.out.println( "Found param: {"+param+"}	in comment: "+ text.substring(cstart, cend) +"}");
			// Else	try the	next one
			start =	cend;
		}

		// Not found
		return null;
	}

	public String toString() {
		return buff.toString();
	}

	public void write( PrintWriter out ) {
		out.println( toString()	);
	}

	public void write( PrintStream out ) {
		out.println( toString()	);
	}

	/**
		usage: filename	param value
	*/
	public static void main( String	[] args	) throws IOException
	{
		String filename	= args[0];
		String param = args[1];
		String value = args[2];

		FileReader fr =	new FileReader(	filename );
		String templateText = SimpleTemplate.getStringFromStream( fr );
		SimpleTemplate template	= new SimpleTemplate( templateText );

		template.replace( param, value );
		template.write(	System.out );
	}

	public static void setCacheTemplates( boolean b	) {
		cacheTemplates = b;
	}

}

