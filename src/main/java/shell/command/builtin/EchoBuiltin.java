package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.io.ExecContext;

public class EchoBuiltin implements Builtin {
    @Override
    public String name() {
        return "echo";
    }

    @Override
    public int run(ParsedLine line, ExecContext context) {
        context.out().println(String.join(" ", line.args()));
        return 0;
    }
}
