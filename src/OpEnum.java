public enum OpEnum {
    Minus, Plus, Multiply, Divide, LessThan, GreaterThan, Equals;

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
        case 'L':
        case '<':
            return OpEnum.LessThan;
        case '>':
            return OpEnum.GreaterThan;
        case '=':
            return OpEnum.Equals;

        default:
            throw new IllegalArgumentException("char: " + c + " Could not be parsed");
        }
    }
}
