import java.util.*;

public class Utils {
    static void ERREXIT(String msg) {
        int i = 1;
        var caller = Thread.currentThread().getStackTrace()[i];
        while ((caller = Thread.currentThread().getStackTrace()[i]).getFileName().startsWith("Utils")) {
            ++i;
        } // Skips the errors from this file, and prints where the ERREXIT was called
        System.err.print("ERR (");
        System.err.print(caller.getFileName());
        System.err.print(":");
        System.err.print(caller.getLineNumber());
        System.err.print("): ");
        System.err.println(msg);
        System.exit(-1);
    }

    static void ASSERT(Boolean predicate, String fail_msg) {
        if (!(predicate))
            ERREXIT(fail_msg);
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

    // TODO: make non-recursive with stack if needed
    static TupleType FLATTEN(FNNType Type) {
        var result = new TupleType();
        if (Type instanceof TupleType) {
            for (var tuple_elem : ((TupleType) Type).Types) {
                var flat = FLATTEN(tuple_elem); // recursively flatten each subsequent element of the tuple
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
