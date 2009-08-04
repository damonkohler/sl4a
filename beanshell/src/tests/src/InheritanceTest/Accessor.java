package InheritanceTest;

public class Accessor {
	public static A getWbyA() {return new W();}
	public static B getWbyB() {return new W();}
	public static A getXbyA() {return new X();}
	public static B getXbyB() {return new X();}
	public static X getX()    {return new X();}
	public static A getYbyA() {return new Y();}
	public static B getYbyB() {return new Y();}
	public static C getYbyC() {return new Y();}
	public static Y getY()    {return new Y();}
	public static A getZbyA() {return new Z();}
	public static B getZbyB() {return new Z();}
	public static C getZbyC() {return new Z();}
}
