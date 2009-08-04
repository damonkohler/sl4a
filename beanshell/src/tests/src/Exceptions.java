/**
	test misc. generation of exceptions
*/
public class Exceptions {

	public static void throwRuntime() {
		throw new RuntimeException();
	}

	public static void throwArithmetic() {
		throw new ArithmeticException();
	}

	public static void throwException() throws Exception {
		throw new Exception();
	}

}
