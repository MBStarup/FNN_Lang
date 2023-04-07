import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.io.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CharStreams;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hellow from Java!");

        if (args.length < 1) {
            System.err.println("U need to specify a filepath!");
            System.exit(1);
        }

        FNNLexer lexer = new FNNLexer(CharStreams.fromFileName(args[0]));
        FNNParser parser = new FNNParser(new CommonTokenStream(lexer));
        FNNParser.ProgramContext p_tree = parser.program();
        Visitor visitor = new Visitor();
        ToCCompiler compiler = new ToCCompiler();
        Writer fw = new FileWriter("out.c", false);

        fw.write(compiler.Compile((ProgramNode) visitor.visitProgram(p_tree)));
        fw.close();
        // var process = new ProcessBuilder("gcc", "out.c").start();

        // // Print gcc output
        // BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        // process.waitFor();
        // System.out.printf("gcc output:");
        // String line;
        // while ((line = reader.readLine()) != null) {
        // System.out.println(line);
        // }
        // end print gcc output

        // new File("out.c").delete();
    }
}