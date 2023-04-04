import java.util.List;
import java.util.Vector;

public class AssignNode extends StmtNode {
    public List<FNNType> Types = new Vector<>();
    public List<String> Names = new Vector<>();
    public ExprNode Value;
}
