package shell.io;

import shell.cli.ParsedLine;
import shell.cli.Redirect;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

/**
 * Where a running command sends its output: two {@link Stream}s, stdout and stderr. The
 * command just writes to {@link #out()} / {@link #err()} without caring where they go.
 *
 * <p>This class only composes the streams. Each {@link Stream} knows whether it is a terminal
 * or a file and how to clean itself up, so {@link #close()} is just "close both". Use in
 * try-with-resources so any file streams get flushed.
 */
public final class ExecContext implements AutoCloseable {

    private final Stream out;
    private final Stream err;

    public ExecContext(Stream out, Stream err) {
        this.out = out;
        this.err = err;
    }

    public static ExecContext defaultContext() {
        return new ExecContext(Stream.console(System.out), Stream.console(System.err));
    }

    /**
     * Opens output resources from a parsed line: a redirected stream goes to its file (truncating or
     * appending per {@code >} / {@code >>}), an unredirected one stays on the terminal.
     */
    public static ExecContext open(ParsedLine line) throws IOException {
        Stream out = toStream(line.stdout(), System.out);
        try {
            return new ExecContext(out, toStream(line.stderr(), System.err));
        } catch (IOException | RuntimeException e) {
            out.close();
            throw e;
        }
    }

    private static Stream toStream(Redirect redirect, PrintStream terminal) throws IOException {
        if (redirect == null) {
            return Stream.console(terminal);
        }
        return Stream.toFile(Path.of(redirect.target()), redirect.append());
    }

    /** The stream the running command writes its normal output to. */
    public PrintStream out() {
        return out.stream();
    }

    /** The stream the running command writes its error output to. */
    public PrintStream err() {
        return err.stream();
    }

    /** Closes both streams. Each is a no-op unless it owns a file. */
    @Override
    public void close() {
        out.close();
        err.close();
    }
}
