import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.io.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CharStreams;

public class Main {
    public static void main(String[] argv) throws IOException, InterruptedException {
        CommandArgs args = new CommandArgs();

        String filepath = null;

        args.AddFlag("--no-remove-c", "doesn't remove the intermediary c source file");
        args.AddFlag("--no-invoke-gcc", "doesn't invoke gcc");
        args.AddFlag("--gcc-output", "prints the output from gcc");
        args.AddProp("-o", "a.exe", "the name of the output (default: a.exe)");
        try {
            filepath = args.Parse(argv).get(0);
        } catch (Exception e) {
            Utils.ERREXIT("You need to specify a filepath\nUsage: java -jar FNNC.jar <options> <filepath>\nOptions:\n" + args.toString());
        }
        var name = args.GetProp("-o");
        var c_name = "FNN_OUT.c";

        FNNLexer lexer = new FNNLexer(CharStreams.fromFileName(filepath));
        FNNParser parser = new FNNParser(new CommonTokenStream(lexer));
        FNNParser.ProgramContext p_tree = parser.program();
        Visitor visitor = new Visitor();
        ToCCompiler compiler = new ToCCompiler();
        Writer fw = new FileWriter(c_name, false);

        fw.write(compiler.Compile((ProgramNode) visitor.visitProgram(p_tree)));
        fw.close();
        if (!(args.IsSet("--no-invoke-gcc"))) {

            var process = new ProcessBuilder("gcc", c_name, "-lm", "-O4", "-o", name).start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            process.waitFor();
            if (args.IsSet("--gcc-output")) {
                String line;
                System.out.printf("gcc output:\n");
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
                System.out.println("\n");
            }
        }

        if (!(args.IsSet("--no-remove-c")))
            new File(c_name).delete();

        System.out.println("Done.");
    }
}
