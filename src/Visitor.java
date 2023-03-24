import java.lang.ProcessBuilder.Redirect.Type;
import java.lang.ref.Cleaner;

import org.antlr.v4.runtime.atn.SemanticContext.Operator;
import org.antlr.v4.runtime.tree.TerminalNode;

public class Visitor extends FNNBaseVisitor<AstNode> {
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
            System.err.println("Left operand of bi-operator was not an expression: " +
                    ctx.left_op.getStart() + " to "
                    + ctx.left_op.getStop());
            System.exit(-1);
        }
        result.Left = (ExprNode) l;

        var r = this.visit(ctx.right_op);
        if (!(r instanceof ExprNode)) {
            System.err.println("Right operand of bi-operator was not an expression: " +
                    ctx.right_op.getStart() + " to "
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
            System.err.println("Left operand of bi-operator was not an expression: " +
                    ctx.op.getStart() + " to "
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
            System.err.println("Contents of parens was not an expression: " +
                    ctx.expr_in_parens.getStart() + " to "
                    + ctx.expr_in_parens.getStop());
            System.exit(-1);
        }

        return (ExprNode) result; // this being hard-doed kinda sucks
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
}