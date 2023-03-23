import java.io.IOException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CharStreams;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Hellow from Java!");

        if (args.length < 1) {
            System.err.println("U need to specify a filepath!");
            System.exit(1);
        }

        FNNLexer lexer = new FNNLexer(CharStreams.fromFileName(args[0]));
        FNNParser parser = new FNNParser(new CommonTokenStream(lexer));
        FNNParser.ProgramContext p_tree = parser.program();
        Visitor visitor = new Visitor();
        System.out.println("This is result: " + visitor.visitProgram(p_tree));

    }

    public static String Compile_To_C(AstNode Node) {
        switch (Node.Type) {
            case BiOperator: {
                BiOperatorNode n = (BiOperatorNode) Node;
                break;
            }
            case Program: {
                ProgramNode n = (ProgramNode) Node;
                break;
            }
            case UnOperator: {
                UnOperatorNode n = (UnOperatorNode) Node;
                break;
            }
            default:
                break;
        }
        return null;
    }
}