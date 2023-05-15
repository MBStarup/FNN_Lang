import java.util.Vector;

import org.antlr.v4.runtime.misc.Pair;

public class FunctionDeclarationNode extends StmtNode {
    public String Name;
    public TypeEnum ReturnType;
    public Vector<Pair<TypeEnum, String>> Params;
    public Vector<StmtNode> Body;
}
