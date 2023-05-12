public enum OpEnum {
    Minus, Plus, Multiply, Divide, LessThan, GreaterThan, Equals, Power;

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
        case '<':
            return OpEnum.LessThan;
        case '>':
            return OpEnum.GreaterThan;
        case '=':
            return OpEnum.Equals;
        case '^':
            return OpEnum.Power;

        default:
            throw new IllegalArgumentException("char: " + c + " Could not be parsed");
        }
    }
}
