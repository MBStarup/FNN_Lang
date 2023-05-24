import java.util.*;

public class ToCCompiler {
    public Stack<Map<String, FNNType>> Scopes;
    private int funcNum = 0;
    private int indexNum = 0;

    public ToCCompiler() {
        Scopes = new Stack<>();
    }

    public String Compile(ProgramNode Node) {
        String result = "#include <math.h>\n#include <stdio.h>\n#include <time.h>\n#include \"c_ml_base.c\"\n";
        Scopes.push(new HashMap<>());
        result += "int main(int argc, char* argv[]){";
        result += "srand(time(NULL));";
        result += this.Compile(Node.Stmts);
        result += CleanUp(Scopes.pop());
        result += "return 0;}";
        return result;
    }

    public String Compile(List<StmtNode> Stmts) {
        String result = "";
        for (StmtNode stmt : Stmts) {
            result += Compile(stmt) + ";";
        }
        return result;
    }

    public String Compile(StmtNode Node) {
        if (Node instanceof ExprNode)
            return Compile((ExprNode) Node);
        else if (Node instanceof AssignNode)
            return Compile((AssignNode) Node);
        else if (Node instanceof TrainNode)
            return Compile((TrainNode) Node);
        else if (Node instanceof WhileNode)
            return Compile((WhileNode) Node);
        else if (Node instanceof ExternNode)
            return Compile((ExternNode) Node);
        else {
            Utils.ERREXIT("Unexpected StmtNode: " + Node.getClass() + " while trying to compile to C (you prolly need to add it to the switch case lmao), exiting...");
            return null; // unreachable
        }
    }

    public String Compile(ExprNode Node) {
        if (Node instanceof BiOperatorNode)
            return Compile((BiOperatorNode) Node);
        else if (Node instanceof UnOperatorNode)
            return Compile((UnOperatorNode) Node);
        else if (Node instanceof FloatNode)
            return Compile((FloatNode) Node);
        else if (Node instanceof IntNode)
            return Compile((IntNode) Node);
        else if (Node instanceof NNNode)
            return Compile((NNNode) Node);
        else if (Node instanceof EvalNode)
            return Compile((EvalNode) Node);
        else if (Node instanceof StringNode)
            return Compile((StringNode) Node);
        else if (Node instanceof CallNode)
            return Compile((CallNode) Node);
        else if (Node instanceof TupleNode)
            return Compile((TupleNode) Node);
        else if (Node instanceof ArrNode)
            return Compile((ArrNode) Node);
        else if (Node instanceof ArrAccessNode)
            return Compile((ArrAccessNode) Node);
        else if (Node instanceof FuncNode)
            return Compile((FuncNode) Node);
        else if (Node instanceof TestNode)
            return Compile((TestNode) Node);
        else {
            Utils.ERREXIT("Unexpected ExprNode: " + Node.getClass() + " while trying to compile to C (you prolly need to add it to the switch case lmao), exiting...");
            return null; // unreachable
        }
    }

    public String Compile(WhileNode Node) {
        String result = "while(" + this.Compile(Node.Predicate) + "){" + this.Compile(Node.Stmts) + "}";
        return result;
    }

    public String Compile(BiOperatorNode Node) {
        String result = "(";
        if (Node.Operator == OpEnum.Power) {
            result += "pow(" + Compile(Node.Left) + "," + Compile(Node.Right) + ")";
        } else {
            result += Compile(Node.Left);
            switch (Node.Operator) {
            case Plus:
                result += " + ";
                break;
            case Minus:
                result += " - ";
                break;
            case Multiply:
                result += " * ";
                break;
            case Divide:
                result += " / ";
                break;
            case LessThan:
                result += " < ";
                break;
            case GreaterThan:
                result += " > ";
                break;
            case Equals:
                result += " == ";
                break;
            default:
                Utils.ERREXIT("Unknown binary-operator: " + Node.Operator + " exiting..."); // Might be redundant if we check correctly in the visitor
                break; // unreachable
            }
            result += Compile(Node.Right);
        }
        result += ")";
        return result;
    }

    public String Compile(UnOperatorNode Node) {
        String result = "(";
        switch (Node.Operator) {
        case Plus:
            result += " + ";
            break;
        case Minus:
            result += " - ";
            break;
        default:
            Utils.ERREXIT("Unknown Unary-operator: " + Node.Operator + " exiting..."); // Might be redundant if we check correctly in the visitor
            return null; // unreachable
        }
        result += Compile(Node.Operand);
        result += ")";
        return result;
    }

    public static String TypeEnumToString(TypeEnum Enum) {
        switch (Enum) {
        case Int:
            return "int PLACEHOLDER";
        case Float:
            return "double PLACEHOLDER";
        case String:
            return "char *PLACEHOLDER";
        case NN:
            return "model_T PLACEHOLDER";
        default:
            Utils.ERREXIT("Unexpected type (" + Enum + ") cannot be converted to c-type");
            return null; // unreachable
        }
    }

    public static String TypeToString(FNNType Type) {
        if (Type instanceof BaseType) {
            return TypeToString((BaseType) Type);
        } else if (Type instanceof FuncType) {
            return TypeToString((FuncType) Type);
        } else if (Type instanceof TupleType) {
            return TypeToString((TupleType) Type);
        } else if (Type instanceof ArrType) {
            return TypeToString((ArrType) Type);
        } else {
            Utils.ERREXIT("Unknown type " + Type + ", maybe update switch case");
            return null; // unreachable
        }
    }

    public static String TypeToString(ArrType Type) {
        return TypeToString(Type.Type).replaceAll("PLACEHOLDER", "*PLACEHOLDER");
    }

    public static String TypeToString(FuncType Type) {
        var rets = Utils.TRY_UNWRAP(Type.Ret);
        String result = TypeToString(rets).replaceAll("PLACEHOLDER", "") + " (*PLACEHOLDER)(";
        var args = Type.Args;
        if (args.size() > 0) {
            result += TypeToString(args.get(0)).replaceAll("PLACEHOLDER", "");
            for (int i = 1; i < args.size(); i++) {
                result += ", " + TypeToString(args.get(i)).replaceAll("PLACEHOLDER", "");
            }
        }
        result += ")";
        return result;
    }

    public static String TypeToString(TupleType Type) {
        return "char *PLACEHOLDER";
    }

    public static String TypeToString(BaseType Type) {
        return TypeEnumToString(Type.Type);
    }

    public String Compile(FloatNode Node) {
        String result = "(";
        result += Node.Value;
        result += ")";
        return result;
    }

    public String Compile(IntNode Node) {
        String result = "(";
        result += Node.Value;
        result += ")";
        return result;
    }

    public String Compile(StringNode Node) {
        String result = "(\"";
        result += Node.Value;
        result += "\")";
        return result;
    }

    public String Compile(NNNode Node) {
        String result = "(model_new(";
        if (Node.LayerSizes.size() < 1) {
            Utils.ERREXIT("A NN must have at least one layer");
        }

        result += "(";
        result += Node.LayerSizes.size();
        result += " -1)"; // n numbers = n-1 layers as they are paired up, ie. <100 50 10> becomes 2 layers: (100 -> 50) (50 -> 10)

        for (int i = 1; i < Node.LayerSizes.size(); i++) {
            result += ",";
            result += "(layer_new(";
            result += this.Compile(Node.LayerSizes.get(i - 1));
            result += ",";
            result += this.Compile(Node.LayerSizes.get(i));
            result += ",";
            result += this.Compile(Node.Activation);
            result += ",";
            result += this.Compile(Node.Derivative);
            result += "))";
        }
        result += "))";
        return result;
    }

    public String Compile(EvalNode Node) {
        String result = "(";
        if (Node.Type instanceof FuncType) {
            result += "*" + Node.Name;
        } else {
            result += Node.Name;
        }
        result += ")";
        return result;
    }

    private Boolean isDeclared(String name) {
        for (var scope : Scopes) {
            var type = scope.get(name);
            if (type != null) {
                return true;
            }
        }
        return false;
    }

    public String Compile(AssignNode Node) {
        String result = "";

        if (Node.Names.size() == 1) {
            if (!(isDeclared(Node.Names.get(0)))) { // not declared
                Scopes.peek().put(Node.Names.get(0), Node.Types.get(0));
                result += Declare(Node.Names.get(0), Node.Types.get(0));
            } else {
                result += Node.Names.get(0);
            }
            result += " = " + this.Compile(Node.Value) + ";\n";
        } else {
            for (int i = 0; i < Node.Names.size(); i++) {
                if (!(isDeclared(Node.Names.get(i)))) { // not declared
                    Scopes.peek().put(Node.Names.get(i), Node.Types.get(i));
                    result += Declare(Node.Names.get(i), Node.Types.get(i));
                    result += ";";
                }
            }
            result += "{";
            var tuple_name = "T_TEMP";
            var type = new TupleType();
            type.Types = Node.Types;
            result += Declare(tuple_name + "_O", type) + " = " + Compile(Node.Value) + ";";
            result += Declare(tuple_name, type) + " = " + Copy(tuple_name + "_O", type) + ";";
            for (int i = 0; i < Node.Names.size(); i++) {
                result += Node.Names.get(i) + " = " + IndexTuple(tuple_name, Node.Types, i) + ";";
            }
            result += "ass_free(" + tuple_name + ");"; // cleanup the tuple itself
            result += "}";
        }

        return result;
    }

    public String Compile(ArrNode Node) {
        String result = "({";
        result += Declare("ARR", Node.Type) + " = ass_malloc_fnn_arr(sizeof(" + TypeToString(((ArrType) Node.Type).Type).replaceAll("PLACEHOLDER", "") + "), " + Node.Count + ");";
        for (int i = 0; i < Node.Exprs.size(); i++) {
            result += "ARR[" + i + "] = " + this.Compile(Node.Exprs.get(i)) + ";";
        }
        result += "ARR;";
        result += "})";
        return result;
    }

    public String Compile(TrainNode Node) {
        String result = "(train_model(";
        result += this.Compile(Node.NN) + ",";
        result += this.Compile(Node.Rate) + ",";
        result += this.Compile(Node.Epochs) + ",";
        result += this.Compile(Node.Input) + ",";
        result += this.Compile(Node.Expected);
        result += "))";
        return result;
    }

    public String Compile(TestNode Node) {
        String result = "(test_model(";
        result += this.Compile(Node.NN) + ",";
        result += this.Compile(Node.In) + ",";
        result += this.Compile(Node.Out);
        result += "))";
        return result;
    }

    public String Compile(ExternNode Node) {
        String result = Declare(Node.Name, Node.Type);
        result += " = ";
        if (Node.Type instanceof FuncType) {
            result += "&" + "E_" + Node.Name;
        } else {
            result += "E_" + Node.Name;
        }
        return result;
    }

    public String Compile(CallNode Node) {
        String result = "(";
        result += Compile(Node.Function);
        result += "(";
        if (Node.Args.size() > 0) {
            result += Compile(Node.Args.get(0));
            for (int i = 1; i < Node.Args.size(); i++) {
                result += ",";
                result += Compile(Node.Args.get(i));
            }
        }
        result += "))";
        return result;
    }

    private String Declare(String name, FNNType type) {
        return TypeToString(type).replaceAll("PLACEHOLDER", name);
    }

    public String Compile(TupleNode Node) {
        var types = Utils.AS_LIST(Node.Type);

        if (types.size() == 1) {
            return this.Compile(Node.Exprs.get(0)); // single tuples are just ignored outside the type system
        }

        String result = "({";
        result += "int T_SIZE = sizeof(" + TypeToString(types.get(0)).replaceAll("PLACEHOLDER", "") + ")";
        for (int i = 1; i < types.size(); i++) {
            result += " + sizeof(" + TypeToString(types.get(i)).replaceAll("PLACEHOLDER", "") + ")";
        }
        result += ";";
        result += "char* TUPLE = ass_malloc(T_SIZE);";

        for (int i = 0; i < types.size(); i++) {
            result += IndexTuple("TUPLE", types, i) + " = " + Copy(this.Compile(Node.Exprs.get(i)), types.get(i)) + ";";
        }

        result += "TUPLE;";

        result += "})";
        return result;
    }

    public String Compile(ArrAccessNode Node) {
        String result = "(";
        result += this.Compile(Node.Array);
        result += ")";
        result += "[(";
        result += this.Compile(Node.Index);
        result += ")]";
        return result;
    }

    public String Compile(FuncNode Node) {
        int func_num = this.funcNum++;
        Utils.ASSERT(Node.Type instanceof FuncType, "Trying to declare function with non function type, compiler shit the bed");
        var type = (FuncType) Node.Type;

        var ret = Utils.TRY_UNWRAP(type.Ret);
        String result = "({";
        result += TypeToString(ret).replaceAll("PLACEHOLDER", "") + "FUNC" + func_num + "(";

        Scopes.push(new HashMap<>()); // New scope for functions
        if (type.Args.size() > 0) {
            var arg_name = Node.ParamNames.get(0);
            var arg_type = Utils.TRY_UNWRAP(type.Args.get(0));
            if (HeapAlloced(arg_type)) {
                result += Declare(arg_name + "_O", arg_type);
            } else {
                result += Declare(arg_name, arg_type);
            }
            Scopes.peek().put(arg_name, arg_type);

            for (int i = 1; i < type.Args.size(); i++) {
                arg_name = Node.ParamNames.get(i);
                arg_type = Utils.TRY_UNWRAP(type.Args.get(i));
                result += ",";
                result += Declare(arg_name, arg_type);
                Scopes.peek().put(arg_name, arg_type);
            }
        }
        result += ")";
        result += "{";

        for (int i = 0; i < type.Args.size(); i++) { // for all the arrays and nns and tuples, we want to copy them to simulate pass-by-value
            var arg_name = Node.ParamNames.get(0);
            var arg_type = Utils.TRY_UNWRAP(type.Args.get(0));
            if (HeapAlloced(arg_type)) {
                result += Declare(arg_name, arg_type) + " = " + Copy(arg_name + "_O", arg_type) + ";";
            }
        }

        result += this.Compile(Node.Stmts);

        result += Declare("RET_VAL", ret) + " = " + this.Compile(Node.Result) + ";";
        Scopes.peek().put("RET_VAL", ret); // Put it in the scope for cleanup

        result += Declare("RET_VAL_COPY", ret) + " = " + Copy("RET_VAL", ret) + ";"; // copy it, in case it's heap allocated so we don't return a bad pointer
        result += "return RET_VAL_COPY;";

        result += CleanUp(Scopes.pop()); // Add mem cleanup code to the end of the function for all heap allocated memory in the function

        result += "};";
        result += "&FUNC" + func_num;
        result += ";})";

        return result;

    }

    private String Copy(String name, FNNType Type) {
        var type = Utils.TRY_UNWRAP(Type);
        if (type instanceof ArrType) {
            ++indexNum;
            var index = "INDEX" + indexNum;
            var res = "({";
            res += Declare("COPY", type) + " = ass_malloc_fnn_arr(sizeof(" + TypeToString(((ArrType) type).Type).replaceAll("PLACEHOLDER", "") + "), " + LengthOfArr(name) + ");";
            res += "for (int " + index + " = 0; " + index + " < " + LengthOfArr(name) + "; " + index + "++){";
            res += "COPY[" + index + "] = " + Copy(name + "[" + index + "]", ((ArrType) type).Type) + ";";
            res += "}";
            res += "COPY;";
            res += "})";
            --indexNum;
            return res;
        }
        if (type instanceof TupleType) {
            var res = "({";
            res += Declare("COPY", type) + " = ass_malloc(" + SizeOfTuple((TupleType) type) + ");";
            for (int i = 0; i < ((TupleType) type).Types.size(); i++) {
                res += IndexTuple("COPY", (TupleType) type, i) + " = " + Copy(IndexTuple(name, (TupleType) type, i), ((TupleType) type).Types.get(i)) + ";";
            }
            res += "COPY;";
            res += "})";
            return res;
        }
        if (type.equals(new BaseType(TypeEnum.NN))) {
            var res = "({";
            res += "model_copy(" + name + ");";
            res += "})";
            return res;
        }
        return "(" + name + ")"; // not heap allocated data
    }

    public String CleanUp(Map<String, FNNType> Scope) {
        String result = "";
        for (var name : Scope.keySet()) {
            result += CleanUpSingle(name, Scope.get(name));
        }
        return result;
    }

    public String CleanUpSingle(String Name, FNNType Type) {
        if (Type instanceof ArrType) {
            var index = "INDEX" + indexNum++;
            String result = "for (int " + index + " = 0; " + index + " < " + LengthOfArr(Name) + "; " + index + "++){" + CleanUpSingle(Name + "[" + index + "]", ((ArrType) Type).Type) + "};";
            result += "(ass_free_fnn_arr(" + Name + "));";
            --indexNum;
            return result;

        } else if (Type instanceof TupleType) {
            String result = "";
            for (int i = 0; i < ((TupleType) Type).Types.size(); i++) {
                result += CleanUpSingle(IndexTuple(Name, (TupleType) Type, i), ((TupleType) Type).Types.get(i)) + ";";
            }
            result += "ass_free(" + Name + ");";
            return result;
        } else if (Type instanceof BaseType && ((BaseType) Type).Type == TypeEnum.NN) {
            return "(model_del(" + Name + "));";
        }
        return ""; // rest of Basetypes and functypes are fully stack allocated, so require no cleanup
    }

    public String IndexTuple(String Name, TupleType Type, int i) {
        return IndexTuple(Name, Type.Types, i);
    }

    public String IndexTuple(String Name, List<FNNType> Types, int index) {
        var res = "(*((" + TypeToString(Types.get(index)).replaceAll("PLACEHOLDER", "*") + ")(&(" + Name + "[0+"; // interprets the pointer as a pointer to whatever type we have
        for (int i = 0; i < index; i++) {
            res += "sizeof(" + TypeToString(Types.get(i)).replaceAll("PLACEHOLDER", "") + ")+";
        }
        res += "0]))))";
        return res;
    }

    public String LengthOfArr(String Name) {
        return "((int *)" + Name + ")[-1]";
    }

    public String SizeOfTuple(TupleType type) {
        String result = "0";

        for (var t : type.Types) {
            result += "+sizeof(" + TypeToString(t).replaceAll("PLACEHOLDER", "") + ")";
        }

        return result;
    }

    public boolean HeapAlloced(FNNType type) {
        var t = Utils.TRY_UNWRAP(type);
        if (t instanceof TupleType)
            return true;
        if (t instanceof ArrType)
            return true;
        if (t instanceof BaseType)
            return t.equals(new BaseType(TypeEnum.NN));
        return false;
    }
}