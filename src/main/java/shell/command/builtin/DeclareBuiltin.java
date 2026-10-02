package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.io.ExecContext;

public class DeclareBuiltin implements Builtin {
    @Override
    public String name() {
        return "declare";
    }

    @Override
    public int run(ParsedLine line, ExecContext context) {
        return 0;
    }
}
