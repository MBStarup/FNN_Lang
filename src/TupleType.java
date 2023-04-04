import java.lang.ProcessBuilder.Redirect.Type;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

public class TupleType extends FNNType {
    public List<FNNType> Types = new Vector<>();

    @Override
    public String toString() {
        String result = "( ";
        for (FNNType type : Types) {
            result += type;
            result += " ";
        }
        result += ")";
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;

        if (!(other instanceof FNNType))
            return false;

        if (!(other instanceof TupleType))
            if (Types.size() != 1)
                return false;
            else
                return Types.get(0).equals(other);

        // Else, both are tuples
        // flatten both tuples
        Stack<FNNType> stack = new Stack<>();
        Stack<FNNType> reverse_stack = new Stack<>();
        List<FNNType> list_a = new Vector<>();
        stack.push(this);
        while (stack.size() != 0) {
            var curr = stack.pop();
            if (curr instanceof TupleType) {
                for (FNNType type : ((TupleType) curr).Types) {
                    reverse_stack.push(type);
                }
                while (reverse_stack.size() > 0) {
                    stack.push(reverse_stack.pop());
                }
            } else {
                list_a.add(curr);
            }
        }
        List<FNNType> list_b = new Vector<>();
        stack.push((TupleType) other);
        while (stack.size() != 0) {
            var curr = stack.pop();
            if (curr instanceof TupleType) {
                for (FNNType type : ((TupleType) curr).Types) {
                    reverse_stack.push(type);
                }
                while (reverse_stack.size() > 0) {
                    stack.push(reverse_stack.pop());
                }
            } else {
                list_b.add(curr);
            }
        }

        if (list_a.size() != list_b.size())
            return false;

        for (int i = 0; i < list_a.size(); i++) {
            if (!(list_a.get(i).equals(list_b.get(i))))
                return false;
        }
        return true;
    }
}