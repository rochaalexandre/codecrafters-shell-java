package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.io.ExecContext;

public class CompleteBuiltin implements Builtin {
    @Override
    public String name() {
        return "complete";
    }

    @Override
    public int run(ParsedLine line, ExecContext context) {
        String command = line.args().replace("-p", "").trim();

        context.out().printf("complete: %s: no completion specification\n", command);
        return 0;
    }
}
