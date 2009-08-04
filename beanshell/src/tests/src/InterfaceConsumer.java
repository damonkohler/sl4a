
public class InterfaceConsumer
{
	public InterfaceConsumer() { }

	public boolean consumeInterface( Interface interf ) {
		String s = interf.getString();
		Integer i = interf.getInteger();
		int i2 = interf.getPrimitiveInt();
		boolean b = interf.getPrimitiveBool();
		assertTrue( interf.getNull() == null );
		return true;
	}
	public static void assertTrue( boolean cond ) {
		if ( !cond ) throw new RuntimeException("assert failed..");
	}
}

