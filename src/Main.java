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
        visitor.visitProgram(p_tree);
    }
}