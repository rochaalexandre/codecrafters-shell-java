package shell.cli;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns a raw input line into a {@link Pipeline} of {@link ParsedLine} stages.
 *
 * <p>Arguments are split on whitespace here. This is the single place quoting,
 * escaping and whitespace handling will live once those phases arrive, so nothing else
 * in the shell needs to learn about tokenization.
 */
public final class InputParser {
    /** fd (group 1: "", "1", "2") + operator (group 2: ">" or ">>") + target (group 3). */
    private static final Pattern REDIRECT_PATTERN = Pattern.compile("([12]?)(>>?)\\s*(\\S+)");

    /**
     * Splits on {@code |} into stages. Naive: a {@code |} inside quotes would split too;
     * revisit when the quoting phase lands.
     */
    public Pipeline parse(String input) {
        return new Pipeline(Arrays.stream(input.split("\\|"))
                .map(String::strip)
                .map(this::parseStage)
                .toList());
    }

    private ParsedLine parseStage(String input) {
        boolean isBackgroundCommand = input.trim().endsWith("&");
        String[] parts = input.replace("&", "").split("\\s+", 2);
        String command = parts[0];
        if (parts.length < 2 || parts[1].isBlank()) {
            return new ParsedLine(command, List.of(), isBackgroundCommand);
        }

        String rest = parts[1];
        Matcher matcher = REDIRECT_PATTERN.matcher(rest);
        String args = rest;
        Redirect stdout = null;
        Redirect stderr = null;

        boolean firstMatch = true;
        while (matcher.find()) {
            if (firstMatch) {
                args = rest.substring(0, matcher.start()).trim();
                firstMatch = false;
            }
            String fd = matcher.group(1); // "", "1", or "2"
            Redirect r = new Redirect(matcher.group(3), matcher.group(2).equals(">>"));

            if (fd.equals("2")) {
                stderr = r;
            } else {
                stdout = r;  // "" or "1" both mean stdout
            }
        }

        // Quoted argument grouping remains deferred until the quoting phase.
        args = args.replace("\"", "");

        List<String> arguments = args.isBlank() ? List.of() : List.of(args.strip().split("\\s+"));
        return new ParsedLine(command, arguments, stdout, stderr, isBackgroundCommand);
    }
}
