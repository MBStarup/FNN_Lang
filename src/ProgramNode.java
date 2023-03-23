import java.util.*;
import java.lang.reflect.Field;

public class ProgramNode extends AstNode {
    public ProgramNode() {
        Exprs = new Vector<ExprNode>();
    }

    public List<ExprNode> Exprs;
}
