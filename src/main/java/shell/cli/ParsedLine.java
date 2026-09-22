package shell.cli;

/**
 * Result of splitting a raw input line into the command name, its argument string,
 * and any output redirections.
 *
 * <p>This record is the contract between {@link InputParser} and the rest of the shell.
 * When real quoting/tokenization arrives, {@code InputParser} changes; ideally this shape
 * grows (e.g. a {@code List<String> argv}) without callers needing a rewrite.
 *
 * <p>{@code stdout} / {@code stderr} are {@code null} when that stream is not redirected.
 */
public record ParsedLine(String command, String args, Redirect stdout, Redirect stderr, boolean runInBackground) {

    public ParsedLine(String command, String args, boolean runInBackground) {
        this(command, args, null, null, runInBackground);
    }

    public boolean isCommand(String name) {
        return command.equals(name);
    }

    public boolean hasStdoutRedirect() {
        return stdout != null;
    }

    public boolean hasStderrRedirect() {
        return stderr != null;
    }

    public boolean runInBackground() {
        return runInBackground;
    }
}
