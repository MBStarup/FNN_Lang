import org.junit.*;
import org.junit.runners.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.runner.RunWith;
import org.mockito.*;

import java.util.*;


public class TestToCCompiler {
    ToCCompiler CCompiler = new ToCCompiler();

    @Test
    public void testCompileProgramNode() {
        String programContent = "#include <math.h>\n#include <stdio.h>\n#include <time.h>\n#include \"c_ml_base.c\"\nint main(int argc, char* argv[]){srand(time(NULL));return 0;}";
        ProgramNode program = new ProgramNode();
        assertEquals(programContent, CCompiler.Compile(program));
    }
    @Test
    public void testCompileEmptyStmtList() {
        List<StmtNode> stmtList = new Vector<StmtNode>();

        assertTrue(CCompiler.Compile(stmtList).isEmpty());
    }
    @Test
    public void testCompileNonEmptyStmtList() {
        List<StmtNode> stmtList = new Vector<StmtNode>();
        stmtList.add(new EvalNode());
        stmtList.add(new EvalNode());

        assertFalse(CCompiler.Compile(stmtList).isEmpty());
    }
    @Test
    public void testComplieStmtExpr() {
        EvalNode evalNode = new EvalNode();

        assertFalse(CCompiler.Compile(evalNode).isEmpty());
    }
    @Test
    public void testComplieStmtAssign() {
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        AssignNode assignNode = new AssignNode();
        when(CCompiler.Compile(any(AssignNode.class))).thenReturn("true");
        assertEquals("true", CCompiler.Compile(assignNode));
    }
    @Test
    public void testCompileStmtTrain() {
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        TrainNode trainNode = new TrainNode();
        when(CCompiler.Compile(any(TrainNode.class))).thenReturn("true");
        assertEquals("true", CCompiler.Compile(trainNode));
    }
    @Test
    public void testCompileStmtWhile() {
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        WhileNode whileNode = new WhileNode();
        when(CCompiler.Compile(any(WhileNode.class))).thenReturn("true");
        assertEquals("true", CCompiler.Compile(whileNode));
    }
    @Test
    public void testCompileStmtExtern() {
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        ExternNode externNode = new ExternNode();
        when(CCompiler.Compile(any(ExternNode.class))).thenReturn("true");
        assertEquals("true", CCompiler.Compile(externNode));
    }
    @Test
    public void testCompileExprBiOp() {
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        BiOperatorNode biOpNode = new BiOperatorNode();
        when(CCompiler.Compile(any(BiOperatorNode.class))).thenReturn("true");
        assertEquals("true", CCompiler.Compile(biOpNode));
    }
    @Test
    public void testCompileExprUnOp() {
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        UnOperatorNode unOpNode = new UnOperatorNode();
        when(CCompiler.Compile(any(UnOperatorNode.class))).thenReturn("true");
        assertEquals("true", CCompiler.Compile(unOpNode));
    }
    @Test
    public void testCompileExprFloatNode() {
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        FloatNode floatNode = new FloatNode();
        when(CCompiler.Compile(any(FloatNode.class))).thenReturn("true");
        assertEquals("true", CCompiler.Compile(floatNode));
    }
    @Test
    public void testCompileExprIntNode() {
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        IntNode intNode = new IntNode();
        when(CCompiler.Compile(any(IntNode.class))).thenReturn("true");
        assertEquals("true", CCompiler.Compile(intNode));
    }
    @Test
    public void testCompileExprNNNode() {
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        NNNode nnNode = new NNNode();
        when(CCompiler.Compile(any(NNNode.class))).thenReturn("true");
        assertEquals("true", CCompiler.Compile(nnNode));
    }
    @Test
    public void testCompileExprEvalNode() {
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        EvalNode evalNode = new EvalNode();
        when(CCompiler.Compile(any(EvalNode.class))).thenReturn("true");
        assertEquals("true", CCompiler.Compile(evalNode));
    }
    @Test
    public void testCompileExprStringNode() {
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        StringNode stringNode = new StringNode();
        when(CCompiler.Compile(any(StringNode.class))).thenReturn("true");
        assertEquals("true", CCompiler.Compile(stringNode));
    }
    @Test
    public void testCompileExprCallNode() {
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        CallNode callNode = new CallNode();
        when(CCompiler.Compile(any(CallNode.class))).thenReturn("true");
        assertEquals("true", CCompiler.Compile(callNode));
    }
    @Test
    public void testCompileExprTupleNode() {
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        TupleNode tupleNode = new TupleNode();
        when(CCompiler.Compile(any(TupleNode.class))).thenReturn("true");
        assertEquals("true", CCompiler.Compile(tupleNode));
    }
    @Test
    public void testCompileExprArrAccessNode() {
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        ArrAccessNode arrAccessNodeNode = new ArrAccessNode();
        when(CCompiler.Compile(any(ArrAccessNode.class))).thenReturn("true");
        assertEquals("true", CCompiler.Compile(arrAccessNodeNode));
    }
    @Test
    public void testCompileExprFuncNode() {
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        FuncNode funcNode = new FuncNode();
        when(CCompiler.Compile(any(FuncNode.class))).thenReturn("true");
        assertEquals("true", CCompiler.Compile(funcNode));
    }
    @Test
    public void testCompileExprTestNode() {
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        TestNode testNode = new TestNode();
        when(CCompiler.Compile(any(TestNode.class))).thenReturn("true");
        assertEquals("true", CCompiler.Compile(testNode));
    }
    @Test
    public void testCompileWhileNode(){
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        when(CCompiler.Compile(any(Vector.class))).thenReturn("statements");
        when(CCompiler.Compile(any(WhileNode.class))).thenCallRealMethod();
        when(CCompiler.Compile(any(ExprNode.class))).thenReturn("predicate");
        WhileNode whileNode = new WhileNode();
        whileNode.Predicate = new ExprNode();
        assertEquals("while(predicate){statements}", CCompiler.Compile(whileNode));
    }
    @Test
    public void testCompilePowNode(){
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        BiOperatorNode biOpNode = new BiOperatorNode();
        when(CCompiler.Compile(any(ExprNode.class))).thenReturn("Expression");
        when(CCompiler.Compile(any(BiOperatorNode.class))).thenCallRealMethod();
        biOpNode.Operator = OpEnum.Power;
        biOpNode.Left = new ExprNode();
        biOpNode.Right = new ExprNode();
        assertEquals("(pow(Expression,Expression))", CCompiler.Compile(biOpNode));
    }
    @Test
    public void testCompileFloatNode(){
        FloatNode floatNode = new FloatNode();
        floatNode.Value = 2.5f;
        assertEquals("(2.5)", CCompiler.Compile(floatNode));
    }
    @Test
    public void testCompileIntNode(){
        IntNode intNode = new IntNode();
        intNode.Value = 5;
        assertEquals("(5)", CCompiler.Compile(intNode));
    }
    @Test
    public void testCompileStringNode(){
        StringNode stringNode = new StringNode();
        stringNode.Value = "value";
        assertEquals("(\"value\")", CCompiler.Compile(stringNode));
    }
    @Test
    public void testCompileNNNode(){
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        NNNode nnNode = new NNNode();
        nnNode.Activation = new FuncNode();
        nnNode.Derivative = new FuncNode();
        IntNode intNode = new IntNode();
        intNode.Value = 5;
        nnNode.LayerSizes = new Vector<ExprNode>();
        nnNode.LayerSizes.add(intNode);
        nnNode.LayerSizes.add(intNode);
        when(CCompiler.Compile(any(FuncNode.class))).thenReturn("Func");
        when(CCompiler.Compile(any(IntNode.class))).thenReturn("Int");
        when(CCompiler.Compile(any(NNNode.class))).thenCallRealMethod();
        when(CCompiler.Compile(any(ExprNode.class))).thenCallRealMethod();
        assertEquals("(model_new((2 -1),(layer_new(Int,Int,Func,Func))))", CCompiler.Compile(nnNode));    
    }
    @Test
    public void testCompileEvalNode(){
        EvalNode evalNode = new EvalNode();
        evalNode.Name = "variable";
        assertEquals("(variable)", CCompiler.Compile(evalNode));
    }
    @Test
    public void testCompileEvalNodeFunc(){
        EvalNode evalNode = new EvalNode();
        evalNode.Name = "variable";
        evalNode.Type = new FuncType();
        assertEquals("(*variable)", CCompiler.Compile(evalNode));
    }
    @Test
    public void testCompileTrainNode(){
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        TrainNode trainNode = new TrainNode();
        trainNode.Epochs = new ExprNode();
        trainNode.Expected = new ExprNode();
        trainNode.Input = new ExprNode();
        trainNode.NN = new EvalNode();
        trainNode.Rate = new ExprNode();
        when(CCompiler.Compile(any(ExprNode.class))).thenReturn("Expression");
        when(CCompiler.Compile(any(EvalNode.class))).thenReturn("NN");
        when(CCompiler.Compile(any(TrainNode.class))).thenCallRealMethod();
        assertEquals("(train_model(NN,Expression,Expression,Expression,Expression))", CCompiler.Compile(trainNode));
    }
    @Test
    public void testCompileTestNode(){
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        when(CCompiler.Compile(any(ExprNode.class))).thenReturn("Expression");
        when(CCompiler.Compile(any(TestNode.class))).thenCallRealMethod();
        TestNode testNode = new TestNode();
        testNode.In = new ExprNode();
        testNode.NN = new ExprNode();
        testNode.Out = new ExprNode();
        assertEquals("(test_model(Expression,Expression,Expression))", CCompiler.Compile(testNode));
    }
    @Test
    public void testCompileExternNodeNotFunc(){
        ExternNode externNode = new ExternNode();
        externNode.Name = "ExternNode";
        externNode.Type = new BaseType(TypeEnum.Int);
        assertEquals("int ExternNode = E_ExternNode", CCompiler.Compile(externNode));
    }
    @Test
    public void testCompileExternNodeFunc(){
        ExternNode externNode = new ExternNode();
        externNode.Name = "ExternNode";
        FuncType func = new FuncType();
        func.Args.add(new BaseType(TypeEnum.Int));
        func.Ret = new TupleType();
        func.Ret.Types.add(new BaseType(TypeEnum.Int));
        externNode.Type = func;
        assertEquals("int  (*ExternNode)(int ) = &E_ExternNode", CCompiler.Compile(externNode));
    }
}
