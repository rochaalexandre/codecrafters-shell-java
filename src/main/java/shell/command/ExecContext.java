package shell.command;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ExecContext implements AutoCloseable {
    private final PrintStream out;
    private final boolean ownsOut;

    private ExecContext(PrintStream out, boolean ownsOut) {
        this.out = out;
        this.ownsOut = ownsOut;
    }

    public static ExecContext console() {
        return new ExecContext(System.out, false);
    }

    public static ExecContext toFile(Path target) throws IOException {
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        return new ExecContext(new PrintStream(Files.newOutputStream(target)), true);
    }

    public PrintStream out() {
        return out;
    }

    @Override
    public void close() {
        if (ownsOut) {
            out.close();
        }
    }
}
