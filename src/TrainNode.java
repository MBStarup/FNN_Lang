import org.stringtemplate.v4.compiler.STParser.exprNoComma_return;

public class TrainNode extends StmtNode {
    public ExprNode Epochs;
    public ExprNode BatchSize;
    public ExprNode Model;
    public ExprNode Expected;
    public ExprNode Input;
}
