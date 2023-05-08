public class BaseType extends FNNType {
    public TypeEnum Type;

    public BaseType(TypeEnum type) {
        this.Type = type;
    }

    @Override
    public String toString() {
        return Type.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (other instanceof TupleType)
            return other.equals(this);
        if (!(other instanceof BaseType))
            return false;

        return this.Type == ((BaseType) other).Type;
    }
}
