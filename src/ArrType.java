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
}
