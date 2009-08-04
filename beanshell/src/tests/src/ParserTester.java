import bsh.Interpreter;

public class ParserTester { 
    public static Object eval(String code) {
        Interpreter interpreter = new Interpreter();
        try {
            interpreter.eval(code);
            return null;
        } catch (Exception e) {
            return e;
        }
    }
}
