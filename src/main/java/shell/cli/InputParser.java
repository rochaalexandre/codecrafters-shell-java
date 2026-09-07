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
        if (parts.length < 2 || parts[1].isBlank()) {
            return new ParsedLine(command, "");
        }

        String part = parts[1];
        boolean appendRedirect = part.contains(">>") || part.contains("<<");
        String[] halves = part.split("\\s*1?>\\s*", 2);
        String args = halves[0].trim();
        return halves.length == 2
                ? new ParsedLine(command, args, halves[1].trim(), appendRedirect)
                : new ParsedLine(command, args);
    }
}
