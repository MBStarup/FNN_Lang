import java.lang.ProcessBuilder.Redirect.Type;
import java.lang.ref.Cleaner;

import org.antlr.v4.runtime.atn.SemanticContext.Operator;

public class Visitor extends FNNBaseVisitor<TypeEnum> {
    @Override
    public TypeEnum visitProgram(FNNParser.ProgramContext ctx) {
        System.out.println("visiting Programme");
        System.out.print("programtext: ");
        System.out.println(ctx.getText());
        // for (var child : ctx.children) {
        // System.out.printf("Asking child %s to accept visitor\n", child.toString());
        // child.accept(this);
        // }
        return this.visitChildren(ctx);
    }

    @Override
    public TypeEnum visitType(FNNParser.TypeContext ctx) {
        System.out.println("Visiting tüüp");
        return this.visitChildren(ctx);
    }

    @Override
    public TypeEnum visitExpr(FNNParser.ExprContext ctx) {
        System.out.printf("arhh: %d \n", ctx.getChildCount());
        System.out.println(ctx.getText());
        // System.out.print("Visiting expr: ");
        // System.out.printf("This expr is a: %s\n", (ctx.getChild(0)).getPayload());
        if (ctx.left != null && ctx.right != null) {
            TypeEnum left = visit(ctx.left);
            TypeEnum right = visit(ctx.right);

            String operator = ctx.OPERATOR().getText();
            switch (operator.charAt(0)) {
                case '+', '-', '*', '/':
                    if (left == TypeEnum.Float || right == TypeEnum.Float) {
                        return TypeEnum.Float;
                    } else {
                        return TypeEnum.Int;
                    }
                default:
                    throw new IllegalArgumentException("Wrong Operator" + operator);

            }
        }

        return this.visitChildren(ctx);
    }

    @Override
    public TypeEnum visitIntlit(FNNParser.IntlitContext ctx) {
        return TypeEnum.Int;
    }

    @Override
    public TypeEnum visitFloatlit(FNNParser.FloatlitContext ctx) {
        return TypeEnum.Float;
    }

}