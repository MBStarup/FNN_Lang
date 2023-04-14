import java.util.*;

public class Utils {
    static void ERREXIT(String msg) {
        System.err.println(msg);
        System.exit(-1);
    }

    static void ASSERT(Boolean predicate, String fail_msg) {
        if (!(predicate))
            ERREXIT("ERR: " + fail_msg);
    }

    static FNNType TRY_UNWRAP(FNNType type) {
        if (type == null) {
            return null;
        }
        if (!(type instanceof TupleType)) {
            return type;
        }
        if (((TupleType) type).Types.size() != 1) {
            return type;
        }
        return TRY_UNWRAP(((TupleType) type).Types.get(0));
    }

    static TupleType FLATTEN(FNNType Type) {
        var result = new TupleType();
        if (Type instanceof TupleType) {
            var tuple = (TupleType) Type;
            Queue<FNNType> right = new LinkedList<>(tuple.Types);
            Queue<FNNType> left = new LinkedList<>();
            Boolean did_we_unpack_tuples = true;
            while (did_we_unpack_tuples) {
                did_we_unpack_tuples = false;
                var current = right.remove();
                if (!(current instanceof TupleType)) {
                    left.add(current);
                } else {
                    did_we_unpack_tuples = true;
                    for (var t : ((TupleType) current).Types) {
                        left.add(t);
                    }
                }
            }
            while (left.size() > 0) {
                var type = left.remove();
                result.Types.add(type);
            }
            while (right.size() > 0) {
                var type = right.remove();
                result.Types.add(type);
            }
        } else {
            result.Types.add(Type);
        }
        return result;
    }
}
