
public class Base 
{
	public String s = null;
	public int i;

	public Base() { }
	public Base( String s ) { this.s = s; }
	public Base( int i ) { this.i = i; }
	public Base( String s, int i ) { 
		this.s = s; 
		this.i = i; 
	}

	public String baseMethod() {
		return "baseMethod";
	}
	public String baseMethod2() {
		return "baseMethod2";
	}
	public String baseMethod3() {
		return "baseMethod3";
	}
}
