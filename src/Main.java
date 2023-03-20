import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.swing.LayoutStyle.ComponentPlacement;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Hellow from Java!");
        FNNLexer lexer = new FNNLexer(CharStreams.fromFileName(args[0]));
        FNNParser parser = new FNNParser(new CommonTokenStream(lexer));
        FNNParser.ProgramContext p_tree = parser.program();
        Visitor uselessVisitor = new Visitor();
        uselessVisitor.visitProgram(p_tree);

    }
}

class Visitor extends FNNBaseVisitor<Object>
{
    @Override public Object visitProgram(FNNParser.ProgramContext ctx) 
    { 
        System.out.println("visiting Programme");
        System.out.println(ctx.getText());
        return this.visitChildren(ctx);
    }

    @Override public Object visitType(FNNParser.TypeContext ctx) 
    {
        System.out.println("Visiting tüüp"); 
        return this.visitChildren(ctx); 
    }
}