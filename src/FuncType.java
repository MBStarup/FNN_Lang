import java.util.*;

public class FuncType extends FNNType {
    List<FNNType> Args = new Vector<>();
    TupleType Ret = new TupleType();

    @Override
    public String toString() {
        var arg_tuple = new TupleType();
        arg_tuple.Types = Args;
        String result = "(";
        result += arg_tuple.toString();
        result += ") -> (";
        result += Ret.toString();
        result += ")";
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TupleType)
            return other.equals(this);

        if (other instanceof FuncType) {
            var f_other = (FuncType) other;
            if (((FuncType) other).Ret.equals(this.Ret)) {
                for (int i = 0; i < Args.size(); i++) {
                    if (!(Args.get(i).equals(f_other.Args.get(i))))
                        return false;
                }
                return true;
            }
            return false;
        }

        return false;
    }
}