import java.util.*;

public class WhileNode extends StmtNode {
    public ExprNode Predicate;
    public List<StmtNode> Stmts = new Vector<StmtNode>();
}
