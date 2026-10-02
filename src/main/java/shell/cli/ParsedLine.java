package shell.cli;

import java.util.List;

/**
 * Parsed command, arguments, and output destinations passed to execution.
 */
public record ParsedLine(String command, List<String> args, Redirect stdout, Redirect stderr, boolean runInBackground) {

    public ParsedLine {
        args = List.copyOf(args);
    }

    public ParsedLine(String command, List<String> args, boolean runInBackground) {
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

    public String getFullCommand() {
        if (args == null || args.isEmpty()) {
            return command;
        }
        return command.concat(" ").concat(String.join(" ", args));
    }
}
