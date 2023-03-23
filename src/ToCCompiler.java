import java.lang.ProcessBuilder.Redirect.Type;

import org.antlr.v4.misc.Graph.Node;

public class ToCCompiler {
    // public String Compile(AstNode Node) {
    // System.err.println("Unexpected nodes: " + Node.getClass() + " while trying to
    // compile to C, exiting...");
    // System.exit(-1);
    // return null;
    // }

    public String Compile(ProgramNode Node) {
        String result = "#include <stdio.h>\n";
        result += "int main(int argc, char* argv[]){";
        for (ExprNode exprNode : Node.Exprs) {
            result += "printf(\"" + (exprNode.Type == TypeEnum.Int ? "%d" : "%f") + "\\n\",";
            result += Compile(exprNode);
            result += ")";
            result += ";";
        }
        result += "return 0;}";
        return result;
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
        else {
            System.err.println("Unexpected nodes: " + Node.getClass() + " while trying to compile to C, exiting...");
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

}
