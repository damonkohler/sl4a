
public class MethodSelection {

	public Class constructedWith;

	// constructors 

	public MethodSelection( Object o ) {
		constructedWith = o.getClass();
		System.out.println("selected object constr");
	}
	public MethodSelection( String o ) {
		constructedWith = o.getClass();
		System.out.println("selected string constr");
	}
	public MethodSelection( long o ) {
		constructedWith = Long.TYPE;
		System.out.println("selected long constr");
	}
	public MethodSelection( int o ) {
		constructedWith = Integer.TYPE;
		System.out.println("selected int constr");
	}
	public MethodSelection( byte o ) {
		constructedWith = Byte.TYPE;
		System.out.println("selected byte constr");
	}
	public MethodSelection( short o ) {
		constructedWith = Short.TYPE;
		System.out.println("selected short constr");
	}
	public MethodSelection() {
		constructedWith = Void.TYPE;
		System.out.println("no args constr");
	}

	// static method selection

	public static Class get_static( Object o ) {
		System.out.println("selected object method");
		return o.getClass();
	}
	public static Class get_static( String o ) {
		System.out.println("selected string method");
		return o.getClass();
	}
	public static Class get_static( int o ) {
		System.out.println("selected int method");
		return Integer.TYPE;
	}
	public static Class get_static( long o ) {
		System.out.println("selected long method");
		return Long.TYPE;
	}
	public static Class get_static( byte o ) {
		System.out.println("selected byte method");
		return Byte.TYPE;
	}
	public static Class get_static( short o ) {
		System.out.println("selected short method");
		return Short.TYPE;
	}
	public static Class get_static() {
		System.out.println("selected no args method");
		return Void.TYPE;
	}

	// dynamic method selection

	public Class get_dynamic( Object o ) {
		System.out.println("selected object method");
		return o.getClass();
	}
	public Class get_dynamic( String o ) {
		System.out.println("selected string method");
		return o.getClass();
	}
	public Class get_dynamic( int o ) {
		System.out.println("selected int method");
		return Integer.TYPE;
	}
	public Class get_dynamic( long o ) {
		System.out.println("selected long method");
		return Long.TYPE;
	}
	public Class get_dynamic( byte o ) {
		System.out.println("selected byte method");
		return Byte.TYPE;
	}
	public Class get_dynamic( short o ) {
		System.out.println("selected short method");
		return Short.TYPE;
	}
	public Class get_dynamic() {
		System.out.println("selected no args method");
		return Void.TYPE;
	}

	/*
		If we try to invoke an instance method through a static context
		javac will error... rather than take the widening match.
		See methodselection2.bsh 
	*/
	public static Class staticVsDynamic1( Object obj ) {
		System.out.println("Object");
		return Object.class;
	}
	public Class staticVsDynamic1( String obj ) {
		System.out.println("String");
		return String.class;
	}

	public static void main( String [] args ) {
		System.out.println("should be string");
		new MethodSelection().staticVsDynamic1( "foo" );

		System.out.println("should be object");
		new MethodSelection().staticVsDynamic1( new Object() );

	}

	private String foo( Integer x ) { return "private"; }
	public String foo( String x ) { return "public"; }

}
