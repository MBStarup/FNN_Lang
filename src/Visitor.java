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
            return result; // TODO: apparently we need to check this ain't null, so we need to do that everywhere else lmao

        for (var antlr_type_node : ctx.children) {
            var ast_type_node = this.visit(antlr_type_node);
            Utils.ASSERT(ast_type_node instanceof TypeNode, "Somehow we have a thing that was parsed to an typelist, but when visiting the children it doesn't result in an TypeNode");
            result.Types.add((TypeNode) ast_type_node);
        }
        return result;
    }

    @Override
    public AstNode visitWhile_stmt(FNNParser.While_stmtContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        WhileNode result = new WhileNode();

        var predicate = this.visit(ctx.predicate);
        Utils.ASSERT(predicate instanceof ExprNode, "Predicate of while stmt was not an expression, on line: " + ctx.predicate.getStart().getLine());
        result.Predicate = (ExprNode) predicate;
        var predicate_target_type = new BaseType(TypeEnum.Int);
        Utils.ASSERT(result.Predicate.Type instanceof BaseType && ((BaseType) result.Predicate.Type).equals(predicate_target_type), "Predicate of while stmt is of type: " + result.Predicate.Type + ", should be: " + predicate_target_type + ", on line: " + ctx.predicate.getStart().getLine());

        Scopes.push(new HashMap<String, FNNType>()); // New scope for functions
        var stmts = this.visit(ctx.stmts);
        Scopes.pop(); // Remove function scope

        Utils.ASSERT(stmts instanceof StmtListNode, "Body of while stmt was not a stmtlist, on line: " + ctx.stmts.getStart().getLine());
        result.Stmts = ((StmtListNode) stmts).Stmts;

        return result;
    }

    @Override
    public AstNode visitBiop(FNNParser.BiopContext ctx) {
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
        // result.Types.add(result.Right.Types.get(0) == FNNType.Int &&
        // result.Left.Types.get(0) == FNNType.Int ? FNNType.Int : FNNType.Float);
        Utils.ASSERT(result.Left.Type.equals(result.Right.Type), "Bi operation: " + ctx.OPERATOR().getText() + ", between mismatched types: " + result.Left.Type + " and " + result.Right.Type + ", on line: " + ctx.start.getLine());

        result.Operator = OpEnum.parseChar(ctx.OPERATOR().getText().charAt(0));
        result.Type = result.Left.Type;

        return result;
    }

    @Override
    public AstNode visitUnop(FNNParser.UnopContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        UnOperatorNode result = new UnOperatorNode();

        var op = this.visit(ctx.op);
        Utils.ASSERT(op instanceof ExprNode, "Left operand of bi-operator was not an expression: " + ctx.op.getStart() + " to " + ctx.op.getStop());
        result.Operand = (ExprNode) op;
        Utils.ASSERT(result.Operand.Type instanceof BaseType, "Unary operations can only be used on single value expressions");
        result.Type = Utils.TRY_UNWRAP(result.Operand.Type);

        result.Operator = OpEnum.parseChar(ctx.OPERATOR().getText().charAt(0));

        return result;
    }

    @Override
    public ExprNode visitParens(FNNParser.ParensContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = this.visit(ctx.expr_in_parens);
        Utils.ASSERT(result instanceof ExprNode, "Contents of parens was not an expression: " + ctx.expr_in_parens.getStart() + " to " + ctx.expr_in_parens.getStop());

        return (ExprNode) result; // this being hard-doed kinda sucks
    }

    @Override
    public AssignNode visitAssign(FNNParser.AssignContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new AssignNode();
        var value = this.visit(ctx.expr_in_assign);
        Utils.ASSERT(value instanceof ExprNode, "Right side of assignment was not an expression: " + ctx.expr_in_assign.getStart() + " to " + ctx.expr_in_assign.getStop());
        result.Value = (ExprNode) value;
        if (ctx.ID().size() > 1) {
            Utils.ASSERT(result.Value.Type instanceof TupleType, "Trying to match non tuple type, " + result.Value.Type + ", to multiple names, on line: " + ctx.start.getLine());
            var tuple = (TupleType) result.Value.Type;
            Utils.ASSERT((tuple.Types.size() <= ctx.ID().size()), "Match failure in assign, mismatched amount");
            Queue<FNNType> right = new LinkedList<>(tuple.Types);
            Queue<FNNType> left = new LinkedList<>();
            if (ctx.ID().size() != tuple.Types.size()) {
                boolean did_we_unpack_tuples = false; // TODO: shouldn't we use this?
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
                        Utils.ASSERT(did_we_unpack_tuples, "Number of variables do not match number of elements in tuple" + ctx.getStart() + " : " + ctx.getStop());
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
            }
            while (right.size() > 0) {
                var name = ctx.ID(i++).getText();
                var type = right.remove();
                result.Types.add(type);
                result.Names.add(name);
                Scopes.peek().put(name, type);
            }
        }

        else {
            var name = ctx.ID(0).getText();
            result.Types.add(result.Value.Type);
            result.Names.add(name);
            Scopes.peek().put(name, result.Value.Type);
        }
        System.out.println("---ASSIGN---");
        for (int i = 0; i < result.Names.size(); i++) {
            System.out.println("ASSIGN: " + result.Names.get(i) + " : " + result.Types.get(i));
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
        type.Arg = params;
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

    // @Override
    // public LayerNode visitLayerlit(FNNParser.LayerlitContext ctx) {
    // System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

    // LayerNode result = new LayerNode();
    // result.Type = new BaseType(TypeEnum.Layer);
    // var inputsize = this.visit(ctx.input_size);
    // Utils.ASSERT(inputsize instanceof ExprNode, "Input size of layer must be an expression: " + ctx.input_size.getStart() + " to " + ctx.input_size.getStop());
    // result.InputSize = (ExprNode) inputsize;
    // Utils.ASSERT(result.InputSize.Type instanceof BaseType, "Input size of layer must be a single value expression: " + ctx.input_size.getStart() + " to " + ctx.input_size.getStop());
    // Utils.ASSERT(((BaseType) result.InputSize.Type).Type == TypeEnum.Int, "Input size of layer must be an integer: " + ctx.input_size.getStart() + " to " + ctx.input_size.getStop());

    // var outputsize = this.visit(ctx.output_size);
    // Utils.ASSERT(outputsize instanceof ExprNode, "Output size of layer must be an expression: " + ctx.output_size.getStart() + " to " + ctx.output_size.getStop());
    // result.OutputSize = (ExprNode) outputsize;
    // Utils.ASSERT(result.OutputSize.Type instanceof BaseType, "Output size of layer must be a single value expression: " + ctx.output_size.getStart() + " to " + ctx.output_size.getStop());
    // Utils.ASSERT(((BaseType) result.OutputSize.Type).Type == TypeEnum.Int, "Output size of layer must be an integer: " + ctx.output_size.getStart() + " to " + ctx.output_size.getStop());

    // result.ActivationFunction = ctx.activation_function.getText();
    // return result;
    // }

    @Override
    public ModelNode visitModellit(FNNParser.ModellitContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

        var result = new ModelNode();
        result.Type = new BaseType(TypeEnum.Model);

        // TODO: make constructors to avoid this
        var float_tuple = new TupleType();
        float_tuple.Types.add(new BaseType(TypeEnum.Float));

        var activation_type = new FuncType(); // Define (FLT) -> (FLT), to compare equality with
        activation_type.Arg = float_tuple;
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
            Utils.ASSERT(size_expr.Type.equals(new BaseType(TypeEnum.Int)), "Model sizes must be ints, on line: " + ctx.start.getLine());
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
    public TrainNode visitTrain_stmt(FNNParser.Train_stmtContext ctx) {
        System.out.println("Enter: " + Thread.currentThread().getStackTrace()[1].getMethodName());

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

        // eval the model, since it's a variable name

        EvalNode model = null;
        var name = ctx.model.getText();
        for (int i = Scopes.size() - 1; i >= 0; i--) {
            System.out.println("scope: " + i + "/" + (Scopes.size() - 1));
            var scope = Scopes.get(i);
            var type = scope.get(name);
            if (type != null) {
                System.out.println("EVAL: " + name + " : " + type);
                model = new EvalNode();
                model.Type = Utils.TRY_UNWRAP(type);
                model.Name = name;
            }
        }
        Utils.ASSERT(model != null, "Could not evaluate variable " + name + " on line: " + ctx.getStart().getLine());
        result.Model = model;
        Utils.ASSERT(result.Model.Type instanceof BaseType, "Model in training must be a single value expression, on line: " + ctx.model.getLine());
        Utils.ASSERT(((BaseType) result.Model.Type).Type == TypeEnum.Model, "Model in training must be an model, on line: " + ctx.model.getLine());

        var inputData = this.visit(ctx.input);
        Utils.ASSERT(inputData instanceof ExprNode, "Input Data in train must be an expression, on line: " + ctx.input.getStart().getLine());
        result.Input = (ExprNode) inputData;
        Utils.ASSERT(result.Input.Type instanceof ArrType, "Input Data in train must be an array");
        // TODO: more type checking, I can't be assed

        var expectedOutput = this.visit(ctx.expected);
        Utils.ASSERT(expectedOutput instanceof ExprNode, "Expected Output in train must be an expression: " + ctx.expected.getStart() + " to " + ctx.expected.getStop());
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

        Utils.ASSERT(arg_type.equals(func_type.Arg), "Types in function call doesn't match, expected: " + func_type.Arg + ", got: " + arg_type);
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
        TupleType arg_tuple = new TupleType();
        for (var arg_type : ((TypeListNode) args).Types) {
            arg_tuple.Types.add(arg_type.Type);
        }
        type.Arg = arg_tuple;
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
        var type = new ArrType(((TypeNode) arrtypenode).Type, 69); // TODO: something about the size??
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
}