/**
 * Result of splitting a raw input line into the command name and its argument string.
 *
 * <p>This record is the contract between {@link InputParser} and the rest of the shell.
 * When real quoting/tokenization arrives, {@code InputParser} changes; ideally this shape
 * grows (e.g. a {@code List<String> argv}) without callers needing a rewrite.
 */
public record ParsedLine(String command, String args) {

    public boolean isCommand(String name) {
        return command.equals(name);
    }
}
