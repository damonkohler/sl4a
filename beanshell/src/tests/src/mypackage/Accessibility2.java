package mypackage;

public class Accessibility2 extends Accessibility1 
{
	private Accessibility2(int a, int b, int c) { }
	Accessibility2(int a, int b) { }
	protected Accessibility2(int a ) { }
	public Accessibility2() { }

	private int field1 = 1;
	int field2 = 2;
	protected int field3 = 3;
	public int field4 = 4;

	private int getB1() { return 1; }
	int getB2() { return 2; }
	protected int getB3() { return 3; }
	public int getB4() { return 4; }

	static private int sget1() { return 1; }
	static private int sget1(int a) { return 1; }
	static int sget2() { return 2; }
	static protected int sget3() { return 3; }
	static public int sget4() { return 4; }

	static private int sfield1 = 1;
	static int sfield2 = 2;
	static protected int sfield3 = 3;
	static public int sfield4 = 4;
}

