import org.junit.*;
import org.junit.runners.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.runner.RunWith;
import org.mockito.*;

import java.util.*;

@RunWith(Parameterized.class)
public class TestCompileUnOpNode{
    OpEnum operator;
    String compilerOutput;
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {{OpEnum.Plus, " + "}, {OpEnum.Minus, " - "},});
    }
    public TestCompileUnOpNode(OpEnum op, String output){
        this.operator = op;
        this.compilerOutput = output;
    }  
    @Test
    public void testCompileUnOpNode(){
        ToCCompiler CCompiler = Mockito.mock(ToCCompiler.class);
        UnOperatorNode unOpNode = new UnOperatorNode();
        when(CCompiler.Compile(any(ExprNode.class))).thenReturn("Expression");
        when(CCompiler.Compile(any(UnOperatorNode.class))).thenCallRealMethod();
        unOpNode.Operator = operator;
        unOpNode.Operand = new ExprNode();
        assertEquals("("+compilerOutput+"Expression)", CCompiler.Compile(unOpNode));
    }    
}