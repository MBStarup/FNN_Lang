public enum OpEnum {
    Minus, Plus, Multiply, Divide;

    public static OpEnum parseChar(char c) {
        switch (c) {
            case '-':
                return OpEnum.Minus;
            case '+':
                return OpEnum.Plus;
            case '*':
                return OpEnum.Multiply;
            case '/':
                return OpEnum.Divide;

            default:
                throw new IllegalArgumentException("char: " + c + " Could not be parsed");
        }
    }
}
