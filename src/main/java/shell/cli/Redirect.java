package shell.cli;

/**
 * One output redirection parsed off the command line: the file a stream is pointed at,
 * and whether it truncates ({@code >}) or appends ({@code >>}).
 *
 * <p>Which stream this applies to (stdout / stderr) is recorded by {@link ParsedLine}'s
 * field name, not here.
 */
public record Redirect(String target, boolean append) {
}
