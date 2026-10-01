package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.io.ExecContext;

public class HistoryBuiltin implements Builtin {
    @Override
    public String name() {
        return "history";
    }

    @Override
    public int run(ParsedLine line, ExecContext context) {
        return 0;
    }
}
