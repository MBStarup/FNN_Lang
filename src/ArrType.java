public class ArrType extends FNNType {
    public FNNType Type;
    public int Size;

    public ArrType(FNNType type, int size) {
        this.Type = type;
        this.Size = size;
    }

    @Override
    public String toString() {
        return Type.toString() + "[" + Size + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;

        if (!(other instanceof FNNType))
            return false;

        if (other instanceof TupleType)
            return other.equals(this);

        if (other instanceof ArrType)
            return ((ArrType) other).Size == this.Size && ((ArrType) other).Type.equals(this.Type);

        return false;
    }
}
