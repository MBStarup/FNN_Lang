import java.lang.ProcessBuilder.Redirect.Type;
import java.lang.ref.Cleaner;

import org.antlr.v4.runtime.atn.SemanticContext.Operator;
import org.antlr.v4.runtime.tree.TerminalNode;

public class Visitor extends FNNBaseVisitor<AstNode> {
    @Override
    public AstNode visitProgram(FNNParser.ProgramContext ctx) {
        ProgramNode result = new ProgramNode();
        for (int i = 0; i < ctx.getChildCount() - 1; i++) { // -1 to skip th EOF
            result.Exprs.add((ExprNode) this.visit(ctx.getChild(i)));
        }
        return result;
    }

    @Override
    public AstNode visitBiop(FNNParser.BiopContext ctx) {

        BiOperatorNode result = new BiOperatorNode();
        result.Left = this.visit(ctx.left_op);
        result.Right = this.visit(ctx.right_op);
        result.Operator = ctx.OPERATOR().getText();
    }

    public AstNode visitExpr(FNNParser.ExprContext ctx) {
        // AstNode result;
        // if (ctx.left != null && ctx.right != null) {
        // AstNode left = visit(ctx.left);
        // AstNode right = visit(ctx.right);

        // String operator = ctx.OPERATOR().getText();
        // switch (operator.charAt(0)) {
        // case '+':
        // case '-':
        // case '*':
        // case '/':
        // if (left == AstNode.Float || right == AstNode.Float) {
        // result = TypeEnum.Float;
        // } else {
        // result = TypeEnum.Int;
        // }
        // break;
        // default:
        // throw new IllegalArgumentException("Wrong Operator" + operator);

        // }
        // }
        // result = this.visitChildren(ctx);
        // System.out.println("Returning expr: " + result);
        // return result;
        return null;
    }
}