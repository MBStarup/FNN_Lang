
public class Visitor extends FNNBaseVisitor<Object> {
    @Override
    public Object visitProgram(FNNParser.ProgramContext ctx) {
        System.out.println("visiting Programme");
        for (var child : ctx.children) {
            System.out.printf("Asking child %s to accept visitor\n", child.toString());
            child.accept(this);
        }
        return this.visitChildren(ctx);
    }

    @Override
    public Object visitType(FNNParser.TypeContext ctx) {
        System.out.println("Visiting tüüp");
        return this.visitChildren(ctx);
    }

    @Override
    public Object visitExpr(FNNParser.ExprContext ctx) {
        System.out.print("Visiting expr: ");
        System.out.printf("This expr is a: %s\n", (ctx.getChild(0)).getPayload());
        return this.visitChildren(ctx);
    }
}