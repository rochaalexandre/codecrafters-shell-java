package shell.cli;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns a raw input line into a {@link ParsedLine}.
 *
 * <p>Today this is a naive split on the first space. This is the single place quoting,
 * escaping and whitespace handling will live once those phases arrive, so nothing else
 * in the shell needs to learn about tokenization.
 */
public final class InputParser {
    /** fd (group 1: "", "1", "2") + operator (group 2: ">" or ">>") + target (group 3). */
    private static final Pattern REDIRECT_PATTERN = Pattern.compile("([12]?)(>>?)\\s*(\\S+)");

    public ParsedLine parse(String input) {
        String[] parts = input.split(" ", 2);
        String command = parts[0];
        if (parts.length < 2 || parts[1].isBlank()) {
            return new ParsedLine(command, "");
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

        return new ParsedLine(command, args, stdout, stderr);
    }
}
