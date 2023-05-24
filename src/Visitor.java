import java.util.*;

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
        result.Stmts = ((StmtListNode) stmtList).Stmts;
        return result;
    }

    @Override
    public StmtListNode visitStmtlist(FNNParser.StmtlistContext ctx) {
        var result = new StmtListNode();
        if (ctx.children != null) {
            for (var antlr_stmt_node : ctx.children) {
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
        var result = new ExprListNode();

        if (ctx.children == null) { // TODO: Make this allowed if we want empty tuples
            Utils.ERREXIT("Empty exprlist, on line: " + ctx.getStart().getLine());
        }

        for (var antlr_expr_node : ctx.children) {
            var ast_expr_node = this.visit(antlr_expr_node);
            Utils.ASSERT(ast_expr_node instanceof ExprNode, "Somehow we have a thing that was parsed to an exprlist, " + antlr_expr_node.getText() + ", but when visiting the children it doesn't result in an ExprNode, on line: " + ctx.getStart().getLine());
            result.Exprs.add((ExprNode) ast_expr_node);
        }
        return result;
    }

    @Override
    public TypeListNode visitTypelist(FNNParser.TypelistContext ctx) {
        var result = new TypeListNode();
        if (ctx == null || ctx.children == null)
            return result; // TODO: apparently we need to check this ain't null, so we need to do that everywhere else lmao

        for (var antlr_type_node : ctx.children) {
            var ast_type_node = this.visit(antlr_type_node);
            Utils.ASSERT(ast_type_node instanceof TypeNode, "Somehow we have a thing that was parsed to an typelist, but when visiting the children it doesn't result in an TypeNode");
            result.Types.add((TypeNode) ast_type_node);
        }
        return result;
    }

    @Override
    public WhileNode visitWhile_stmt(FNNParser.While_stmtContext ctx) {
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
        BiOperatorNode result = new BiOperatorNode();

        var l = this.visit(ctx.left_op);
        Utils.ASSERT(l instanceof ExprNode, "Left operand of bi-operator was not an expression, on line: " + ctx.getStart().getLine());
        result.Left = (ExprNode) l;

        var r = this.visit(ctx.right_op);
        Utils.ASSERT(r instanceof ExprNode, "Right operand of bi-operator was not an expression, on line: " + ctx.getStart().getLine());
        result.Right = (ExprNode) r;

        Utils.ASSERT(result.Right.Type.equals(new BaseType(TypeEnum.Float)) || result.Right.Type.equals(new BaseType(TypeEnum.Int)), "Binary operations can only be used on expressions of type FLT or INT, not " + result.Right.Type + ", on line: " + ctx.getStart().getLine());
        Utils.ASSERT(result.Left.Type.equals(new BaseType(TypeEnum.Float)) || result.Left.Type.equals(new BaseType(TypeEnum.Int)), "Binary operations can only be used on expressions of type FLT or INT, not " + result.Left.Type + ", on line: " + ctx.getStart().getLine());
        Utils.ASSERT(result.Left.Type.equals(result.Right.Type), "Bi operation: " + ctx.OPERATOR().getText() + ", between mismatched types: " + result.Left.Type + " and " + result.Right.Type + ", on line: " + ctx.getStart().getLine());

        result.Operator = OpEnum.parseChar(ctx.OPERATOR().getText().charAt(0));
        if (result.Operator == OpEnum.GreaterThan || result.Operator == OpEnum.LessThan || result.Operator == OpEnum.Equals)
            result.Type = new BaseType(TypeEnum.Int);
        else
            result.Type = result.Left.Type;

        return result;
    }

    @Override
    public UnOperatorNode visitUnop(FNNParser.UnopContext ctx) {
        UnOperatorNode result = new UnOperatorNode();

        var op = this.visit(ctx.op);
        Utils.ASSERT(op instanceof ExprNode, "Operand of unary operator was not an expression, on line: " + ctx.op.getStart().getLine());
        result.Operand = (ExprNode) op;
        result.Type = Utils.TRY_UNWRAP(result.Operand.Type);
        Utils.ASSERT(result.Operand.Type.equals(new BaseType(TypeEnum.Float)) || result.Operand.Type.equals(new BaseType(TypeEnum.Int)), "Unary operations can only be used on expressions of type FLT or INT, not " + result.Operand.Type + ", on line: " + ctx.getStart().getLine());

        result.Operator = OpEnum.parseChar(ctx.OPERATOR().getText().charAt(0));
        if (result.Operator != OpEnum.Minus && result.Operator != OpEnum.Plus)
            Utils.ERREXIT("unexpected unary operator: " + result.Operator + ", on line: " + ctx.getStart().getLine());

        return result;
    }

    @Override
    public AssignNode visitAssign(FNNParser.AssignContext ctx) {
        var result = new AssignNode();
        var value = this.visit(ctx.expr_in_assign);
        Utils.ASSERT(value instanceof ExprNode, "Right side of assignment was not an expression, on line: " + ctx.expr_in_assign.getStart().getLine());
        result.Value = (ExprNode) value;
        result.Value.Type = Utils.TRY_UNWRAP(result.Value.Type); // redundant? we should probably specify a single place that's responsible for the unwrapping

        if (ctx.ID().size() > 1) {
            Utils.ASSERT(result.Value.Type instanceof TupleType, "Attempt to assign non-tuple type: " + result.Value.Type + ", to multiple names, on line: " + ctx.getStart().getLine());
            var t_type = (TupleType) result.Value.Type;
            Utils.ASSERT(ctx.ID().size() == t_type.Types.size(), "Attempt to assign " + t_type.Types.size() + " tuple " + t_type + " to " + ctx.ID().size() + " names " + ctx.ID() + ", on line: " + ctx.getStart().getLine());

            for (int i = 0; i < ctx.ID().size(); i++) {
                var name = ctx.ID(i).getText();
                var type = t_type.Types.get(i);
                result.Types.add(type);
                result.Names.add(name);
                var scope = getScopeWith(name);
                if (scope == null) {
                    Scopes.peek().put(name, type); // if it's not already in any scope, put it in the top one
                } else {
                    var old_type = scope.get(name);
                    Utils.ASSERT(type.equals(old_type), "Trying to assign value of type: " + type + " to already used variable (" + name + ") of type: " + old_type + ", on line: " + ctx.getStart().getLine());
                }
            }
        } else {
            var name = ctx.ID(0).getText();
            var type = result.Value.Type;
            result.Types.add(type);
            result.Names.add(name);
            var scope = getScopeWith(name);
            if (scope == null) {
                Scopes.peek().put(name, type); // if it's not already in any scope, put it in the top one
            } else {
                var old_type = scope.get(name);
                Utils.ASSERT(type.equals(old_type), "Trying to assign value of type: " + type + " to already used variable (" + name + ") of type: " + old_type + ", on line: " + ctx.getStart().getLine());
            }
        }
        return result;
    }

    // returns the first map in the stack that contains the name, or null if none does
    public Map<String, FNNType> getScopeWith(String name) {
        for (int i = Scopes.size() - 1; i >= 0; i--) {
            var scope = Scopes.get(i);
            var type = scope.get(name);
            if (type != null) {
                return scope;
            }
        }
        return null;
    }

    @Override
    public EvalNode visitEval(FNNParser.EvalContext ctx) {
        var name = ctx.ID().getText();
        for (int i = Scopes.size() - 1; i >= 0; i--) {
            var scope = Scopes.get(i);
            var type = scope.get(name);
            if (type != null) {
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
        var result = new FuncNode();

        var param_types = this.visit(ctx.params);
        Utils.ASSERT(param_types instanceof ParamDeclListNode, "Function parameters error, on line: " + ctx.stmts.getStart().getLine()); // Might be redundant, depending on how ANTLR handles errors

        Scopes.push(new HashMap<String, FNNType>()); // New scope for functions
        var params = new TupleType();
        var rets = new TupleType();

        for (var param : ((ParamDeclListNode) param_types).Params) {
            params.Types.add(param.Type);
            result.ParamNames.add(param.Name);
            Scopes.peek().put(param.Name, param.Type);
        }

        var stmts = this.visit(ctx.stmts);
        Utils.ASSERT(stmts instanceof StmtListNode, "Function body not a stmt list, on line: " + ctx.stmts.getStart().getLine()); // Might be redundant, depending on how ANTLR handles errors
        result.Stmts = ((StmtListNode) stmts).Stmts;

        var ret_expr = this.visit(ctx.return_);
        Utils.ASSERT(ret_expr instanceof ExprNode, "Function return is not an expression, on line: " + ctx.return_.getStart().getLine()); // Might be redundant, depending on how ANTLR handles errors
        var type = new FuncType();
        result.Result = (ExprNode) ret_expr;
        rets.Types.add(result.Result.Type);
        type.Ret = rets;
        type.Args = params.Types;
        result.Type = type;

        Scopes.pop(); // Remove function scope

        return result;
    }

    @Override
    public ParamDeclListNode visitParamdecllist(FNNParser.ParamdecllistContext ctx) {
        var result = new ParamDeclListNode();

        Utils.ASSERT(ctx.children != null, "Empty parameter declaration, on line: " + ctx.getStart().getLine()); // TODO: consider supporting parameterless functions
        for (var antlr_paramdecl_node : ctx.children) {
            var ast_paramdecl_node = this.visit(antlr_paramdecl_node);
            Utils.ASSERT(ast_paramdecl_node instanceof ParamDeclNode, "Parameter declaration error on line: " + ctx.getStart().getLine()); // Might be redundant
            result.Params.add((ParamDeclNode) ast_paramdecl_node);
        }

        return result;
    }

    @Override
    public ParamDeclNode visitParamdecl(FNNParser.ParamdeclContext ctx) {
        var result = new ParamDeclNode();
        result.Name = ctx.ID().getText();

        var param_type = this.visit(ctx.param_type);
        Utils.ASSERT(param_type instanceof TypeNode, "Type in parameter declaration could not be resolved, on line: " + ctx.getStart().getLine());
        result.Type = ((TypeNode) param_type).Type;

        return result;
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
    public TupleNode visitTuplelit(FNNParser.TuplelitContext ctx) {
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
        var result = new NNNode();
        result.Type = new BaseType(TypeEnum.NN);

        var float_tuple = new TupleType();
        float_tuple.Types.add(new BaseType(TypeEnum.Float));

        var activation_type = new FuncType(); // Define (FLT) -> (FLT), to compare equality with
        activation_type.Args = float_tuple.Types;
        activation_type.Ret = float_tuple;

        var activation_expr = this.visit(ctx.activation);
        Utils.ASSERT(activation_expr instanceof ExprNode, "activation function must be an expression, on line: " + ctx.getStart().getLine());
        Utils.ASSERT(((ExprNode) activation_expr).Type instanceof FuncType && ((ExprNode) activation_expr).Type.equals(activation_type), "activation function must have type: " + activation_type + " , on line: " + ctx.getStart().getLine());
        result.Activation = (ExprNode) activation_expr;

        var derivative_expr = this.visit(ctx.derivative);
        Utils.ASSERT(derivative_expr instanceof ExprNode, "activation derivative function must be an expression, on line: " + ctx.getStart().getLine());
        Utils.ASSERT(((ExprNode) derivative_expr).Type instanceof FuncType && ((ExprNode) derivative_expr).Type.equals(activation_type), "activation derivative function must have type: " + activation_type + " , on line: " + ctx.getStart().getLine());
        result.Derivative = (ExprNode) derivative_expr;

        result.LayerSizes = new Vector<>();

        var layer_size_list = this.visit(ctx.sizes);
        Utils.ASSERT(layer_size_list instanceof ExprListNode, "BRUH");

        for (var size_expr : ((ExprListNode) layer_size_list).Exprs) {
            Utils.ASSERT(size_expr.Type.equals(new BaseType(TypeEnum.Int)), "NN layer sizes must be ints, on line: " + ctx.getStart().getLine());
            result.LayerSizes.add(size_expr);
        }
        return result;
    }

    @Override
    public StringNode visitStrlit(FNNParser.StrlitContext ctx) {
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
        var result = new TrainNode();
        var epochs = this.visit(ctx.epochs);
        Utils.ASSERT(epochs instanceof ExprNode, "Epochs in training must be an expression: " + ctx.epochs.getStart() + " to " + ctx.epochs.getStop());
        result.Epochs = (ExprNode) epochs;
        Utils.ASSERT(result.Epochs.Type.equals(new BaseType(TypeEnum.Int)), "Epochs in training must be an integer, on line: " + ctx.epochs.getStart().getLine());

        var rate = this.visit(ctx.rate);
        Utils.ASSERT(rate instanceof ExprNode, "Rate in training must be an expression, on line: " + ctx.epochs.getStart().getLine());
        result.Rate = (ExprNode) rate;
        Utils.ASSERT(result.Rate.Type.equals(new BaseType(TypeEnum.Float)), "Rate in training must be an float, on line: " + ctx.epochs.getStart().getLine());

        EvalNode nn = null;
        var name = ctx.nn.getText();
        for (int i = Scopes.size() - 1; i >= 0; i--) {
            var scope = Scopes.get(i);
            var type = scope.get(name);
            if (type != null) {
                nn = new EvalNode();
                nn.Type = Utils.TRY_UNWRAP(type);
                nn.Name = name;
            }
        }
        Utils.ASSERT(nn != null, "Could not evaluate variable " + name + " on line: " + ctx.getStart().getLine());
        result.NN = nn;
        Utils.ASSERT(result.NN.Type.equals(new BaseType(TypeEnum.NN)), "Model in training must be an model, on line: " + ctx.nn.getLine());

        var fltarrarr_type = new ArrType(new ArrType(new BaseType(TypeEnum.Float)));

        var inputData = this.visit(ctx.input);
        Utils.ASSERT(inputData instanceof ExprNode, "Input Data in train must be an expression, on line: " + ctx.input.getStart().getLine());
        result.Input = (ExprNode) inputData;
        Utils.ASSERT(result.Input.Type.equals(fltarrarr_type), "Input Data in train must be of type: " + fltarrarr_type + " not: " + result.Input.Type + ", on line : " + ctx.input.getStart().getLine());

        var expectedOutput = this.visit(ctx.expected);
        Utils.ASSERT(expectedOutput instanceof ExprNode, "Expected Output in train must be an expression, on line: " + ctx.expected.getStart().getLine());
        result.Expected = (ExprNode) expectedOutput;
        Utils.ASSERT(result.Expected.Type.equals(fltarrarr_type), "Expected Putput in train must be of type: " + fltarrarr_type + " not: " + result.Expected.Type + ", on line : " + ctx.expected.getStart().getLine());

        return result;
    }

    @Override
    public CallNode visitCall(FNNParser.CallContext ctx) {
        var result = new CallNode();
        var func_expr = this.visit(ctx.func);
        Utils.ASSERT(func_expr instanceof ExprNode, "Attempting to call non expression, " + ctx.getText() + ", on line: " + ctx.func.getStart().getLine());
        result.Function = (ExprNode) func_expr;
        Utils.ASSERT(result.Function.Type instanceof FuncType, "Attempting to call expression, " + ctx.getText() + ", that doesn't evaluate to a function, on line " + ctx.func.getStart().getLine());
        var args_node = this.visit(ctx.exprs);
        Utils.ASSERT(args_node instanceof ExprListNode, "Function argument error, on line: " + ctx.exprs.getStart().getLine());
        var func_type = (FuncType) ((ExprNode) func_expr).Type;
        TupleType arg_type = new TupleType();
        for (var expr : ((ExprListNode) args_node).Exprs) {
            arg_type.Types.add(expr.Type);
        }

        var exp_arg_type = new TupleType();
        exp_arg_type.Types = func_type.Args;

        Utils.ASSERT(arg_type.equals(exp_arg_type), "Types in function call doesn't match, expected: " + exp_arg_type + ", got: " + arg_type + ", on line: " + ctx.exprs.getStart().getLine());
        result.Args = (((ExprListNode) args_node).Exprs);

        result.Type = Utils.TRY_UNWRAP(((FuncType) result.Function.Type).Ret);

        return result;
    }

    @Override
    public ExternNode visitExtern(FNNParser.ExternContext ctx) {
        var result = new ExternNode();
        var name = ctx.ID().getText();

        var type_node = this.visit(ctx.type());
        Utils.ASSERT(type_node instanceof TypeNode, "Extern declaration type error, on line: " + ctx.type().getStart().getLine());
        var type = ((TypeNode) type_node).Type;

        var scope = getScopeWith(name);
        if (scope == null) {
            Scopes.peek().put(name, type); // if it's not already in any scope, put it in the top one
        } else {
            var old_type = scope.get(name);
            Utils.ASSERT(type.equals(old_type), "Trying to declare extern variable with type: " + type + ", but variable (" + name + ") is already used with type: " + old_type + ", on line: " + ctx.getStart().getLine());
        }

        result.Name = name;
        result.Type = Utils.TRY_UNWRAP(type);
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
        case "FNN":
            result.Type = new BaseType(TypeEnum.NN);
            break;
        default:
            Utils.ERREXIT("Mismatched basetype: " + ctx.BASETYPE().getText() + ", on line: " + ctx.getStart().getLine());
            break;
        }
        return result;
    }

    @Override
    public TypeNode visitFunctypelit(FNNParser.FunctypelitContext ctx) {
        var result = new TypeNode();
        var type = new FuncType();
        var rets = this.visit(ctx.rets);
        Utils.ASSERT(rets instanceof TypeListNode, "Return type specification error in function type, on line: " + ctx.rets.getStart().getLine());
        TupleType ret_tuple = new TupleType();
        for (var return_type : ((TypeListNode) rets).Types) {
            ret_tuple.Types.add(return_type.Type);
        }
        type.Ret = ret_tuple;
        var args = this.visit(ctx.args);
        Utils.ASSERT(args instanceof TypeListNode, "Argument type specification error in function type, on line: " + ctx.args.getStart().getLine());
        for (var arg_type : ((TypeListNode) args).Types) {
            type.Args.add(arg_type.Type);
        }
        result.Type = Utils.TRY_UNWRAP(type);
        return result;
    }

    @Override
    public TypeNode visitTupletypelit(FNNParser.TupletypelitContext ctx) {
        var result = new TypeNode();
        var type = new TupleType();
        var types = this.visit(ctx.tupletypes);
        Utils.ASSERT(types instanceof TypeListNode, "Tuple type specification error, on line: " + ctx.tupletypes.getStart().getLine());
        for (var return_type : ((TypeListNode) types).Types) {
            type.Types.add(return_type.Type);
        }
        result.Type = Utils.TRY_UNWRAP(type);
        return result;
    }

    @Override
    public TypeNode visitArrtypelit(FNNParser.ArrtypelitContext ctx) {
        var result = new TypeNode();
        var arrtypenode = this.visit(ctx.arrtype);
        Utils.ASSERT(arrtypenode instanceof TypeNode, "Array type sepcification error, on line: " + ctx.arrtype.getStart().getLine());
        var type = new ArrType(((TypeNode) arrtypenode).Type);
        result.Type = Utils.TRY_UNWRAP(type);
        return result;
    }

    @Override
    public ArrAccessNode visitArraccess(FNNParser.ArraccessContext ctx) {
        var result = new ArrAccessNode();

        var arr = this.visit(ctx.arr);
        Utils.ASSERT(arr instanceof ExprNode, "Array is not an expression, on line: " + ctx.arr.getStart().getLine());
        result.Array = (ExprNode) arr;
        Utils.ASSERT(result.Array.Type instanceof ArrType, "Cannot index non-array type: " + result.Array.Type + ", on line: " + ctx.arr.getStart().getLine()); // kinda expects the type to be unwrapped, however can we know it is?
        result.Type = ((ArrType) result.Array.Type).Type;

        var index = this.visit(ctx.index);
        Utils.ASSERT(index instanceof ExprNode, "Array index must be an expression, on line: " + ctx.index.getStart().getLine());
        result.Index = (ExprNode) index;
        var index_type = new BaseType(TypeEnum.Int);
        Utils.ASSERT(result.Index.Type.equals(index_type), "Array index must be of type: " + index_type + ", not: " + result.Index.Type + ", on line: " + ctx.index.getStart().getLine());

        return result;
    }

    @Override
    public TestNode visitTestexpr(FNNParser.TestexprContext ctx) {
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