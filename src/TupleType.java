import java.util.*;

public class TupleType extends FNNType {
    public List<FNNType> Types = new Vector<>();

    @Override
    public String toString() {
        String result = "(";
        if (Types.size() > 0) {
            result += Types.get(0);
            for (int i = 1; i < Types.size(); i++) {
                result += " ";
                result += Types.get(i);
            }
        }
        result += ")";
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof FNNType))
            return false;

        var t = Utils.TRY_UNWRAP(this);
        var o = Utils.TRY_UNWRAP((FNNType) other);

        if (!(t instanceof TupleType))
            if (!(o instanceof TupleType))
                return t.equals(o);
            else
                return false;

        // Else, t is a tuple
        if (!(o instanceof TupleType))
            return false; // t is a tuple, but o isn't

        // Else, both are tuples
        TupleType tuple_t = (TupleType) t;
        TupleType tuple_o = (TupleType) o;

        if (tuple_t.Types.size() != tuple_o.Types.size())
            return false;

        for (int i = 0; i < tuple_t.Types.size(); i++)
            if (!(tuple_t.Types.get(i).equals(tuple_o.Types.get(i))))
                return false;

        return true;
    }
}