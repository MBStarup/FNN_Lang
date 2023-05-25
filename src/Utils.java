import java.util.*;

public class Utils {
    static void ERREXIT(String msg) {
        int i = 1;
        var caller = Thread.currentThread().getStackTrace()[i];
        while ((caller = Thread.currentThread().getStackTrace()[i]).getFileName().startsWith("Utils")) { // Skips the errors from this file, and prints where the ERREXIT was called (fx. so it doesn't say the err is from the ASSERT method in this file, but where that was called originally)
            ++i;
        }
        System.err.print("ERR (" + caller.getFileName() + ":" + caller.getLineNumber() + "): ");
        System.err.println(msg);
        System.exit(-1);
    }

    static void ASSERT(Boolean predicate, String fail_msg) {
        if (!(predicate))
            ERREXIT(fail_msg);
    }

    static FNNType TRY_UNWRAP(FNNType type) {
        if (!(type instanceof TupleType)) {
            return type;
        }

        if (((TupleType) type).Types.size() != 1) {
            return type;
        }

        return TRY_UNWRAP(((TupleType) type).Types.get(0));
    }

    static List<FNNType> AS_LIST(FNNType type) {
        var t = Utils.TRY_UNWRAP(type);
        if (t instanceof TupleType) {
            return ((TupleType) t).Types;
        }

        var res = new Vector<FNNType>();
        res.add(type);
        return res;
    }

    static TupleType FLATTEN(FNNType Type) {
        var result = new TupleType();
        if (Type instanceof TupleType) {
            for (var tuple_elem : ((TupleType) Type).Types) {
                var flat = FLATTEN(tuple_elem);
                for (var t : flat.Types) {
                    result.Types.add(t);
                }
            }
        } else {
            result.Types.add(Type);
        }
        return result;
    }

}
