
public class Fields {
	public static boolean staticField;
	public static int staticField2;

	public int x = 5;
	public static short shortTwentyTwo = 22;
	public static int propTest = 22;
	
	public static Fields getFields() {
		return new Fields();
	}

	public Fields getFields2() {
		return new Fields();
	}

	// ambiguity in field vs method
	public String ambigName = "field";
	public String ambigName() { return "method"; }
}
