import java.lang.ProcessBuilder.Redirect.Type;
import java.util.*;

import javax.swing.text.Utilities;

import org.antlr.v4.codegen.model.chunk.ThisRulePropertyRef_parser;

public class ToCCompiler {
    // public String Compile(AstNode Node) {
    // System.err.println("Unexpected nodes: " + Node.getClass() + " while trying to
    // compile to C, exiting...");
    // System.exit(-1);
    // return null;
    // }

    public Stack<Map<String, Boolean>> Scopes;
    private int funcNum;

    public ToCCompiler() {
        Scopes = new Stack<>();
        Scopes.push(new HashMap<>());
    }

    public String Compile(ProgramNode Node) {
        String result = "#include <stdio.h>\n#include \"c_ml_base.c\"\n";
        // result += "#define TRAINING_DATA_AMOUNT 12\n";
        result += "int main(int argc, char* argv[]){";
        // result += " double *training_data_input[TRAINING_DATA_AMOUNT];\ndouble
        // *training_expected_output[TRAINING_DATA_AMOUNT];\nload_csv(training_expected_output,
        // OUTPUT_SIZE, training_data_input, INPUT_SIZE, TRAINING_DATA_AMOUNT,
        // \"../c_ml/mnist_train.csv\", 5);\n";
        // result += " activation
        // sigmoid_activationfunction;\nsigmoid_activationfunction.function =
        // sigmoid;\nsigmoid_activationfunction.derivative = derivative_of_sigmoid;\n";
        for (StmtNode stmt : Node.Stmts) {
            result += Compile(stmt);
            // result += ";";
        }
        result += "return 0;}";
        return result;
    }

    public String Compile(StmtNode Node) {
        if (Node instanceof ExprNode)
            return Compile((ExprNode) Node);
        else if (Node instanceof AssignNode)
            return Compile((AssignNode) Node);
        else if (Node instanceof TrainNode)
            return Compile((TrainNode) Node);
        else if (Node instanceof ExternNode)
            return Compile((ExternNode) Node);
        else {
            System.err.println("Unexpected StmtNode: " + Node.getClass() + " while trying to compile to C (you prolly need to add it to the switch case lmao), exiting...");
            System.exit(-1);
        }
        return null;
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
        else if (Node instanceof ModelNode)
            return Compile((ModelNode) Node);
        else if (Node instanceof EvalNode)
            return Compile((EvalNode) Node);
        else if (Node instanceof StringNode)
            return Compile((StringNode) Node);
        else if (Node instanceof CallNode)
            return Compile((CallNode) Node);
        else if (Node instanceof TupleNode)
            return Compile((TupleNode) Node);
        else if (Node instanceof ArrAccessNode)
            return Compile((ArrAccessNode) Node);
        else if (Node instanceof FuncNode)
            return Compile((FuncNode) Node);
        else {
            System.err.println("Unexpected ExprNode: " + Node.getClass() + " while trying to compile to C (you prolly need to add it to the switch case lmao), exiting...");
            System.exit(-1);
        }
        return null;
    }

    public String Compile(BiOperatorNode Node) {
        String result = "(";
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
        default:
            System.err.println("Unknown binary-operator: " + Node.Operator + " exiting...");
            System.exit(-1);
            break;
        }
        result += Compile(Node.Right);
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
            System.err.println("Unknown Unary-operator: " + Node.Operator + " exiting...");
            System.exit(-1);
            break;
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
        case Model:
            return "model_T PLACEHOLDER";
        default:
            Utils.ERREXIT("Unexpected type (" + Enum + ") cannot be converted to c-type");
        }

        return null;
    }

    public static String TypeToString(ArrType Type) {
        return TypeToString(Type.Type).replaceAll("PLACEHOLDER", "*PLACEHOLDER");
    }

    public static String TypeToString(FuncType Type) {
        String result = "";
        result += "void (*PLACEHOLDER)("; // all funcitons return void
        var rets = TypeToString(Type.Ret).replaceAll("PLACEHOLDER", "*"); // Return types are actually pointers to that
                                                                          // type
        if (rets.length() > 0) {
            rets += ",";
        }
        result += rets;
        result += TypeToString(Type.Arg).replaceAll("PLACEHOLDER", ""); // Argument types have no names
        result += ")";
        return result;
    }

    public static String TypeToString(TupleType Type) {
        String result = "";
        Utils.ASSERT(Type.Types.size() > 0, "Empty tuple. TODO: consider allowing empty tuples."); // TODO: consider
                                                                                                   // allowing empty
                                                                                                   // tuples
        result += TypeToString(Type.Types.get(0));
        for (int i = 1; i < Type.Types.size(); i++) {
            result += ",";
            result += TypeToString(Type.Types.get(i)); // TODO: something about PLACEHOLDER?
        }
        return result;
    }

    public static String TypeToString(BaseType Type) {
        return TypeEnumToString(Type.Type);
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

    public String Compile(ModelNode Node) {
        String result = "(model_new(";
        if (Node.LayerSizes.size() < 1) {
            Utils.ERREXIT("A model must have at least one layer");
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
                Scopes.peek().put(Node.Names.get(0), true);
                result += Declare(Node.Names.get(0), Node.Types.get(0));
            } else {
                result += Node.Names.get(0);
            }
            result += " = ";
            result += this.Compile(Node.Value);
            result += ";\n";
        } else {
            for (int i = 0; i < Node.Names.size(); i++) {
                if (!(isDeclared(Node.Names.get(i)))) { // not declared
                    Scopes.peek().put(Node.Names.get(i), true);
                    result += Declare(Node.Names.get(i), Node.Types.get(i));
                    result += ";";
                }
            }
            result += "{";
            result += "void **TEMP = " + Compile(Node.Value) + ";"; // at this point we have a void** TEMP_x, that represents a single depth tuple
            for (int i = 0; i < Node.Names.size(); i++) {
                result += Node.Names.get(i);
                if (Node.Types.get(i) instanceof TupleType) {
                    var flat = Utils.FLATTEN(Node.Types.get(i));
                    result += " = ({(void *[]){";
                    for (int j = 0; j < flat.Types.size(); j++) {
                        result += "TEMP[" + i + "],";
                        i++;
                    }
                    result += "};});";
                } else {
                    result += " = (*((";
                    result += TypeToString(Node.Types.get(i)).replaceAll("PLACEHOLDER", ""); // gets just the type, with no name
                    result += "*)(TEMP[";
                    result += i;
                    result += "])));";
                }
            }
            result += "}";
        }

        System.out.println("Assign: " + result);
        return result;
    }

    public String Compile(TrainNode Node) {
        String result = "(train_model(";
        result += this.Compile(Node.Model);
        result += ",";
        result += this.Compile(Node.Epochs);
        result += ",";
        result += this.Compile(Node.BatchSize);
        result += ",";
        result += this.Compile(Node.Input);
        result += ",";
        result += this.Compile(Node.Expected);
        result += "))";
        return result;
    }

    public String Compile(ExternNode Node) {
        String result = "";
        result += Declare(Node.Name, Node.Type);
        result += " = ";
        if (Node.Type instanceof FuncType) {
            result += "&" + "E_" + Node.Name;
        } else {
            result += "E_" + Node.Name;
        }
        return result;
    }

    public String Compile(CallNode Node) {
        String result = "";
        int t_count = 0;
        var ret_types = Utils.FLATTEN(Node.Type).Types;
        result += "({";
        for (int i = 0; i < Node.Args.size(); i++) {
            if (Node.Args.get(i).Type instanceof TupleType) {
                for (FNNType type : ((TupleType) Node.Args.get(i).Type).Types) {
                    Utils.ASSERT(!(type instanceof TupleType), "Multi depth tuple in c impl");
                    result += Declare("ARGT_" + t_count++, type) + ";";
                }
            }
        }

        t_count = 0;
        for (int i = 0; i < Node.Args.size(); i++) {
            if (Node.Args.get(i).Type instanceof TupleType) {
                result += "{";
                result += "void** T = " + Compile(Node.Args.get(i)) + ";";
                int j = 0;
                for (FNNType type : ((TupleType) Node.Args.get(i).Type).Types) {
                    Utils.ASSERT(!(type instanceof TupleType), "Multi depth tuple in c impl");
                    result += "ARGT_" + t_count++ + "= (*((" + TypeToString(type).replaceAll("PLACEHOLDER", "") + " *)(T[" + j++ + "])));";
                }
                result += "}";
            }
        }

        for (int i = 0; i < ret_types.size(); i++) {
            result += Declare("TEMP" + i, ret_types.get(i)) + ";";
        }

        result += Compile(Node.Function);
        result += "(";
        if (ret_types.size() > 0) {
            result += "&TEMP0";
            for (int i = 1; i < ret_types.size(); i++) {
                result += ",";
                result += "&TEMP" + i;
            }
        }

        t_count = 0;
        for (int i = 0; i < Node.Args.size(); i++) {
            result += ",";
            if (Node.Args.get(i).Type instanceof TupleType) {
                int size = ((TupleType) Node.Args.get(i).Type).Types.size();
                if (size > 0) {
                    result += "ARGT_" + t_count++;
                    for (int j = 1; j < size; j++) {
                        result += ", ARGT_" + t_count++;
                    }
                }
            } else {
                result += Compile(Node.Args.get(i));
            }
        }
        result += ");";
        if (ret_types.size() == 1) {
            result += "TEMP0";
        }
        // TODO: generalize this with the same exact hard coded shit in TupleNode...
        if (ret_types.size() > 1) {
            result += "(void*[]){&TEMP0";
            for (int i = 1; i < ret_types.size(); i++) {
                result += ",";
                result += "&TEMP" + i;
            }
            result += "}";
        }
        result += ";})";
        return result;
    }

    private String Declare(String name, FNNType type) {
        // System.out.println("Declare: " + name + " : " + type);
        String result = "";
        if (type instanceof BaseType) { // TODO: this is the same, look at it again
            result += TypeToString((BaseType) type).replaceAll("PLACEHOLDER", name);
        } else if (type instanceof FuncType) {
            result += TypeToString((FuncType) type).replaceAll("PLACEHOLDER", name);
        } else if (type instanceof ArrType) {
            result += TypeToString((ArrType) type).replaceAll("PLACEHOLDER", name);
        } else if (type instanceof TupleType) {
            result += "void **";
            result += name;
        } else {
            Utils.ERREXIT("IDK BIG ERROR");
            return null; // unreachable
        }
        return result;
    }

    public String Compile(TupleNode Node) {
        String result = "({";
        Utils.ASSERT(Node.Type instanceof TupleType, "Type of tuple node isn't a tuple type guh"); // TODO: maybe redundant tbh
        var real_type = (TupleType) Node.Type;
        var type = Utils.FLATTEN(real_type); // we implement all tuples flatly

        for (int i = 0; i < type.Types.size(); i++) {
            result += Declare("T" + i, type.Types.get(i)) + ";";
        }

        int tuple_num = 0;
        for (int i = 0; i < real_type.Types.size(); i++) {
            if (real_type.Types.get(i) instanceof TupleType) {
                result += "void** TEMP" + tuple_num + ";";
                result += "TEMP" + tuple_num + " = " + Compile(Node.Exprs.get(i)) + ";";
                var flat = Utils.FLATTEN(real_type.Types.get(i)); // since they're all flat, we assume the returned tuple is flat too
                for (int j = 0; j < flat.Types.size(); j++) {
                    result += "/*" + flat.Types.get(j) + "*/";
                    result += "T" + i + " = (*((" + TypeToString(flat.Types.get(j)).replaceAll("PLACEHOLDER", "*") + ")(TEMP" + tuple_num + "[" + j + "])));";
                    i++;
                }
            } else {
                result += "T" + i + " = " + Compile(Node.Exprs.get(i)) + ";";
            }
        }

        result += "(void*[]){";
        for (int i = 0; i < type.Types.size(); i++) {
            result += "&T" + i + ",";
        }
        result += "};";

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

    private String tupleParamDelcare(String name, FNNType type) {
        if (!(type instanceof TupleType)) {
            return Declare(name, type);
        }
        var t = (TupleType) type;
        String result = "";
        if (t.Types.size() > 0) {
            result += tupleParamDelcare(name + "_" + 0, t.Types.get(0));
            for (int i = 1; i < t.Types.size(); i++) {
                result += ",";
                result += tupleParamDelcare(name + "_" + i, t.Types.get(i));
            }
        }

        return result;
    }

    // probably the least general code I've ever written lmao
    private String tupleParamAddrNoTypes(String name, FNNType type) { // this is so fucking stupid, like, lmao, I can't even. I deeply apologize to anyone who ever reads this. I truly am sorry.
        if (!(type instanceof TupleType)) {
            return "&" + name;
        }
        var t = (TupleType) type;
        String result = "";
        if (t.Types.size() > 0) {
            result += tupleParamAddrNoTypes(name + "_" + 0, t.Types.get(0));
            for (int i = 1; i < t.Types.size(); i++) {
                result += ",";
                result += tupleParamAddrNoTypes(name + "_" + i, t.Types.get(i));
            }
        }

        return result;
    }

    public String Compile(FuncNode Node) {
        int func_num = this.funcNum++;
        Utils.ASSERT(Node.Type instanceof FuncType, "Trying to declare function with non function type, compiler shit the bed");
        var type = (FuncType) Node.Type;

        var rets = Utils.FLATTEN(type.Ret);
        String result = "({";
        result += "void ";
        result += "FUNC" + func_num;
        result += "(";
        if (rets.Types.size() > 0) {
            result += Declare("*RET" + 0, rets.Types.get(0));
            for (int i = 1; i < rets.Types.size(); i++) {
                result += ",";
                result += Declare("*RET" + i, rets.Types.get(i));
            }
        }

        Scopes.push(new HashMap<String, Boolean>()); // New scope for functions
        for (int i = 0; i < type.Arg.Types.size(); i++) {
            result += ",";
            result += tupleParamDelcare(Node.ParamNames.get(i), type.Arg.Types.get(i));
            Scopes.peek().put(Node.ParamNames.get(i), true);
        }
        result += ")";
        result += "{";

        for (int i = 0; i < type.Arg.Types.size(); i++) {
            if (type.Arg.Types.get(i) instanceof TupleType) {
                result += "void** " + Node.ParamNames.get(i) + " = ((void*[]){" + tupleParamAddrNoTypes(Node.ParamNames.get(i), type.Arg.Types.get(i)) + "});";
            }
        }

        for (var stmt : Node.Stmts) {
            result += this.Compile(stmt);
            result += ";";
        }
        result += "(*RET0)=";
        result += this.Compile(Node.Result);
        result += ";";
        Scopes.pop();
        result += "};";
        result += "&FUNC" + func_num;
        result += ";})";

        return result;

    }

}