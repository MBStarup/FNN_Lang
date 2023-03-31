import java.util.List;
import java.util.Vector;

public class AssignNode extends StmtNode {
    public List<TypeEnum> Types = new Vector<>();
    public List<String> Names = new Vector<>();
    public ExprNode Value;
}
