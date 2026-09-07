package shell.command;

import java.io.PrintStream;

/**
 * Where a running command sends its output. {@code Main} builds one from two {@link Stream}s
 * (stdout, stderr); the command just writes to {@link #out()} / {@link #err()} without caring
 * where they go.
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
