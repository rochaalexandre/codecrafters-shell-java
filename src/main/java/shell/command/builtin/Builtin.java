package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.io.ExecContext;

public interface Builtin {
    String name();
    int run(ParsedLine line, ExecContext context);
}
