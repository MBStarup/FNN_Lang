import java.lang.ProcessBuilder.Redirect.Type;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.*;

import org.antlr.runtime.tree.ParseTree;
import org.antlr.v4.runtime.ParserRuleContext;
import org.stringtemplate.v4.compiler.STParser.ifstat_return;

public class Visitor extends FNNBaseVisitor<AstNode> {

    public Stack<Map<String, FNNType>> Scopes;

    public Visitor() {
        Scopes = new Stack<>();
        Scopes.push(new HashMap<>());
    }

    @Override
    public ProgramNode visitProgram(FNNParser.ProgramContext ctx) {
        ProgramNode result = new ProgramNode();
        var stmtList = this.visit(ctx.stmts);
        Utils.ASSERT(stmtList instanceof StmtListNode, "I legit don't know how we'd ever get this error lmao");
        result.Stmts = ((StmtListNode) stmtList).Stmts; // This is fucking retarded
        return result;
    }

    @Override
    public StmtListNode visitStmtlist(FNNParser.StmtlistContext ctx) {
        var result = new StmtListNode();
        for (var antlr_stmt_node : ctx.children) {
            System.out.println("going to visit: " + antlr_stmt_node.getText());
            var ast_stmt_node = this.visit(antlr_stmt_node);
            Utils.ASSERT(ast_stmt_node instanceof StmtNode, "ASDASDASDASDDSDSDADASDSD");
            result.Stmts.add((StmtNode) ast_stmt_node);
        }
        return result;
    }

    @Override
    public ExprListNode visitExprlist(FNNParser.ExprlistContext ctx) {
        var result = new ExprListNode();
        for (var antlr_expr_node : ctx.children) {
            var ast_expr_node = this.visit(antlr_expr_node);
            Utils.ASSERT(ast_expr_node instanceof ExprNode, "Somehow we have a thing that was parsed to an exprlist, but when visiting the children it doesn't result in an ExprNode");
            result.Exprs.add((ExprNode) ast_expr_node);
        }
        return result;
    }

    @Override
    public TypeListNode visitTypelist(FNNParser.TypelistContext ctx) {
        var result = new TypeListNode();
        for (var antlr_type_node : ctx.children) {
            var ast_type_node = this.visit(antlr_type_node);
            Utils.ASSERT(ast_type_node instanceof TypeNode, "Somehow we have a thing that was parsed to an typelist, but when visiting the children it doesn't result in an TypeNode");
            result.Types.add((TypeNode) ast_type_node);
        }
        return result;
    }

    @Override
    public AstNode visitBiop(FNNParser.BiopContext ctx) {

        BiOperatorNode result = new BiOperatorNode();

        var l = this.visit(ctx.left_op);
        Utils.ASSERT(l instanceof ExprNode, "Left operand of bi-operator was not an expression: " + ctx.left_op.getStart() + " to " + ctx.left_op.getStop());
        result.Left = (ExprNode) l;

        var r = this.visit(ctx.right_op);
        Utils.ASSERT(r instanceof ExprNode, "Right operand of bi-operator was not an expression: " + ctx.right_op.getStart() + " to " + ctx.right_op.getStop());
        result.Right = (ExprNode) r;

        Utils.ASSERT(result.Right.Type instanceof BaseType, "Binary operations can only be used on single value expressions");
        Utils.ASSERT(result.Left.Type instanceof BaseType, "Binary operations can only be used on single value expressions");
        // TODO: Consider other types
        // result.Types.add(result.Right.Types.get(0) == FNNType.Int &&
        // result.Left.Types.get(0) == FNNType.Int ? FNNType.Int : FNNType.Float);
        Utils.ASSERT(result.Left.Type == result.Right.Type, "Bi operation: " + ctx.OPERATOR().getText() + ", between mismatched types: " + result.Left.Type + " and " + result.Right.Type);

        result.Operator = OpEnum.parseChar(ctx.OPERATOR().getText().charAt(0));

        return result;
    }

    @Override
    public AstNode visitUnop(FNNParser.UnopContext ctx) {

        UnOperatorNode result = new UnOperatorNode();

        var op = this.visit(ctx.op);
        Utils.ASSERT(op instanceof ExprNode, "Left operand of bi-operator was not an expression: " + ctx.op.getStart() + " to " + ctx.op.getStop());
        result.Operand = (ExprNode) op;
        Utils.ASSERT(result.Operand.Type instanceof BaseType, "Unary operations can only be used on single value expressions");
        result.Type = result.Operand.Type;

        result.Operator = OpEnum.parseChar(ctx.OPERATOR().getText().charAt(0));

        return result;
    }

    @Override
    public ExprNode visitParens(FNNParser.ParensContext ctx) {
        var result = this.visit(ctx.expr_in_parens);
        Utils.ASSERT(result instanceof ExprNode, "Contents of parens was not an expression: " + ctx.expr_in_parens.getStart() + " to " + ctx.expr_in_parens.getStop());

        return (ExprNode) result; // this being hard-doed kinda sucks
    }

    @Override
    public AssignNode visitAssign(FNNParser.AssignContext ctx) {
        var result = new AssignNode();
        var value = this.visit(ctx.expr_in_assign);
        Utils.ASSERT(value instanceof ExprNode, "Right side of assignment was not an expression: " + ctx.expr_in_assign.getStart() + " to " + ctx.expr_in_assign.getStop());
        result.Value = (ExprNode) value;
        if (ctx.ID().size() > 1) {
            Utils.ASSERT(result.Value.Type instanceof TupleType, "Trying to match non tuple type, " + result.Value.Type + ", to multiple names");
            var tuple = (TupleType) result.Value.Type;
            Utils.ASSERT((tuple.Types.size() <= ctx.ID().size()), "Match failure in assign, mismatched amount");
            Queue<FNNType> right = new LinkedList<>(tuple.Types);
            Queue<FNNType> left = new LinkedList<>();
            if (ctx.ID().size() != tuple.Types.size()) {
                boolean did_we_unpack_tuples = false;
                while (left.size() + right.size() != ctx.ID().size()) {
                    var current = right.remove();
                    if (!(current instanceof TupleType)) {
                        left.add(current);
                    } else {
                        did_we_unpack_tuples = true;
                        for (var t : ((TupleType) current).Types) {
                            left.add(t);
                        }
                    }
                    if (right.size() == 0) {
                        Utils.ASSERT(did_we_unpack_tuples, "Number of variables do not match number of elements in tuple" + ctx.getStart() + ":" + ctx.getStop());
                        var temp = right;
                        right = left;
                        left = temp;
                    }
                }
            }
            int i = 0;
            while (left.size() > 0) {
                var name = ctx.ID(i++).getText();
                var type = left.remove();
                result.Types.add(type);
                result.Names.add(name);
                Scopes.peek().put(name, type);
                System.out.println("ASSIGN: " + name + " : " + type);
            }
            while (right.size() > 0) {
                var name = ctx.ID(i++).getText();
                var type = right.remove();
                result.Types.add(type);
                result.Names.add(name);
                Scopes.peek().put(name, type);
                System.out.println("ASSIGN: " + name + " : " + type);
            }
        }

        else {
            var name = ctx.ID(0).getText();
            result.Types.add(result.Value.Type);
            result.Names.add(name);
            Scopes.peek().put(name, result.Value.Type);
            System.out.println("ASSIGN: " + name + " : " + result.Value.Type);
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
                result.Type = type;
                result.Name = name;
                return result;
            }
        }
        Utils.ERREXIT("Could not evaluate variable " + name + " : " + ctx.getStart() + " to " + ctx.getStop());
        return null;
    }

    @Override
    public AstNode visitFunction_declaration(FNNParser.Function_declarationContext ctx) {
        Utils.ERREXIT("FUNCTIONS NOT IMPLEMENTED");
        return null;
    }

    @Override
    public IntNode visitIntlit(FNNParser.IntlitContext ctx) {
        IntNode result = new IntNode();
        result.Type = new BaseType(TypeEnum.Int);
        result.Value = Integer.parseInt(ctx.getText());
        return result;
    }

    @Override
    public FloatNode visitFloatlit(FNNParser.FloatlitContext ctx) {
        FloatNode result = new FloatNode();
        result.Type = new BaseType(TypeEnum.Float);
        result.Value = Float.parseFloat(ctx.getText());
        return result;
    }

    @Override
    public LayerNode visitLayerlit(FNNParser.LayerlitContext ctx) {
        LayerNode result = new LayerNode();
        result.Type = new BaseType(TypeEnum.Layer);
        var inputsize = this.visit(ctx.input_size);
        Utils.ASSERT(inputsize instanceof ExprNode, "Input size of layer must be an expression: " + ctx.input_size.getStart() + " to " + ctx.input_size.getStop());
        result.InputSize = (ExprNode) inputsize;
        Utils.ASSERT(result.InputSize.Type instanceof BaseType, "Input size of layer must be a single value expression: " + ctx.input_size.getStart() + " to " + ctx.input_size.getStop());
        Utils.ASSERT(((BaseType) result.InputSize.Type).Type == TypeEnum.Int, "Input size of layer must be an integer: " + ctx.input_size.getStart() + " to " + ctx.input_size.getStop());

        var outputsize = this.visit(ctx.output_size);
        Utils.ASSERT(outputsize instanceof ExprNode, "Output size of layer must be an expression: " + ctx.output_size.getStart() + " to " + ctx.output_size.getStop());
        result.OutputSize = (ExprNode) outputsize;
        Utils.ASSERT(result.OutputSize.Type instanceof BaseType, "Output size of layer must be a single value expression: " + ctx.output_size.getStart() + " to " + ctx.output_size.getStop());
        Utils.ASSERT(((BaseType) result.OutputSize.Type).Type == TypeEnum.Int, "Output size of layer must be an integer: " + ctx.output_size.getStart() + " to " + ctx.output_size.getStop());

        result.ActivationFunction = ctx.activation_function.getText();
        return result;
    }

    @Override
    public ModelNode visitModellit(FNNParser.ModellitContext ctx) {
        var result = new ModelNode();
        result.Type = new BaseType(TypeEnum.Model);
        result.Layers = new Vector<>();
        for (var expr : ctx.children.subList(2, ctx.getChildCount() - 1)) { // TODO: gotta be a better way to get all
                                                                            // the expressions without the "model<>"
                                                                            // part
            var layer = this.visit(expr);
            Utils.ASSERT(layer instanceof ExprNode, "Model parameters must be expressions: " + ((ParserRuleContext) expr.getPayload()).getStart() + " to " + ((ParserRuleContext) expr.getPayload()).getStop());
            var exprNode = (ExprNode) layer;
            if (exprNode.Type instanceof TupleType) {
                for (FNNType type : ((TupleType) exprNode.Type).Types) {
                    Utils.ASSERT(type instanceof BaseType, "Multiple nested tuples not supported for model declaration");
                    Utils.ASSERT(((BaseType) type).Type == TypeEnum.Layer, "Model parameters must be layers, not " + exprNode.Type + ": " + ((ParserRuleContext) expr.getPayload()).getStart() + " to " + ((ParserRuleContext) expr.getPayload()).getStop());
                    result.Layers.add(exprNode);
                }
            } else if (exprNode.Type instanceof BaseType) {

            } else {
                Utils.ERREXIT("Paramters in model cannot be of type: " + exprNode.Type);
            }
        }
        return result;
    }

    @Override
    public StringNode visitStrlit(FNNParser.StrlitContext ctx) {
        var result = new StringNode();
        result.Type = new BaseType(TypeEnum.String);
        result.Value = ctx.STR_CONTENT().getText();
        if (result.Value == null)
            result.Value = "";
        return result;
    }

    @Override
    public TrainNode visitTrain_stmt(FNNParser.Train_stmtContext ctx) {
        var result = new TrainNode();
        var epochs = this.visit(ctx.epochs);
        Utils.ASSERT(epochs instanceof ExprNode, "Epochs in training must be an expression: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());
        result.Epochs = (ExprNode) epochs;
        Utils.ASSERT(result.Epochs.Type instanceof BaseType, "Epoch in training must be a single value expression");
        Utils.ASSERT(((BaseType) result.Epochs.Type).Type == TypeEnum.Int, "Epochs in training must be an integer: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());

        var batchSize = this.visit(ctx.batch_size);
        Utils.ASSERT(batchSize instanceof ExprNode, "Batch size in training must be an expression: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());
        result.BatchSize = (ExprNode) batchSize;
        Utils.ASSERT(result.BatchSize.Type instanceof BaseType, "Batch size in training must be a single value expression");
        Utils.ASSERT(((BaseType) result.BatchSize.Type).Type == TypeEnum.Int, "Batch size in training must be an integer: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());

        var model = this.visit(ctx.model);
        Utils.ASSERT(model instanceof ExprNode, "Model in training must be an expression: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());
        result.Model = (ExprNode) model;
        Utils.ASSERT(result.Model.Type instanceof BaseType, "Model in training must be a single value expression");
        Utils.ASSERT(((BaseType) result.Model.Type).Type == TypeEnum.Model, "Model in training must be an model: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());

        return result;
    }

    @Override
    public CallNode visitCall(FNNParser.CallContext ctx) {
        var result = new CallNode();
        var func_expr = this.visit(ctx.func);
        Utils.ASSERT(func_expr instanceof ExprNode, "Attempting to call non expression");
        result.Function = (ExprNode) func_expr;
        Utils.ASSERT(result.Function.Type instanceof FuncType, "Attempting to call expression that doesn't evaluate to a function");
        var args_node = this.visit(ctx.exprs);
        Utils.ASSERT(args_node instanceof ExprListNode, "I'm still not entirely sure when you'd get this error... exprs is somehow not a list of exprs");
        var func_type = (FuncType) ((ExprNode) func_expr).Type;
        TupleType arg_type = new TupleType();
        for (var expr : ((ExprListNode) args_node).Exprs) {
            arg_type.Types.add(expr.Type);
        }

        Utils.ASSERT(arg_type.equals(func_type.Arg), "Types in function call doesn't match, expected: " + func_type.Arg + ", got: " + arg_type);
        result.Args = (((ExprListNode) args_node).Exprs);

        result.Type = ((FuncType) result.Function.Type).Ret;

        return result;
    }

    @Override
    public ExternNode visitExtern(FNNParser.ExternContext ctx) {
        var result = new ExternNode();
        var name = ctx.ID().getText();

        var type_node = this.visit(ctx.type());
        Utils.ASSERT(type_node instanceof TypeNode, "Type of extern declaration bad");
        var type = ((TypeNode) type_node).Type;

        Scopes.peek().put(name, type);

        System.out.println("DCLR EXTERN: " + name + " : " + type);

        result.Name = name;
        result.Type = type;
        return result;
    }

    @Override
    public TypeNode visitBasetypelit(FNNParser.BasetypelitContext ctx) {
        var result = new TypeNode();
        switch (ctx.BASETYPE().getText()) {
        case "STR":
            result.Type = new BaseType(TypeEnum.String);
            break;
        case "INT":
            result.Type = new BaseType(TypeEnum.Int);
            break;
        case "FLT":
            result.Type = new BaseType(TypeEnum.Float);
            break;
        case "LYR":
            result.Type = new BaseType(TypeEnum.Layer);
            break;
        case "MDL":
            result.Type = new BaseType(TypeEnum.Model);
            break;
        default:
            Utils.ERREXIT("Mismatched basetype: " + ctx.BASETYPE().getText());
            break;
        }
        return result;
    }

    @Override
    public TypeNode visitFunctypelit(FNNParser.FunctypelitContext ctx) {
        var result = new TypeNode();
        var type = new FuncType();
        var rets = this.visit(ctx.rets);
        Utils.ASSERT(rets instanceof TypeListNode, "Specificed returntypes not actually a list of types somehow");
        TupleType ret_tuple = new TupleType();
        for (var return_type : ((TypeListNode) rets).Types) {
            ret_tuple.Types.add(return_type.Type);
        }
        type.Ret = ret_tuple;
        var args = this.visit(ctx.args);
        Utils.ASSERT(args instanceof TypeListNode, "Specificed argumettypes not actually a list of types somehow");
        TupleType arg_tuple = new TupleType();
        for (var arg_type : ((TypeListNode) args).Types) {
            arg_tuple.Types.add(arg_type.Type);
        }
        type.Arg = arg_tuple;
        result.Type = type;
        return result;
    }

    @Override
    public TypeNode visitTupletypelit(FNNParser.TupletypelitContext ctx) {
        var result = new TypeNode();
        var type = new TupleType();
        var types = this.visit(ctx.tupletypes);
        Utils.ASSERT(types instanceof TypeListNode, "Specificed tupletypes not actually a list of types somehow");
        for (var return_type : ((TypeListNode) types).Types) {
            type.Types.add(return_type.Type);
        }
        result.Type = type;
        return result;
    }

    @Override
    public TypeNode visitArrtypelit(FNNParser.ArrtypelitContext ctx) {
        var result = new TypeNode();
        var arrtypenode = this.visit(ctx.arrtype);
        Utils.ASSERT(arrtypenode instanceof TypeNode, "Type in array is not actually a type???? huh??");
        var type = new ArrType(((TypeNode) arrtypenode).Type, 10); // TODO: something about the size??
        result.Type = type;
        return result;
    }
}