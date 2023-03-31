import java.util.*;
import java.lang.reflect.Field;

public class ProgramNode extends AstNode {
    public ProgramNode() {
        Stmts = new Vector<StmtNode>();
    }

    public List<StmtNode> Stmts;
}
