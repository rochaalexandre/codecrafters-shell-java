package shell.command;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Where a command sends its output. {@code Main} picks the destination (terminal or file),
 * hands this to the command, and the command just writes to {@link #out()} without caring
 * where it goes.
 *
 * <p>{@link #close()} closes the stream only when we opened it ({@link #toFile}); it never
 * closes {@link System#out} ({@link #console}), which belongs to the JVM. Use it in
 * try-with-resources so the file gets flushed.
 *
 * <p>Next bit: a second stream for stderr ({@code type nope 2> err}).
 */
public final class ExecContext implements AutoCloseable {

    private final PrintStream out;

    /** {@code true} if this object opened {@link #out} and is responsible for closing it. */
    private final boolean ownsOut;

    private ExecContext(PrintStream out, boolean ownsOut) {
        this.out = out;
        this.ownsOut = ownsOut;
    }

    /** Output goes to the terminal. The stream is {@link System#out} and is never closed. */
    public static ExecContext console() {
        return new ExecContext(System.out, false);
    }

    /**
     * Output goes to {@code target}, truncating any existing file. Missing parent directories
     * are created. The returned context owns the file stream — close it (try-with-resources).
     */
    public static ExecContext toFile(Path target) throws IOException {
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        return new ExecContext(new PrintStream(Files.newOutputStream(target)), true);
    }

    /** The stream the running command writes its normal output to. */
    public PrintStream out() {
        return out;
    }

    /** Closes the output stream only if this context opened it (a file); a no-op for the terminal. */
    @Override
    public void close() {
        if (ownsOut) {
            out.close();
        }
    }
}
