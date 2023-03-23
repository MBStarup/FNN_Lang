import java.util.*;
import java.lang.reflect.Field;

public class ProgramNode extends AstNode {
    public ProgramNode() {
        this.Type = AstNodeType.Program;
    }

    public List<ExprNode> Exprs;
}
