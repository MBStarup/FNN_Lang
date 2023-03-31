import java.util.*;

import javax.swing.text.html.HTMLDocument.RunElement;

import org.antlr.v4.runtime.ParserRuleContext;

public class Visitor extends FNNBaseVisitor<AstNode> {

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
        if (!(l instanceof ExprNode)) {
            System.err.println("Left operand of bi-operator was not an expression: " + ctx.left_op.getStart() + " to " + ctx.left_op.getStop());
            System.exit(-1);
        }
        result.Left = (ExprNode) l;

        var r = this.visit(ctx.right_op);
        if (!(r instanceof ExprNode)) {
            System.err.println("Right operand of bi-operator was not an expression: " + ctx.right_op.getStart() + " to " + ctx.right_op.getStop());
            System.exit(-1);
        }
        result.Right = (ExprNode) r;

        // TODO: Consider other types
        result.Type = result.Right.Type == TypeEnum.Int && result.Left.Type == TypeEnum.Int ? TypeEnum.Int : TypeEnum.Float;

        result.Operator = OpEnum.parseChar(ctx.OPERATOR().getText().charAt(0));

        return result;
    }

    @Override
    public AstNode visitUnop(FNNParser.UnopContext ctx) {

        UnOperatorNode result = new UnOperatorNode();

        var op = this.visit(ctx.op);
        if (!(op instanceof ExprNode)) {
            System.err.println("Left operand of bi-operator was not an expression: " + ctx.op.getStart() + " to " + ctx.op.getStop());
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
            System.err.println("Contents of parens was not an expression: " + ctx.expr_in_parens.getStart() + " to " + ctx.expr_in_parens.getStop());
            System.exit(-1);
        }

        return (ExprNode) result; // this being hard-doed kinda sucks
    }

    @Override
    public AssignNode visitAssign(FNNParser.AssignContext ctx) {
        var result = new AssignNode();
        var name = ctx.ID().getText();
        var value = this.visit(ctx.expr_in_assign);
        if (!(value instanceof ExprNode)) {
            System.err.println("Right side of assignment was not an expression: " + ctx.expr_in_assign.getStart() + " to " + ctx.expr_in_assign.getStop());
            System.exit(-1);
        }
        result.Value = (ExprNode) value;
        result.Type = result.Value.Type;
        result.Name = name;

        Scopes.peek().put(name, result.Value.Type);

        System.out.println("ASSIGN: " + name + " : " + result.Value.Type);

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
                result.Type = type;
                result.Name = name;
                return result;
            }
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
        if (!(inputsize instanceof ExprNode)) {
            System.err.println("Input size of layer must be an expression: " + ctx.input_size.getStart() + " to " + ctx.input_size.getStop());
            System.exit(-1);
        }
        result.InputSize = (ExprNode) inputsize;
        if (result.InputSize.Type != TypeEnum.Int) {
            System.err.println("Input size of layer must be an integer: " + ctx.input_size.getStart() + " to " + ctx.input_size.getStop());
            System.exit(-1);
        }

        var outputsize = this.visit(ctx.output_size);
        if (!(inputsize instanceof ExprNode)) {
            System.err.println("Output size of layer must be an expression: " + ctx.output_size.getStart() + " to " + ctx.output_size.getStop());
            System.exit(-1);
        }
        result.OutputSize = (ExprNode) outputsize;
        if (result.OutputSize.Type != TypeEnum.Int) {
            System.err.println("Input size of layer must be an integer: " + ctx.input_size.getStart() + " to " + ctx.input_size.getStop());
            System.exit(-1);
        }

        result.ActivationFunction = ctx.activation_function.getText();
        return result;
    }

    @Override
    public ModelNode visitModellit(FNNParser.ModellitContext ctx) {
        var result = new ModelNode();
        result.Type = TypeEnum.Model;
        result.Layers = new Vector<>();
        for (var expr : ctx.children.subList(2, ctx.getChildCount() - 1)) { // TODO: gotta be a better way to get all the expressions without the "model<>" part
            var layer = this.visit(expr);
            if (!(layer instanceof ExprNode)) {
                // TODO: This does some absolute meme casts
                System.err.println("Model parameters must be expressions: " + ((ParserRuleContext) expr.getPayload()).getStart() + " to " + ((ParserRuleContext) expr.getPayload()).getStop());
                System.exit(-1);
            }
            if (((ExprNode) layer).Type != TypeEnum.Layer) {
                // TODO: This does some absolute meme casts
                System.err.println("Model parameters must be layers, not " + ((ExprNode) layer).Type + ": " + ((ParserRuleContext) expr.getPayload()).getStart() + " to " + ((ParserRuleContext) expr.getPayload()).getStop());
                System.exit(-1);
            }
            result.Layers.add((ExprNode) layer);
        }
        return result;
    }

    @Override
    public StringNode visitStrlit(FNNParser.StrlitContext ctx) {
        var result = new StringNode();
        result.Type = TypeEnum.String;
        result.Value = ctx.STR_CONTENT().getText();
        if (result.Value == null)
            result.Value = "";
        return result;
    }

    @Override
    public TrainNode visitTrain_stmt(FNNParser.Train_stmtContext ctx) {
        var result = new TrainNode();
        var epochs = this.visit(ctx.epochs);
        if (!(epochs instanceof ExprNode)) {
            System.err.println("Epochs in training must be an expression: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());
            System.exit(-1);
        }
        result.Epochs = (ExprNode) epochs;
        if (result.Epochs.Type != TypeEnum.Int) {
            System.err.println("Epochs in training must be an integer: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());
            System.exit(-1);
        }

        var batchSize = this.visit(ctx.batch_size);
        if (!(batchSize instanceof ExprNode)) {
            System.err.println("Batch size in training must be an expression: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());
            System.exit(-1);
        }
        result.BatchSize = (ExprNode) batchSize;
        if (result.BatchSize.Type != TypeEnum.Int) {
            System.err.println("Batch size in training must be an integer: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());
            System.exit(-1);
        }

        var model = this.visit(ctx.model);
        if (!(model instanceof ExprNode)) {
            System.err.println("Model in training must be an expression: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());
            System.exit(-1);
        }
        result.Model = (ExprNode) model;
        if (result.Model.Type != TypeEnum.Model) {
            System.err.println("Model in training must be an model: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());
            System.exit(-1);
        }

        return result;
    }
}