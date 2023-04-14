import java.util.*;

public class FuncNode extends ExprNode {
    public List<StmtNode> Stmts = new Vector<>();
    public List<String> ParamNames = new Vector<>();
    public ExprNode Result;
}
