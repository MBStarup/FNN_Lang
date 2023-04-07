import java.lang.ProcessBuilder.Redirect.Type;
import java.util.List;
import java.util.Vector;

import javax.naming.spi.DirStateFactory.Result;

import org.antlr.v4.codegen.model.decl.Decl;
import org.antlr.v4.misc.Graph.Node;

public class ToCCompiler {
    // public String Compile(AstNode Node) {
    // System.err.println("Unexpected nodes: " + Node.getClass() + " while trying to
    // compile to C, exiting...");
    // System.exit(-1);
    // return null;
    // }

    public String Compile(ProgramNode Node) {
        String result = "#include <stdio.h>\n#include \"c_ml_base.c\"\n";
        // result += "#define TRAINING_DATA_AMOUNT 12\n";
        result += "int main(int argc, char* argv[]){";
        // result += " double *training_data_input[TRAINING_DATA_AMOUNT];\ndouble *training_expected_output[TRAINING_DATA_AMOUNT];\nload_csv(training_expected_output, OUTPUT_SIZE, training_data_input, INPUT_SIZE, TRAINING_DATA_AMOUNT, \"../c_ml/mnist_train.csv\", 5);\n";
        result += " activation sigmoid_activationfunction;\nsigmoid_activationfunction.function = sigmoid;\nsigmoid_activationfunction.derivative = derivative_of_sigmoid;\n";
        for (StmtNode stmt : Node.Stmts) {
            result += Compile(stmt);
            result += ";";
        }
        result += "return 0;}";
        return result;
    }

    public String Compile(StmtNode Node) {
        if (Node instanceof ExprNode)
            return Compile((ExprNode) Node);
        else if (Node instanceof FunctionDeclarationNode)
            return Compile((FunctionDeclarationNode) Node);
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
        else if (Node instanceof LayerNode)
            return Compile((LayerNode) Node);
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

    public String Compile(FunctionDeclarationNode Node) {
        String result = Node.ReturnType + Node.Name + "(";
        for (var param : Node.Params) {
            result += "";

        }

        return result;
    }

    public static String TypeEnumToString(TypeEnum Enum) {
        switch (Enum) {
        case Int:
            return "int";
        case Float:
            return "double";
        case Layer:
            return "layer";
        case String:
            return "char *";
        case Model:
            return "model";
        default:
            System.err.println("Unexpected type (" + Enum + ") cannot be converted to c-type");
            System.exit(-1);
            // TODO: Make exit function.
        }

        return null;
    }

    public static String TypeToString(ArrType Type) {
        String result = "";
        result += TypeToString(Type.Type);
        result += " *";
        return result;
    }

    public static String TypeToString(FuncType Type) {
        String result = "";
        result += "void (*)("; // all funcitons return void
        var argsString = TypeToString(Type.Arg);
        argsString.replace(",", "*,");
        result += argsString;
        result += TypeToString(Type.Ret);
        result += ")";
        return result;
    }

    public static String TypeToString(TupleType Type) {
        String result = "";
        Utils.ASSERT(Type.Types.size() > 0, "Empty tuple"); // TODO: consider allowing empty tuples
        result += TypeToString(Type.Types.get(0));
        for (int i = 1; i < Type.Types.size(); i++) {
            result += ",";
            result += TypeToString(Type.Types.get(i));
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

    public String Compile(LayerNode Node) {
        String result = "(layer_new((";
        result += this.Compile(Node.InputSize);
        result += "),(";
        result += this.Compile(Node.OutputSize);
        result += "),";
        switch (Node.ActivationFunction) {
        case "sigmoid":
            result += "sigmoid_activationfunction";
            break;
        default:
            System.err.println("Unknown activation function: " + Node.ActivationFunction);
            System.exit(-1);
            break;
        }
        result += "))";
        return result;
    }

    public String Compile(ModelNode Node) {
        String result = "(model_new(";
        if (Node.Layers.size() < 1) {
            System.err.println("A model must have at least one layer");
            System.exit(-1);
        }

        result += Node.Layers.size();
        result += ",";

        result += this.Compile(Node.Layers.get(0));

        for (var layer : Node.Layers.subList(1, Node.Layers.size())) {
            result += ",";
            result += this.Compile(layer);
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

    public String Compile(AssignNode Node) {
        String result = "";
        if (Node.Names.size() == 1) {
            result += Declare(Node.Names.get(0), Node.Types.get(0));
            result += " = ";
            result += this.Compile(Node.Value);
            result += ";\n";
        } else {
            for (int i = 0; i < Node.Names.size(); i++) {
                result += Declare(Node.Names.get(i), Node.Types.get(i));
                result += ";";
            }
            result += "{";
            result += "void **TEMP = ";
            result += Compile(Node.Value);
            result += ";";
            for (int i = 0; i < Node.Names.size(); i++) {
                result += Node.Names.get(i);
                result += " = (*((";
                result += TypeToString(Node.Types.get(i));
                result += "*)(TEMP[";
                result += i;
                result += "])));";
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
        // TODO: START HERE!, make the data paramerters of the TrainNode
        result += ", training_data_input, training_expected_output, TRAINING_DATA_AMOUNT";
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
        result += ";";
        return result;
    }

    public String Compile(CallNode Node) {
        String result = "";
        List<FNNType> ret_types;

        if (Node.Type instanceof TupleType) {
            ret_types = ((TupleType) Node.Type).Types;
        } else {
            ret_types = new Vector<FNNType>();
            ret_types.add(Node.Type);
        }
        result += "({";
        for (int i = 0; i < ret_types.size(); i++) {
            result += Declare("TEMP" + i, ret_types.get(i));
            result += ";";
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
        for (int i = 0; i < Node.Args.size(); i++) {
            result += ",";
            result += Compile(Node.Args.get(i));
        }
        result += ");";
        if (ret_types.size() == 1) {
            result += "TEMP0;";
        }
        // TODO: generalize this with the same exact hard coded shit in TupleNode, we don't even make tuple nodes...
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

    public String Declare(String name, FNNType type) {
        System.out.println("Declare: " + name + " : " + type);
        String result = "";
        if (type instanceof BaseType) {
            result += TypeToString((BaseType) type);
            result += " ";
            result += name;
        } else if (type instanceof FuncType) {
            result += "void (*"; // all funcitons return void
            result += name;
            result += ")(";
            result += TypeToString(((FuncType) type).Ret);
            result += "*,"; // TODO: THIS IS WRONG
            result += TypeToString(((FuncType) type).Arg);
            result += ")";
        } else if (type instanceof ArrType) {
            result += TypeToString((ArrType) type);
            result += name;
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
        var type = (TupleType) Node.Type;

        for (int i = 0; i < type.Types.size(); i++) {
            result += Declare("T" + i, type.Types.get(i));
            result += ";";
        }

        for (int i = 0; i < type.Types.size(); i++) {
            result += "T" + i + " = " + Compile(Node.Exprs.get(i)) + ";";
        }

        result += "(void*[]){";
        for (int i = 0; i < type.Types.size(); i++) {
            result += "&T" + i + ",";
        }
        result += "};";

        result += "})";
        return result;
    }

}
