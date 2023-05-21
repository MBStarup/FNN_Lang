import java.util.List;

public class NNNode extends ExprNode {
    public ExprNode Activation;
    public ExprNode Derivative;
    public List<ExprNode> LayerSizes;
}
