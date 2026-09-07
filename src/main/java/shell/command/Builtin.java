package shell.command;

import shell.cli.ParsedLine;

public interface Builtin {
    String name();
    int run(ParsedLine line, ExecContext context);
}
