import java.util.*;
import org.antlr.v4.runtime.ParserRuleContext;

public class Visitor extends FNNBaseVisitor<AstNode> {

    static void ERREXIT(String msg) {
        System.err.println(msg);
        System.exit(-1);
    }

    static void ASSERT(Boolean predicate, String fail_msg) {
        if (!(predicate))
            ERREXIT(fail_msg);
    }

    static String TYPE_LIST_TO_STRING(List<TypeEnum> Types) {
        String result = "{ ";
        for (TypeEnum type : Types) {
            result += type;
            result += " ";
        }
        result += "}";
        return result;
    }

    public Stack<Map<String, TypeEnum>> Scopes;

    public Visitor() {
        Scopes = new Stack<>();
        Scopes.push(new HashMap<>());
    }

    @Override
    public AstNode visitProgram(FNNParser.ProgramContext ctx) {
        ProgramNode result = new ProgramNode();
        for (int i = 0; i < ctx.getChildCount() - 1; i++) { // -1 to skip th EOF
            System.out.println("going to visit: " + ctx.getChild(i).getText());
            result.Stmts.add((StmtNode) this.visit(ctx.getChild(i)));
        }
        return result;
    }

    @Override
    public AstNode visitBiop(FNNParser.BiopContext ctx) {

        BiOperatorNode result = new BiOperatorNode();

        var l = this.visit(ctx.left_op);
        ASSERT(l instanceof ExprNode, "Left operand of bi-operator was not an expression: " + ctx.left_op.getStart() + " to " + ctx.left_op.getStop());
        result.Left = (ExprNode) l;

        var r = this.visit(ctx.right_op);
        ASSERT(r instanceof ExprNode, "Right operand of bi-operator was not an expression: " + ctx.right_op.getStart() + " to " + ctx.right_op.getStop());
        result.Right = (ExprNode) r;

        ASSERT(result.Right.Types.size() == 1, "Binary operations can only be used on single value expressions");
        ASSERT(result.Left.Types.size() == 1, "Binary operations can only be used on single value expressions");
        // TODO: Consider other types
        result.Types.add(result.Right.Types.get(0) == TypeEnum.Int && result.Left.Types.get(0) == TypeEnum.Int ? TypeEnum.Int : TypeEnum.Float);

        result.Operator = OpEnum.parseChar(ctx.OPERATOR().getText().charAt(0));

        return result;
    }

    @Override
    public AstNode visitUnop(FNNParser.UnopContext ctx) {

        UnOperatorNode result = new UnOperatorNode();

        var op = this.visit(ctx.op);
        ASSERT(op instanceof ExprNode, "Left operand of bi-operator was not an expression: " + ctx.op.getStart() + " to " + ctx.op.getStop());
        result.Operand = (ExprNode) op;
        if (result.Operand.Types.size() != 1) {

        }
        ASSERT(result.Operand.Types.size() == 1, "Unary operations can only be used on single value expressions");
        result.Types.add(result.Operand.Types.get(0));

        result.Operator = OpEnum.parseChar(ctx.OPERATOR().getText().charAt(0));

        return result;
    }

    @Override
    public ExprNode visitParens(FNNParser.ParensContext ctx) {
        var result = this.visit(ctx.expr_in_parens);
        ASSERT(result instanceof ExprNode, "Contents of parens was not an expression: " + ctx.expr_in_parens.getStart() + " to " + ctx.expr_in_parens.getStop());

        return (ExprNode) result; // this being hard-doed kinda sucks
    }

    @Override
    public AssignNode visitAssign(FNNParser.AssignContext ctx) {
        var result = new AssignNode();
        var value = this.visit(ctx.expr_in_assign);
        ASSERT(value instanceof ExprNode, "Right side of assignment was not an expression: " + ctx.expr_in_assign.getStart() + " to " + ctx.expr_in_assign.getStop());
        result.Value = (ExprNode) value;
        ASSERT(result.Value.Types.size() == ctx.ID().size(), "Match failure in assign, mismatched amount");
        for (int i = 0; i < ctx.ID().size(); i++) {
            var name = ctx.ID(i).getText();
            result.Types.add(result.Value.Types.get(i));
            result.Names.add(name);
            Scopes.peek().put(name, result.Value.Types.get(i));
            System.out.println("ASSIGN: " + name + " : " + result.Value.Types.get(i));
        }
        return result;
    }

    @Override
    public EvalNode visitEval(FNNParser.EvalContext ctx) {
        var name = ctx.ID().getText();
        for (var scope : Scopes) {
            var type = scope.get(name);
            if (type != null) {
                System.out.println("EVAL: " + name + " : " + type);
                EvalNode result = new EvalNode();
                result.Types.add(type);
                result.Name = name;
                return result;
            }
        }
        ERREXIT("Could not evaluate variable " + name + " : " + ctx.getStart() + " to " + ctx.getStop());
        return null;
    }

    @Override
    public AstNode visitFunction_declaration(FNNParser.Function_declarationContext ctx) {
        ERREXIT("FUNCTIONS NOT IMPLEMENTED");
        return null;
    }

    @Override
    public IntNode visitIntlit(FNNParser.IntlitContext ctx) {
        IntNode result = new IntNode();
        result.Types.add(TypeEnum.Int);
        result.Value = Integer.parseInt(ctx.getText());
        return result;
    }

    @Override
    public FloatNode visitFloatlit(FNNParser.FloatlitContext ctx) {
        FloatNode result = new FloatNode();
        result.Types.add(TypeEnum.Float);
        result.Value = Float.parseFloat(ctx.getText());
        return result;
    }

    @Override
    public LayerNode visitLayerlit(FNNParser.LayerlitContext ctx) {
        LayerNode result = new LayerNode();
        result.Types.add(TypeEnum.Layer);
        var inputsize = this.visit(ctx.input_size);
        ASSERT(inputsize instanceof ExprNode, "Input size of layer must be an expression: " + ctx.input_size.getStart() + " to " + ctx.input_size.getStop());
        result.InputSize = (ExprNode) inputsize;
        ASSERT(result.InputSize.Types.size() == 1, "Input size of layer must be a single value expression: " + ctx.input_size.getStart() + " to " + ctx.input_size.getStop());
        ASSERT(result.InputSize.Types.get(0) == TypeEnum.Int, "Input size of layer must be an integer: " + ctx.input_size.getStart() + " to " + ctx.input_size.getStop());

        var outputsize = this.visit(ctx.output_size);
        ASSERT(outputsize instanceof ExprNode, "Output size of layer must be an expression: " + ctx.output_size.getStart() + " to " + ctx.output_size.getStop());
        result.OutputSize = (ExprNode) outputsize;
        ASSERT(result.OutputSize.Types.size() == 1, "Output size of layer must be a single value expression: " + ctx.output_size.getStart() + " to " + ctx.output_size.getStop());
        ASSERT(result.OutputSize.Types.get(0) == TypeEnum.Int, "Output size of layer must be an integer: " + ctx.output_size.getStart() + " to " + ctx.output_size.getStop());

        result.ActivationFunction = ctx.activation_function.getText();
        return result;
    }

    @Override
    public ModelNode visitModellit(FNNParser.ModellitContext ctx) {
        var result = new ModelNode();
        result.Types.add(TypeEnum.Model);
        result.Layers = new Vector<>();
        for (var expr : ctx.children.subList(2, ctx.getChildCount() - 1)) { // TODO: gotta be a better way to get all the expressions without the "model<>" part
            var layer = this.visit(expr);
            ASSERT(layer instanceof ExprNode, "Model parameters must be expressions: " + ((ParserRuleContext) expr.getPayload()).getStart() + " to " + ((ParserRuleContext) expr.getPayload()).getStop());
            var exprNode = (ExprNode) layer;
            for (TypeEnum type : exprNode.Types) {
                ASSERT(type == TypeEnum.Layer, "Model parameters must be layers, not " + TYPE_LIST_TO_STRING(exprNode.Types) + ": " + ((ParserRuleContext) expr.getPayload()).getStart() + " to " + ((ParserRuleContext) expr.getPayload()).getStop());
                result.Layers.add(exprNode);
            }
        }
        return result;
    }

    @Override
    public StringNode visitStrlit(FNNParser.StrlitContext ctx) {
        var result = new StringNode();
        result.Types.add(TypeEnum.String);
        result.Value = ctx.STR_CONTENT().getText();
        if (result.Value == null)
            result.Value = "";
        return result;
    }

    @Override
    public TrainNode visitTrain_stmt(FNNParser.Train_stmtContext ctx) {
        var result = new TrainNode();
        var epochs = this.visit(ctx.epochs);
        ASSERT(epochs instanceof ExprNode, "Epochs in training must be an expression: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());
        result.Epochs = (ExprNode) epochs;
        ASSERT(result.Epochs.Types.size() == 1, "Epoch in training must be a single value expression");
        ASSERT(result.Epochs.Types.get(0) != TypeEnum.Int, "Epochs in training must be an integer: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());

        var batchSize = this.visit(ctx.batch_size);
        ASSERT(batchSize instanceof ExprNode, "Batch size in training must be an expression: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());
        ASSERT(result.BatchSize.Types.size() == 1, "Batch size in training must be a single value expression");
        result.BatchSize = (ExprNode) batchSize;
        ASSERT(result.BatchSize.Types.get(0) != TypeEnum.Int, "Batch size in training must be an integer: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());

        var model = this.visit(ctx.model);
        ASSERT(model instanceof ExprNode, "Model in training must be an expression: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());
        ASSERT(result.Model.Types.size() == 1, "Model in training must be a single value expression");
        result.Model = (ExprNode) model;
        ASSERT(result.Model.Types.get(0) != TypeEnum.Model, "Model in training must be an model: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());

        return result;
    }
}