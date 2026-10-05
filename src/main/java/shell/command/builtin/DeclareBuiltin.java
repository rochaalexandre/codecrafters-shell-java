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
        if (line.args().size() != 2) {
            context.err().println("declare: -p requires a variable name");
            return 1;
        }
        context.out().printf("declare: %s: not found%n", line.args().get(1));
        return 0;
    }
}
