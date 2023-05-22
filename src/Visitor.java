import java.util.*;

public class Visitor extends FNNBaseVisitor<AstNode> {

    public Stack<Map<String, FNNType>> Scopes;

    public Visitor() {
        Scopes = new Stack<>();
        Scopes.push(new HashMap<>());
    }

    @Override
    public ProgramNode visitProgram(FNNParser.ProgramContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        ProgramNode result = new ProgramNode();
        var stmtList = this.visit(ctx.stmts);
        Utils.ASSERT(stmtList instanceof StmtListNode, "I legit don't know how we'd ever get this error lmao");
        result.Stmts = ((StmtListNode) stmtList).Stmts; // This is fucking retarded
        return result;
    }

    @Override
    public StmtListNode visitStmtlist(FNNParser.StmtlistContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new StmtListNode();
        if (ctx.children != null) {
            for (var antlr_stmt_node : ctx.children) {
                System.out.println("going to visit: " + antlr_stmt_node.getText());
                var ast_stmt_node = this.visit(antlr_stmt_node);
                if (ast_stmt_node != null) { // TODO: figure out why this can happen
                    Utils.ASSERT(ast_stmt_node instanceof StmtNode, "Found " + ast_stmt_node + " in stmtList, which is not a statement");
                    result.Stmts.add((StmtNode) ast_stmt_node);
                }
            }
        }
        return result;
    }

    @Override
    public ExprListNode visitExprlist(FNNParser.ExprlistContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new ExprListNode();
        for (var antlr_expr_node : ctx.children) {
            var ast_expr_node = this.visit(antlr_expr_node);
            Utils.ASSERT(ast_expr_node instanceof ExprNode, "Somehow we have a thing that was parsed to an exprlist, " + antlr_expr_node.getText() + ", but when visiting the children it doesn't result in an ExprNode, on line: " + ctx.start.getLine());
            result.Exprs.add((ExprNode) ast_expr_node);
        }
        return result;
    }

    @Override
    public TypeListNode visitTypelist(FNNParser.TypelistContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new TypeListNode();
        if (ctx == null || ctx.children == null)
            return result; // TODO: apparently we need to check this ain't null, so we need to do that
                           // everywhere else lmao

        for (var antlr_type_node : ctx.children) {
            var ast_type_node = this.visit(antlr_type_node);
            Utils.ASSERT(ast_type_node instanceof TypeNode, "Somehow we have a thing that was parsed to an typelist, but when visiting the children it doesn't result in an TypeNode");
            result.Types.add((TypeNode) ast_type_node);
        }
        return result;
    }

    @Override
    public WhileNode visitWhile_stmt(FNNParser.While_stmtContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        WhileNode result = new WhileNode();

        var predicate = this.visit(ctx.predicate);
        Utils.ASSERT(predicate instanceof ExprNode, "Predicate of while stmt was not an expression, on line: " + ctx.predicate.getStart().getLine());
        result.Predicate = (ExprNode) predicate;
        var predicate_target_type = new BaseType(TypeEnum.Int);
        Utils.ASSERT((result.Predicate.Type).equals(predicate_target_type), "Predicate of while stmt is of type: " + result.Predicate.Type + ", should be: " + predicate_target_type + ", on line: " + ctx.predicate.getStart().getLine());

        Scopes.push(new HashMap<String, FNNType>()); // New scope for functions
        var stmts = this.visit(ctx.stmts);
        Scopes.pop(); // Remove function scope

        Utils.ASSERT(stmts instanceof StmtListNode, "Body of while stmt was not a stmtlist, on line: " + ctx.stmts.getStart().getLine());
        result.Stmts = ((StmtListNode) stmts).Stmts;

        return result;
    }

    @Override
    public BiOperatorNode visitBiop(FNNParser.BiopContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        BiOperatorNode result = new BiOperatorNode();

        var l = this.visit(ctx.left_op);
        Utils.ASSERT(l instanceof ExprNode, "Left operand of bi-operator was not an expression, on line: " + ctx.start.getLine());
        result.Left = (ExprNode) l;

        var r = this.visit(ctx.right_op);
        Utils.ASSERT(r instanceof ExprNode, "Right operand of bi-operator was not an expression, on line: " + ctx.start.getLine());
        result.Right = (ExprNode) r;

        Utils.ASSERT(result.Right.Type.equals(new BaseType(TypeEnum.Float)) || result.Right.Type.equals(new BaseType(TypeEnum.Int)), "Binary operations can only be used on expressions of type FLT or INT, not " + result.Right.Type + ", on line: " + ctx.start.getLine());
        Utils.ASSERT(result.Left.Type.equals(new BaseType(TypeEnum.Float)) || result.Left.Type.equals(new BaseType(TypeEnum.Int)), "Binary operations can only be used on expressions of type FLT or INT, not " + result.Left.Type + ", on line: " + ctx.start.getLine());
        // TODO: Consider Lefther types
        Utils.ASSERT(result.Left.Type.equals(result.Right.Type), "Bi operation: " + ctx.OPERATOR().getText() + ", between mismatched types: " + result.Left.Type + " and " + result.Right.Type + ", on line: " + ctx.start.getLine());

        result.Operator = OpEnum.parseChar(ctx.OPERATOR().getText().charAt(0));
        if (result.Operator == OpEnum.GreaterThan || result.Operator == OpEnum.LessThan || result.Operator == OpEnum.Equals)
            result.Type = new BaseType(TypeEnum.Int);
        else
            result.Type = result.Left.Type;

        return result;
    }

    @Override
    public UnOperatorNode visitUnop(FNNParser.UnopContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        UnOperatorNode result = new UnOperatorNode();

        var op = this.visit(ctx.op);
        Utils.ASSERT(op instanceof ExprNode, "Left operand of bi-operator was not an expression: " + ctx.op.getStart() + " to " + ctx.op.getStop());
        result.Operand = (ExprNode) op;
        Utils.ASSERT(result.Operand.Type instanceof BaseType, "Unary operations can only be used on single value expressions");
        result.Type = Utils.TRY_UNWRAP(result.Operand.Type);

        result.Operator = OpEnum.parseChar(ctx.OPERATOR().getText().charAt(0));
        if (result.Operator != OpEnum.Minus && result.Operator != OpEnum.Plus)
            Utils.ERREXIT("unexpected unary operator: " + result.Operator);

        return result;
    }

    @Override
    public AssignNode visitAssign(FNNParser.AssignContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new AssignNode();
        var value = this.visit(ctx.expr_in_assign);
        Utils.ASSERT(value instanceof ExprNode, "Right side of assignment was not an expression: " + ctx.expr_in_assign.getStart() + " to " + ctx.expr_in_assign.getStop());
        result.Value = (ExprNode) value;
        result.Value.Type = Utils.TRY_UNWRAP(result.Value.Type); // redundant? we should probably specify a single place that's responsible for the unwrapping

        if (ctx.ID().size() > 1) {
            Utils.ASSERT(result.Value.Type instanceof TupleType, "Attempt to assign non-tuple type: " + result.Value.Type + ", to multiple names, on line: " + ctx.getStart().getLine());
            var t_type = (TupleType) result.Value.Type;
            Utils.ASSERT(ctx.ID().size() == t_type.Types.size(), "Attempt to assign " + t_type.Types.size() + " tuple to " + ctx.ID().size() + " names, on line: " + ctx.getStart().getLine());

            for (int i = 0; i < ctx.ID().size(); i++) {
                var name = ctx.ID(i).getText();
                var type = t_type.Types.get(i);
                result.Types.add(type);
                result.Names.add(name);
                Scopes.peek().put(name, type);
            }
        } else {
            var name = ctx.ID(0).getText();
            var type = result.Value.Type;
            result.Types.add(type);
            result.Names.add(name);
            Scopes.peek().put(name, type);
        }
        return result;
    }

    @Override
    public EvalNode visitEval(FNNParser.EvalContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var name = ctx.ID().getText();
        for (int i = Scopes.size() - 1; i >= 0; i--) {
            var scope = Scopes.get(i);
            var type = scope.get(name);
            if (type != null) {
                System.out.println("EVAL: " + name + " : " + type);
                EvalNode result = new EvalNode();
                result.Type = Utils.TRY_UNWRAP(type);
                result.Name = name;
                return result;
            }
        }
        Utils.ERREXIT("Could not evaluate variable " + name + " on line: " + ctx.getStart().getLine());
        return null;
    }

    @Override
    public FuncNode visitFunctionlit(FNNParser.FunctionlitContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new FuncNode();

        var param_types = this.visit(ctx.params);
        Utils.ASSERT(param_types instanceof ParamDeclListNode, "I give up");

        Scopes.push(new HashMap<String, FNNType>()); // New scope for functions
        var params = new TupleType();
        var rets = new TupleType();

        for (var param : ((ParamDeclListNode) param_types).Params) {
            params.Types.add(param.Type);
            result.ParamNames.add(param.Name);
            Scopes.peek().put(param.Name, param.Type);
        }

        var stmts = this.visit(ctx.stmts);
        Utils.ASSERT(stmts instanceof StmtListNode, "like bruh I literally don't even know how this happened, but like I'm not confident enough in the rest of the program being correct that I think we don't need the check");
        result.Stmts = ((StmtListNode) stmts).Stmts;

        var ret_expr = this.visit(ctx.return_);
        Utils.ASSERT(ret_expr instanceof ExprNode, "Funcitons must return expressions");
        var type = new FuncType();
        rets.Types.add(((ExprNode) ret_expr).Type); // cringe and cringe
        type.Ret = rets;
        type.Args = params.Types;
        result.Type = type;

        result.Result = (ExprNode) ret_expr;

        Scopes.pop(); // Remove function scope

        return result;
    }

    @Override
    public ParamDeclListNode visitParamdecllist(FNNParser.ParamdecllistContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new ParamDeclListNode();

        for (var antlr_paramdecl_node : ctx.children) {
            var ast_paramdecl_node = this.visit(antlr_paramdecl_node);
            Utils.ASSERT(ast_paramdecl_node instanceof ParamDeclNode, "Look man, somethign ain't where it's supposed to be, or something's where it ain't supposed to be, fix it");
            result.Params.add((ParamDeclNode) ast_paramdecl_node);
        }

        return result;
    }

    @Override
    public ParamDeclNode visitParamdecl(FNNParser.ParamdeclContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new ParamDeclNode();

        result.Name = ctx.ID().getText();

        var param_type = this.visit(ctx.param_type);
        Utils.ASSERT(param_type instanceof TypeNode, "lmeow >:3c");
        result.Type = ((TypeNode) param_type).Type;

        return result;
    }

    @Override
    public IntNode visitIntlit(FNNParser.IntlitContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        IntNode result = new IntNode();
        result.Type = new BaseType(TypeEnum.Int);
        result.Value = Integer.parseInt(ctx.getText());
        return result;
    }

    @Override
    public FloatNode visitFloatlit(FNNParser.FloatlitContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        FloatNode result = new FloatNode();
        result.Type = new BaseType(TypeEnum.Float);
        result.Value = Float.parseFloat(ctx.getText());
        return result;
    }

    @Override
    public TupleNode visitTuplelit(FNNParser.TuplelitContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new TupleNode();
        var elems = this.visit(ctx.exprs);
        var type = new TupleType();
        Utils.ASSERT(elems instanceof ExprListNode, "Error");
        for (var expr : ((ExprListNode) elems).Exprs) {
            type.Types.add(expr.Type);
            result.Exprs.add(expr);
        }
        result.Type = type;
        return result;
    }

    @Override
    public NNNode visitNnlit(FNNParser.NnlitContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new NNNode();
        result.Type = new BaseType(TypeEnum.NN);

        // TODO: make constructors to avoid this
        var float_tuple = new TupleType();
        float_tuple.Types.add(new BaseType(TypeEnum.Float));

        var activation_type = new FuncType(); // Define (FLT) -> (FLT), to compare equality with
        activation_type.Args = float_tuple.Types;
        activation_type.Ret = float_tuple;

        var activation_expr = this.visit(ctx.activation);
        Utils.ASSERT(activation_expr instanceof ExprNode, "activation function must be an expression, on line: " + ctx.start.getLine());
        Utils.ASSERT(((ExprNode) activation_expr).Type instanceof FuncType && ((ExprNode) activation_expr).Type.equals(activation_type), "activation function must have type: " + activation_type + " , on line: " + ctx.start.getLine());
        result.Activation = (ExprNode) activation_expr;

        var derivative_expr = this.visit(ctx.derivative);
        Utils.ASSERT(derivative_expr instanceof ExprNode, "activation derivative function must be an expression, on line: " + ctx.start.getLine());
        Utils.ASSERT(((ExprNode) derivative_expr).Type instanceof FuncType && ((ExprNode) derivative_expr).Type.equals(activation_type), "activation derivative function must have type: " + activation_type + " , on line: " + ctx.start.getLine());
        result.Derivative = (ExprNode) derivative_expr;

        result.LayerSizes = new Vector<>();

        var layer_size_list = this.visit(ctx.sizes);
        Utils.ASSERT(layer_size_list instanceof ExprListNode, "BRUH");

        for (var size_expr : ((ExprListNode) layer_size_list).Exprs) {
            Utils.ASSERT(size_expr.Type.equals(new BaseType(TypeEnum.Int)), "NN layer sizes must be ints, on line: " + ctx.start.getLine());
            result.LayerSizes.add(size_expr);
        }
        return result;
    }

    @Override
    public StringNode visitStrlit(FNNParser.StrlitContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new StringNode();
        result.Type = new BaseType(TypeEnum.String);
        var content = ctx.STR();
        if (content == null)
            result.Value = "";
        else
            result.Value = content.getText().substring(1, content.getText().length() - 1);
        return result;
    }

    @Override
    public ArrNode visitArrlit(FNNParser.ArrlitContext ctx) {
        var result = new ArrNode();

        var exprs = ((ExprListNode) this.visit(ctx.exprs)).Exprs;
        Utils.ASSERT(exprs.size() > 0, "Empty array litteral, on line: " + ctx.getStart().getLine());
        var type = exprs.get(0).Type;
        result.Type = new ArrType(type);

        for (ExprNode expr : exprs) {
            Utils.ASSERT(expr.Type.equals(type), "Type mismatch in array, got " + expr.Type + ", expected " + type + ", on line: " + ctx.getStart().getLine());
        }

        result.Exprs = exprs;
        result.Count = exprs.size();

        return result;
    }

    @Override
    public TrainNode visitTrain_stmt(FNNParser.Train_stmtContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new TrainNode();
        var epochs = this.visit(ctx.epochs);
        Utils.ASSERT(epochs instanceof ExprNode, "Epochs in training must be an expression: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());
        result.Epochs = (ExprNode) epochs;
        Utils.ASSERT(result.Epochs.Type instanceof BaseType, "Epoch in training must be a single value expression");
        Utils.ASSERT(((BaseType) result.Epochs.Type).Type == TypeEnum.Int, "Epochs in training must be an integer, on line: " + ctx.epochs.getStart().getLine());

        var rate = this.visit(ctx.rate);
        Utils.ASSERT(rate instanceof ExprNode, "Rate in training must be an expression, on line: " + ctx.epochs.getStart().getLine());
        result.Rate = (ExprNode) rate;
        Utils.ASSERT(result.Rate.Type instanceof BaseType, "Rate in training must be a single value expression");
        Utils.ASSERT(((BaseType) result.Rate.Type).Type == TypeEnum.Float, "Rate in training must be an float, on line: " + ctx.epochs.getStart().getLine());

        EvalNode nn = null;
        var name = ctx.nn.getText();
        for (int i = Scopes.size() - 1; i >= 0; i--) {
            System.out.println("scope: " + i + "/" + (Scopes.size() - 1));
            var scope = Scopes.get(i);
            var type = scope.get(name);
            if (type != null) {
                System.out.println("EVAL: " + name + " : " + type);
                nn = new EvalNode();
                nn.Type = Utils.TRY_UNWRAP(type);
                nn.Name = name;
            }
        }
        Utils.ASSERT(nn != null, "Could not evaluate variable " + name + " on line: " + ctx.getStart().getLine());
        result.NN = nn;
        Utils.ASSERT(result.NN.Type instanceof BaseType, "NN in training must be a single value expression, on line: " + ctx.nn.getLine());
        Utils.ASSERT(((BaseType) result.NN.Type).Type == TypeEnum.NN, "Model in training must be an model, on line: " + ctx.nn.getLine());

        var inputData = this.visit(ctx.input);
        Utils.ASSERT(inputData instanceof ExprNode, "Input Data in train must be an expression, on line: " + ctx.input.getStart().getLine());
        result.Input = (ExprNode) inputData;
        Utils.ASSERT(result.Input.Type instanceof ArrType, "Input Data in train must be an array");
        // TODO: more type checking, I can't be assed

        var expectedOutput = this.visit(ctx.expected);
        Utils.ASSERT(expectedOutput instanceof ExprNode, "Expected Output in train must be an expression, on line: " + ctx.expected.getStart().getLine());
        result.Expected = (ExprNode) expectedOutput;
        Utils.ASSERT(result.Expected.Type instanceof ArrType, "Expected Putput in train must be an array");
        // TODO: more type checking, I can't be assed

        return result;
    }

    @Override
    public CallNode visitCall(FNNParser.CallContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new CallNode();
        var func_expr = this.visit(ctx.func);
        Utils.ASSERT(func_expr instanceof ExprNode, "Attempting to call non expression");
        result.Function = (ExprNode) func_expr;
        Utils.ASSERT(result.Function.Type instanceof FuncType, "Attempting to call expression, " + ctx.getText() + ", that doesn't evaluate to a function, on line " + ctx.start.getLine());
        var args_node = this.visit(ctx.exprs);
        Utils.ASSERT(args_node instanceof ExprListNode, "I'm still not entirely sure when you'd get this error... exprs is somehow not a list of exprs");
        var func_type = (FuncType) ((ExprNode) func_expr).Type;
        TupleType arg_type = new TupleType();
        for (var expr : ((ExprListNode) args_node).Exprs) {
            arg_type.Types.add(expr.Type);
        }

        var exp_arg_type = new TupleType();
        exp_arg_type.Types = func_type.Args;

        Utils.ASSERT(arg_type.equals(exp_arg_type), "Types in function call doesn't match, expected: " + exp_arg_type + ", got: " + arg_type);
        result.Args = (((ExprListNode) args_node).Exprs);

        result.Type = Utils.TRY_UNWRAP(((FuncType) result.Function.Type).Ret);

        return result;
    }

    @Override
    public ExternNode visitExtern(FNNParser.ExternContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new ExternNode();
        var name = ctx.ID().getText();

        var type_node = this.visit(ctx.type());
        Utils.ASSERT(type_node instanceof TypeNode, "Type of extern declaration bad");
        var type = ((TypeNode) type_node).Type;

        Scopes.peek().put(name, type);

        System.out.println("DCLR EXTERN: " + name + " : " + type);

        result.Name = name;
        result.Type = Utils.TRY_UNWRAP(type);
        return result;
    }

    @Override
    public TypeNode visitBasetypelit(FNNParser.BasetypelitContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

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
        case "FNN":
            result.Type = new BaseType(TypeEnum.NN);
            break;
        default:
            Utils.ERREXIT("Mismatched basetype: " + ctx.BASETYPE().getText());
            break;
        }
        return result;
    }

    @Override
    public TypeNode visitFunctypelit(FNNParser.FunctypelitContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

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
        for (var arg_type : ((TypeListNode) args).Types) {
            type.Args.add(arg_type.Type);
        }
        result.Type = Utils.TRY_UNWRAP(type);
        return result;
    }

    @Override
    public TypeNode visitTupletypelit(FNNParser.TupletypelitContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new TypeNode();
        var type = new TupleType();
        var types = this.visit(ctx.tupletypes);
        Utils.ASSERT(types instanceof TypeListNode, "Specificed tupletypes not actually a list of types somehow");
        for (var return_type : ((TypeListNode) types).Types) {
            type.Types.add(return_type.Type);
        }
        result.Type = Utils.TRY_UNWRAP(type);
        return result;
    }

    @Override
    public TypeNode visitArrtypelit(FNNParser.ArrtypelitContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new TypeNode();
        var arrtypenode = this.visit(ctx.arrtype);
        Utils.ASSERT(arrtypenode instanceof TypeNode, "Type in array is not actually a type???? huh??");
        var type = new ArrType(((TypeNode) arrtypenode).Type);
        result.Type = Utils.TRY_UNWRAP(type);
        return result;
    }

    @Override
    public ArrAccessNode visitArraccess(FNNParser.ArraccessContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new ArrAccessNode();

        var arr = this.visit(ctx.arr);
        Utils.ASSERT(arr instanceof ExprNode, "Array must be an expression, not: " + arr.getClass());
        result.Array = (ExprNode) arr;
        Utils.ASSERT(result.Array.Type instanceof ArrType, "Cannot index non-array type: " + result.Array.Type);
        result.Type = ((ArrType) result.Array.Type).Type;

        var index = this.visit(ctx.index);
        Utils.ASSERT(index instanceof ExprNode, "Array index must be an expression, not: " + index.getClass());
        result.Index = (ExprNode) index;
        Utils.ASSERT(result.Index.Type.equals(new BaseType(TypeEnum.Int)), "Index must evaluate to an int, not: " + result.Index.Type);

        return result;
    }

    @Override
    public TestNode visitTestexpr(FNNParser.TestexprContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new TestNode();

        var nn = this.visit(ctx.nn);
        Utils.ASSERT(nn instanceof ExprNode && ((ExprNode) nn).Type.equals(new BaseType(TypeEnum.NN)), "Can only test models, not " + nn + " on line: " + ctx.getStart().getLine());
        result.NN = (ExprNode) nn;

        var float_arr_arr_type = new ArrType(new ArrType(new BaseType(TypeEnum.Float)));

        var in = this.visit(ctx.in);
        Utils.ASSERT(in instanceof ExprNode && ((ExprNode) in).Type.equals(float_arr_arr_type), "Can only test models with in:[[FLT]], not " + in + " on line: " + ctx.getStart().getLine());
        result.In = (ExprNode) in;

        var out = this.visit(ctx.out);
        Utils.ASSERT(out instanceof ExprNode && ((ExprNode) out).Type.equals(float_arr_arr_type), "Can only test models with out:[[FLT]], not " + out + " on line: " + ctx.getStart().getLine());
        result.Out = (ExprNode) out;

        result.Type = new BaseType(TypeEnum.Float);

        return result;
    }
}