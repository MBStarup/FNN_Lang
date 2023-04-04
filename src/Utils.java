public class Utils {
    static void ERREXIT(String msg) {
        System.err.println(msg);
        System.exit(-1);
    }

    static void ASSERT(Boolean predicate, String fail_msg) {
        if (!(predicate))
            ERREXIT(fail_msg);
    }
}
