import java.util.*;

public class Visitor extends FNNBaseVisitor<AstNode> {

    public Stack<Map<String, ExprNode>> Scopes;

    public Visitor() {
        Scopes = new Stack<>();
        Scopes.push(new HashMap<>());
    }

    @Override
    public AstNode visitProgram(FNNParser.ProgramContext ctx) {
        ProgramNode result = new ProgramNode();
        for (int i = 0; i < ctx.getChildCount() - 1; i++) { // -1 to skip th EOF
            System.out.println("going to visit: " + ctx.getChild(i).getText());
            result.Exprs.add((ExprNode) this.visit(ctx.getChild(i)));
        }
        return result;
    }

    @Override
    public AstNode visitBiop(FNNParser.BiopContext ctx) {

        BiOperatorNode result = new BiOperatorNode();

        var l = this.visit(ctx.left_op);
        if (!(l instanceof ExprNode)) {
            System.err.println("Left operand of bi-operator was not an expression: " + ctx.left_op.getStart() + " to "
                    + ctx.left_op.getStop());
            System.exit(-1);
        }
        result.Left = (ExprNode) l;

        var r = this.visit(ctx.right_op);
        if (!(r instanceof ExprNode)) {
            System.err.println("Right operand of bi-operator was not an expression: " + ctx.right_op.getStart() + " to "
                    + ctx.right_op.getStop());
            System.exit(-1);
        }
        result.Right = (ExprNode) r;

        result.Type = result.Right.Type == TypeEnum.Int && result.Left.Type == TypeEnum.Int ? TypeEnum.Int
                : TypeEnum.Float;

        result.Operator = OpEnum.parseChar(ctx.OPERATOR().getText().charAt(0));

        return result;
    }

    @Override
    public AstNode visitUnop(FNNParser.UnopContext ctx) {

        UnOperatorNode result = new UnOperatorNode();

        var op = this.visit(ctx.op);
        if (!(op instanceof ExprNode)) {
            System.err.println("Left operand of bi-operator was not an expression: " + ctx.op.getStart() + " to "
                    + ctx.op.getStop());
            System.exit(-1);
        }
        result.Operand = (ExprNode) op;
        result.Type = result.Operand.Type;

        result.Operator = OpEnum.parseChar(ctx.OPERATOR().getText().charAt(0));

        return result;
    }

    @Override
    public ExprNode visitParens(FNNParser.ParensContext ctx) {
        var result = this.visit(ctx.expr_in_parens);
        if (!(result instanceof ExprNode)) {
            System.err.println("Contents of parens was not an expression: " + ctx.expr_in_parens.getStart() + " to "
                    + ctx.expr_in_parens.getStop());
            System.exit(-1);
        }

        return (ExprNode) result; // this being hard-doed kinda sucks
    }

    @Override
    public ExprNode visitAssign(FNNParser.AssignContext ctx) {
        var result = this.visit(ctx.expr_in_assign);
        if (!(result instanceof ExprNode)) {
            System.err.println("Right side of assignment was not an expression: " + ctx.expr_in_assign.getStart()
                    + " to " + ctx.expr_in_assign.getStop());
            System.exit(-1);
        }

        Scopes.peek().put(ctx.ID().getText(), (ExprNode) result);

        return (ExprNode) result;
    }

    @Override
    public ExprNode visitEval(FNNParser.EvalContext ctx) {
        var name = ctx.ID().getText();
        for (Map<String, ExprNode> scope : Scopes) {
            var result = scope.get(name);
            if (result != null)
                return result;
        }
        System.err.println("Could not evaluate variable " + name + " : " + ctx.getStart() + " to " + ctx.getStop());
        System.exit(-1);
        return null;
    }

    @Override
    public AstNode visitFunction_declaration(FNNParser.Function_declarationContext ctx) {
        System.err.println("We need stuff here --> ");
        System.exit(-1);
        return visitChildren(ctx);
    }

    @Override
    public IntNode visitIntlit(FNNParser.IntlitContext ctx) {
        IntNode result = new IntNode();
        result.Type = TypeEnum.Int;
        result.Value = Integer.parseInt(ctx.getText());
        return result;
    }

    @Override
    public FloatNode visitFloatlit(FNNParser.FloatlitContext ctx) {
        FloatNode result = new FloatNode();
        result.Type = TypeEnum.Float;
        result.Value = Float.parseFloat(ctx.getText());
        return result;
    }

    @Override
    public LayerNode visitLayerlit(FNNParser.LayerlitContext ctx) {
        LayerNode result = new LayerNode();
        result.Type = TypeEnum.Layer;
        var inputsize = this.visit(ctx.input_size);
        if(!(inputsize instanceof IntNode)){
            System.err.println("Input size of layer must be integer: " + ctx.input_size.getStart() + " to "
                    + ctx.input_size.getStop());
            System.exit(-1);
        }
        result.Inputsize = (IntNode)inputsize;

        var outputsize = this.visit(ctx.output_size);
        if(!(inputsize instanceof IntNode)){
            System.err.println("Output size of layer must be integer: " + ctx.output_size.getStart() + " to "
                    + ctx.output_size.getStop());
            System.exit(-1);
        }
        result.Outputsize = (IntNode)outputsize;

        result.ActivationFunction = ctx.activation_function.getText();
        return result;
    }
}