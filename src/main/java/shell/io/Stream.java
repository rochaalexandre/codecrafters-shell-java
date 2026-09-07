package shell.io;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * One place a command's output can go, plus the knowledge of how to clean it up.
 *
 * <p>A terminal stream ({@link #console}) wraps {@link System#out} / {@link System#err} and
 * is never closed — it belongs to the JVM. A file stream ({@link #toFile}) is opened here and
 * owned here: {@link #close()} closes it.
 *
 * <p>This type exists so {@link ExecContext} does not carry a {@code boolean ownsIt} beside
 * every stream. When a new kind of destination arrives (a pipe to another process), it is a
 * new factory here and a new branch in {@link #close()} — {@code ExecContext} does not change.
 */
public final class Stream implements AutoCloseable {

    private final PrintStream stream;
    private final boolean owns;

    private Stream(PrintStream stream, boolean owns) {
        this.stream = stream;
        this.owns = owns;
    }

    /** Wraps a JVM-owned terminal stream (typically {@link System#out} or {@link System#err}). */
    public static Stream console(PrintStream terminal) {
        return new Stream(terminal, false);
    }

    /**
     * Opens {@code target} for writing and creates missing parent directories. {@code append}
     * false truncates any existing file ({@code >}); true keeps it and writes at the end
     * ({@code >>}). The returned stream owns the file — close it (try-with-resources).
     */
    public static Stream toFile(Path target, boolean append) throws IOException {
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        StandardOpenOption mode = append
                ? StandardOpenOption.APPEND
                : StandardOpenOption.TRUNCATE_EXISTING;
        PrintStream ps = new PrintStream(
                Files.newOutputStream(target, StandardOpenOption.CREATE, mode));
        return new Stream(ps, true);
    }

    /** The stream a command writes to. */
    public PrintStream stream() {
        return stream;
    }

    /** Closes the underlying stream only if this object opened it; a no-op for the terminal. */
    @Override
    public void close() {
        if (owns) {
            stream.close();
        }
    }
}
