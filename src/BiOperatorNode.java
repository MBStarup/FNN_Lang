public class BiOperatorNode extends ExprNode {
    public BiOperatorNode() {
        this.Type = AstNodeType.BiOperator;
    }

    public ExprNode Left;
    public ExprNode Right;
    public OpEnum Operator;

}