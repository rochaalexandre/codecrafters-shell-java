package shell.command;

import shell.cli.ParsedLine;

public class EchoBuiltin implements Builtin {
    @Override
    public String name() {
        return "echo";
    }

    @Override
    public int run(ParsedLine line, ExecContext context) {
        context.out().println(line.args().replace("echo ", ""));
        return 0;
    }
}
