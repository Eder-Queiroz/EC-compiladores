package comp.ppmlexer.lexer;

public sealed interface TokenValue {

    record Text(String value) implements TokenValue {
        @Override
        public String toString() {
            return value;
        }
    }

    record WholeNumber(int value) implements TokenValue {
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
