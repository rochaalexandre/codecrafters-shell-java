package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.io.ExecContext;

public class JobBuiltin implements Builtin {
    @Override
    public String name() {
        return "job";
    }

    @Override
    public int run(ParsedLine line, ExecContext context) {
        return 0;
    }
}
