
public class InterfaceImpl implements Interface
{
	public String getString() { return "foo"; }
	public Integer getInteger() { return new Integer(5); }
	public int getPrimitiveInt() { return 7; }
	public boolean getPrimitiveBool() { return true; }
	public Object getNull() { return null; }
}

