public class FuncType extends FNNType {
    FNNType Arg;
    FNNType Ret;

    @Override
    public String toString() {
        String result = "( ";
        result += Arg.toString();
        result += ") -> ( ";
        result += Ret.toString();
        result += ")";
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;

        if (other instanceof TupleType)
            return other.equals(this);

        if (other instanceof FuncType) {
            return ((FuncType) other).Arg.equals(this.Arg) && ((FuncType) other).Ret.equals(this.Ret);
        }

        return false;
    }
}