
import org.apache.bsf.*;
import java.util.Vector;

public class TestBshBSF 
{
	public static void main( String [] args ) 
		throws BSFException
	{
		BSFManager mgr = new BSFManager();

		// register beanshell with the BSF framework
		String [] extensions = { "bsh" };
		mgr.registerScriptingEngine( 
			"beanshell", "bsh.util.BeanShellBSFEngine", extensions );

		mgr.declareBean("foo", "fooString", String.class);
		mgr.declareBean("bar", "barString", String.class);
		mgr.registerBean("gee", "geeString");
		
		BSFEngine beanshellEngine = mgr.loadScriptingEngine("beanshell");

		String script = "foo + bar + bsf.lookupBean(\"gee\")";
		Object result = beanshellEngine.eval( "Test eval...", -1, -1, script );

		assertTrue( result.equals("fooStringbarStringgeeString" ) );

		// test apply()
		Vector names = new Vector();
		names.addElement("name");
		Vector vals = new Vector();
		vals.addElement("Pat");

		script = "name + name";
		
		result = beanshellEngine.apply( 
			"source string...", -1, -1, script, names, vals );
	
		assertTrue( result.equals("PatPat" ) );

		result = beanshellEngine.eval( "Test eval...", -1, -1, "name" );

		// name should not be set 
		assertTrue( result == null );

		// Verify the primitives are unwrapped
		result = beanshellEngine.eval( "Test eval...", -1, -1, "1+1");

		assertTrue( result instanceof Integer 
			&& ((Integer)result).intValue() == 2 );
	}

	static void assertTrue( boolean cond ) {
		if ( cond )
			System.out.println("Passed...");
		else
			throw new Error("assert failed...");
	}
	
}

