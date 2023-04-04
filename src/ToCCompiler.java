import java.lang.ProcessBuilder.Redirect.Type;

import javax.naming.spi.DirStateFactory.Result;

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
        result += "#define TRAINING_DATA_AMOUNT 12\n";
        result += "int main(int argc, char* argv[]){";
        result += "    double *training_data_input[TRAINING_DATA_AMOUNT];\ndouble *training_expected_output[TRAINING_DATA_AMOUNT];\nload_csv(training_expected_output, OUTPUT_SIZE, training_data_input, INPUT_SIZE, TRAINING_DATA_AMOUNT, \"../c_ml/mnist_train.csv\", 5);\n";
        result += "    activation sigmoid_activationfunction;\nsigmoid_activationfunction.function = sigmoid;\nsigmoid_activationfunction.derivative = derivative_of_sigmoid;\n";
        for (StmtNode exprNode : Node.Stmts) {
            result += Compile(exprNode);
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
            return "float";
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
        } else {
            Utils.ERREXIT("Unknown type type, maybe update switch case");
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
        result += Node.Name;
        result += ")";
        return result;
    }

    public String Compile(AssignNode Node) {
        String result = "";

        if (Node.Names.size() == 1) {
            if (Node.Types.get(0) instanceof BaseType) {
                result += TypeToString((BaseType) Node.Types.get(0));
                result += " ";
                result += Node.Names.get(0);
                result += " = ";
                result += this.Compile(Node.Value);
                result += ";\n";
                return result;
            } else if (Node.Types.get(0) instanceof FuncType) {
                result += "void (*"; // all funcitons return void
                result += Node.Names.get(0);
                result += ")(";
                result += TypeToString(((FuncType) Node.Types.get(0)).Ret);
                result += "*,"; // TODO: THIS IS WRONFGE
                result += TypeToString(((FuncType) Node.Types.get(0)).Arg);
                result += ") = ";
                result += this.Compile(Node.Value);
                result += ";\n";
                System.out.println("FUNC TYPE OUTPUT: " + result);
                return result;
            } else {
                Utils.ERREXIT("IDK BIG ERROR");
                return null; // unreachable
            }
        } else {
            Utils.ERREXIT("Tuples not implemented yet");
            return null; // unreachable
            // TUPLES POOOOOOOOG
        }
    }

    public String Compile(TrainNode Node) {
        String result = "(train_model(";
        result += this.Compile(Node.Model);
        result += ",";
        result += this.Compile(Node.Epochs);
        result += ",";
        result += this.Compile(Node.BatchSize);
        result += ", training_data_input, training_expected_output, TRAINING_DATA_AMOUNT";
        result += "))";
        return result;
    }

}
