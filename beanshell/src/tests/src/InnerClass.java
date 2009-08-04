
public class InnerClass {

	public static class Inner {
		public int x = 5;
		public static int y=6;

		public Inner() { }

		public static class Inner2 {
			public Inner2() { }
		}
	}

}
