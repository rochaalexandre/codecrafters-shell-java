package shell.cli;

/**
 * Turns a raw input line into a {@link ParsedLine}.
 *
 * <p>Today this is a naive split on the first space. This is the single place quoting,
 * escaping and whitespace handling will live once those phases arrive, so nothing else
 * in the shell needs to learn about tokenization.
 */
public final class InputParser {

    public ParsedLine parse(String input) {
        String[] parts = input.split(" ", 2);
        String command = parts[0];
        boolean hasArgs = parts.length > 1 && !parts[1].isBlank();
        String args = hasArgs ? parts[1] : "";
        return new ParsedLine(command, args);
    }
}
