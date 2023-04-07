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
}
