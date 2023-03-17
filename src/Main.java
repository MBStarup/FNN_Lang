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
        Token tok = lexer.nextToken();
        while (tok.getType() != Token.EOF) {
            tok = lexer.nextToken();
            System.out.println(tok);
        }
    }
}