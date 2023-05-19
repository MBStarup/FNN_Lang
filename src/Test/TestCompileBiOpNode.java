import org.junit.*;
import org.junit.runners.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.runner.RunWith;
import org.mockito.*;

import java.util.*;

@RunWith(Parameterized.class)
public class TestCompileBiOpNode{
    OpEnum operator;
    String compilerOutput;
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {{OpEnum.Plus, "+"}, {OpEnum.Minus, "-"},
        {OpEnum.Multiply, "*"},{OpEnum.Divide, "/"},{OpEnum.LessThan, "<"},
        {OpEnum.GreaterThan, ">"},{OpEnum.Equals, "=="}});
    }
    public TestCompileBiOpNode(OpEnum op, String output){
        this.operator = op;
        this.compilerOutput = output;
    }  
    @Test
    public void testCompileBiOpNode(){
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        BiOperatorNode biOpNode = new BiOperatorNode();
        when(CCompiler.Compile(any(ExprNode.class))).thenReturn("Expression");
        when(CCompiler.Compile(any(BiOperatorNode.class))).thenCallRealMethod();
        biOpNode.Operator = operator;
        biOpNode.Left = new ExprNode();
        biOpNode.Right = new ExprNode();
        assertEquals("(Expression "+compilerOutput+" Expression)", CCompiler.Compile(biOpNode));
    }    
}