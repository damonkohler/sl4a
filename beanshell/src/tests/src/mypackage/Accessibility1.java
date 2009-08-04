package mypackage;

class Accessibility1 {

	private Accessibility1(int a, int b, int c) { }
	Accessibility1(int a, int b) { }
	protected Accessibility1(int a ) { }
	public Accessibility1() { }

	private int field1 = 1;
	int field2 = 2;
	protected int field3 = 3;
	public int field4 = 4;

	private int get1() { return 1; }
	private int get1(int a) { return 1; }
	int get2() { return 2; }
	protected int get3() { return 3; }
	public int get4() { return 4; }

	static private int supersget1() { return 1; }
	static private int supersget1(int a) { return 1; }
	static int supersget2() { return 2; }
	static protected int supersget3() { return 3; }
	static public int supersget4() { return 4; }

	static private int supersfield1 = 1;
	static int supersfield2 = 2;
	static protected int supersfield3 = 3;
	static public int supersfield4 = 4;

}

