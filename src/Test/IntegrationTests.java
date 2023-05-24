import static org.junit.Assert.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CharStreams;
import org.junit.Test;

public class IntegrationTests {
    private String addFormatting(String input){
        String program = "#include <math.h>\n#include <stdio.h>\n#include <time.h>\n#include \"c_ml_base.c\"\n";
        program += "int main(int argc, char* argv[]){";
        program += "srand(time(NULL));";
        program += input;
        program += "return 0;}";
        return program;
    }

    @Test
    public void testCompileSum(){
        FNNLexer lexer = new FNNLexer(CharStreams.fromString("2+3"));
        FNNParser parser = new FNNParser(new CommonTokenStream(lexer));
        FNNParser.ProgramContext p_tree = parser.program();
        Visitor visitor = new Visitor();
        ToCCompiler compiler = new ToCCompiler();
        String result = compiler.Compile((ProgramNode) visitor.visitProgram(p_tree));
        assertEquals(addFormatting("((2) + (3));"), result);
    }
    @Test
    public void testCompileNeuralNetwork(){
        FNNLexer lexer = new FNNLexer(CharStreams.fromString("(n): NN((in:FLT)-> {RETURN in^2.0} (in:FLT) -> {RETURN 2.0*in}))(100 50 10)"));
        FNNParser parser = new FNNParser(new CommonTokenStream(lexer));
        FNNParser.ProgramContext p_tree = parser.program();
        Visitor visitor = new Visitor();
        ToCCompiler compiler = new ToCCompiler();
        String result = compiler.Compile((ProgramNode) visitor.visitProgram(p_tree));
        String expected = "model_T n = (model_new((3 -1),(layer_new((100),(50),({double FUNC0(double in){double RET_VAL = (pow((in),(2.0)));double RET_VAL_COPY = (RET_VAL);return RET_VAL_COPY;};&FUNC0;}),({double FUNC1(double in){double RET_VAL = ((2.0) * (in));double RET_VAL_COPY = (RET_VAL);return RET_VAL_COPY;};&FUNC1;}))),(layer_new((50),(10),({double FUNC2(double in){double RET_VAL = (pow((in),(2.0)));double RET_VAL_COPY = (RET_VAL);return RET_VAL_COPY;};&FUNC2;}),({double FUNC3(double in){double RET_VAL = ((2.0) * (in));double RET_VAL_COPY = (RET_VAL);return RET_VAL_COPY;};&FUNC3;})))));\n;(model_del(n));";
        assertEquals(addFormatting(expected), result);
    }
    @Test
    public void testCompileTrainStatement(){
        FNNLexer lexer = new FNNLexer(CharStreams.fromString("(train_in): [[5.0 1.0][4.0 2.0]]\n (train_out): [[3.0 1.5][7.0 2.2]]\n (n): NN((in:FLT)-> {RETURN in^2.0} (in:FLT) -> {RETURN 2.0*in})(100 50 10)\nTRAIN((n 1.2 2 train_in train_out)"));
        FNNParser parser = new FNNParser(new CommonTokenStream(lexer));
        FNNParser.ProgramContext p_tree = parser.program();
        Visitor visitor = new Visitor();
        ToCCompiler compiler = new ToCCompiler();
        String result = compiler.Compile((ProgramNode) visitor.visitProgram(p_tree));
        String expected = "double **train_in = ({double **ARR = ass_malloc_fnn_arr(sizeof(double *), 2);ARR[0] = ({double *ARR = ass_malloc_fnn_arr(sizeof(double ), 2);ARR[0] = (5.0);ARR[1] = (1.0);ARR;});ARR[1] = ({double *ARR = ass_malloc_fnn_arr(sizeof(double ), 2);ARR[0] = (4.0);ARR[1] = (2.0);ARR;});ARR;});\n;double **train_out = ({double **ARR = ass_malloc_fnn_arr(sizeof(double *), 2);ARR[0] = ({double *ARR = ass_malloc_fnn_arr(sizeof(double ), 2);ARR[0] = (3.0);ARR[1] = (1.5);ARR;});ARR[1] = ({double *ARR = ass_malloc_fnn_arr(sizeof(double ), 2);ARR[0] = (7.0);ARR[1] = (2.2);ARR;});ARR;});\n;model_T n = (model_new((3 -1),(layer_new((100),(50),({double FUNC0(double in){double RET_VAL = (pow((in),(2.0)));double RET_VAL_COPY = (RET_VAL);return RET_VAL_COPY;};&FUNC0;}),({double FUNC1(double in){double RET_VAL = ((2.0) * (in));double RET_VAL_COPY = (RET_VAL);return RET_VAL_COPY;};&FUNC1;}))),(layer_new((50),(10),({double FUNC2(double in){double RET_VAL = (pow((in),(2.0)));double RET_VAL_COPY = (RET_VAL);return RET_VAL_COPY;};&FUNC2;}),({double FUNC3(double in){double RET_VAL = ((2.0) * (in));double RET_VAL_COPY = (RET_VAL);return RET_VAL_COPY;};&FUNC3;})))));\n;(train_model((n),(1.2),(2),(train_in),(train_out)));for (int INDEX0 = 0; INDEX0 < ((int *)train_in)[-1]; INDEX0++){for (int INDEX1 = 0; INDEX1 < ((int *)train_in[INDEX0])[-1]; INDEX1++){};(ass_free_fnn_arr(train_in[INDEX0]));};(ass_free_fnn_arr(train_in));for (int INDEX0 = 0; INDEX0 < ((int *)train_out)[-1]; INDEX0++){for (int INDEX1 = 0; INDEX1 < ((int *)train_out[INDEX0])[-1]; INDEX1++){};(ass_free_fnn_arr(train_out[INDEX0]));};(ass_free_fnn_arr(train_out));(model_del(n));";
        assertEquals(addFormatting(expected), result);
    }
    @Test
    public void testCompileTestStatement(){
        FNNLexer lexer = new FNNLexer(CharStreams.fromString("(test_in): [[5.0 1.0][4.0 2.0]]\n (test_out): [[3.0 1.5][7.0 2.2]]\n (n): NN((in:FLT)-> {RETURN in^2.0} (in:FLT) -> {RETURN 2.0*in})(100 50 10)\nTEST(n test_in test_out)"));
        FNNParser parser = new FNNParser(new CommonTokenStream(lexer));
        FNNParser.ProgramContext p_tree = parser.program();
        Visitor visitor = new Visitor();
        ToCCompiler compiler = new ToCCompiler();
        String result = compiler.Compile((ProgramNode) visitor.visitProgram(p_tree));
        String expected = "double **test_in = ({double **ARR = ass_malloc_fnn_arr(sizeof(double *), 2);ARR[0] = ({double *ARR = ass_malloc_fnn_arr(sizeof(double ), 2);ARR[0] = (5.0);ARR[1] = (1.0);ARR;});ARR[1] = ({double *ARR = ass_malloc_fnn_arr(sizeof(double ), 2);ARR[0] = (4.0);ARR[1] = (2.0);ARR;});ARR;});\n;double **test_out = ({double **ARR = ass_malloc_fnn_arr(sizeof(double *), 2);ARR[0] = ({double *ARR = ass_malloc_fnn_arr(sizeof(double ), 2);ARR[0] = (3.0);ARR[1] = (1.5);ARR;});ARR[1] = ({double *ARR = ass_malloc_fnn_arr(sizeof(double ), 2);ARR[0] = (7.0);ARR[1] = (2.2);ARR;});ARR;});\n;model_T n = (model_new((3 -1),(layer_new((100),(50),({double FUNC0(double in){double RET_VAL = (pow((in),(2.0)));double RET_VAL_COPY = (RET_VAL);return RET_VAL_COPY;};&FUNC0;}),({double FUNC1(double in){double RET_VAL = ((2.0) * (in));double RET_VAL_COPY = (RET_VAL);return RET_VAL_COPY;};&FUNC1;}))),(layer_new((50),(10),({double FUNC2(double in){double RET_VAL = (pow((in),(2.0)));double RET_VAL_COPY = (RET_VAL);return RET_VAL_COPY;};&FUNC2;}),({double FUNC3(double in){double RET_VAL = ((2.0) * (in));double RET_VAL_COPY = (RET_VAL);return RET_VAL_COPY;};&FUNC3;})))));\n;(test_model((n),(test_in),(test_out)));for (int INDEX0 = 0; INDEX0 < ((int *)test_in)[-1]; INDEX0++){for (int INDEX1 = 0; INDEX1 < ((int *)test_in[INDEX0])[-1]; INDEX1++){};(ass_free_fnn_arr(test_in[INDEX0]));};(ass_free_fnn_arr(test_in));for (int INDEX0 = 0; INDEX0 < ((int *)test_out)[-1]; INDEX0++){for (int INDEX1 = 0; INDEX1 < ((int *)test_out[INDEX0])[-1]; INDEX1++){};(ass_free_fnn_arr(test_out[INDEX0]));};(ass_free_fnn_arr(test_out));(model_del(n));";
        assertEquals(addFormatting(expected), result);
    }
    @Test
    public void testCompileWhileLoop(){
        FNNLexer lexer = new FNNLexer(CharStreams.fromString("WHILE(1){2+1}"));
        FNNParser parser = new FNNParser(new CommonTokenStream(lexer));
        FNNParser.ProgramContext p_tree = parser.program();
        Visitor visitor = new Visitor();
        ToCCompiler compiler = new ToCCompiler();
        String result = compiler.Compile((ProgramNode) visitor.visitProgram(p_tree));
        String expected = "while((1)){((2) + (1));};";
        assertEquals(addFormatting(expected), result);
    }
    @Test
    public void testCompileLoadCSV(){
        FNNLexer lexer = new FNNLexer(CharStreams.fromString("@load_csv: (STR INT INT INT INT) -> (([[FLT]] [[FLT]]))\n(train_o train_i ) : load_csv!(\"mnist_train.csv\" 500 100 13000 6)"));
        FNNParser parser = new FNNParser(new CommonTokenStream(lexer));
        FNNParser.ProgramContext p_tree = parser.program();
        Visitor visitor = new Visitor();
        ToCCompiler compiler = new ToCCompiler();
        String result = compiler.Compile((ProgramNode) visitor.visitProgram(p_tree));
        String expected = "char * (*load_csv)(char *, int , int , int , int ) = &E_load_csv;double **train_o;double **train_i;{char *T_TEMP_O = ((*load_csv)((\"mnist_train.csv\"),(500),(100),(13000),(6)));char *T_TEMP = ({char *COPY = ass_malloc(0+sizeof(double **)+sizeof(double **));(*((double ***)(&(COPY[0+0])))) = ({double **COPY = ass_malloc_fnn_arr(sizeof(double *), ((int *)(*((double ***)(&(T_TEMP_O[0+0])))))[-1]);for (int INDEX1 = 0; INDEX1 < ((int *)(*((double ***)(&(T_TEMP_O[0+0])))))[-1]; INDEX1++){COPY[INDEX1] = ({double *COPY = ass_malloc_fnn_arr(sizeof(double ), ((int *)(*((double ***)(&(T_TEMP_O[0+0]))))[INDEX1])[-1]);for (int INDEX2 = 0; INDEX2 < ((int *)(*((double ***)(&(T_TEMP_O[0+0]))))[INDEX1])[-1]; INDEX2++){COPY[INDEX2] = ((*((double ***)(&(T_TEMP_O[0+0]))))[INDEX1][INDEX2]);}COPY;});}COPY;});(*((double ***)(&(COPY[0+sizeof(double **)+0])))) = ({double **COPY = ass_malloc_fnn_arr(sizeof(double *), ((int *)(*((double ***)(&(T_TEMP_O[0+sizeof(double **)+0])))))[-1]);for (int INDEX1 = 0; INDEX1 < ((int *)(*((double ***)(&(T_TEMP_O[0+sizeof(double **)+0])))))[-1]; INDEX1++){COPY[INDEX1] = ({double *COPY = ass_malloc_fnn_arr(sizeof(double ), ((int *)(*((double ***)(&(T_TEMP_O[0+sizeof(double **)+0]))))[INDEX1])[-1]);for (int INDEX2 = 0; INDEX2 < ((int *)(*((double ***)(&(T_TEMP_O[0+sizeof(double **)+0]))))[INDEX1])[-1]; INDEX2++){COPY[INDEX2] = ((*((double ***)(&(T_TEMP_O[0+sizeof(double **)+0]))))[INDEX1][INDEX2]);}COPY;});}COPY;});COPY;});train_o = (*((double ***)(&(T_TEMP[0+0]))));train_i = (*((double ***)(&(T_TEMP[0+sizeof(double **)+0]))));ass_free(T_TEMP);};for (int INDEX0 = 0; INDEX0 < ((int *)train_i)[-1]; INDEX0++){for (int INDEX1 = 0; INDEX1 < ((int *)train_i[INDEX0])[-1]; INDEX1++){};(ass_free_fnn_arr(train_i[INDEX0]));};(ass_free_fnn_arr(train_i));for (int INDEX0 = 0; INDEX0 < ((int *)train_o)[-1]; INDEX0++){for (int INDEX1 = 0; INDEX1 < ((int *)train_o[INDEX0])[-1]; INDEX1++){};(ass_free_fnn_arr(train_o[INDEX0]));};(ass_free_fnn_arr(train_o));";
        assertEquals(addFormatting(expected), result);
    }
}
