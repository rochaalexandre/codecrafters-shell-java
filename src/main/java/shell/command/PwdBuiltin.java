package shell.command;

import shell.cli.ParsedLine;
import shell.io.ExecContext;

import java.nio.file.Paths;

public class PwdBuiltin implements Builtin {
    @Override
    public String name() {
        return "pwd";
    }

    @Override
    public int run(ParsedLine line, ExecContext context) {
        String userDir = System.getProperty("user.dir");
        context.out().println(getAbsolutPath(userDir));
        return 0;
    }

    private static String getAbsolutPath(String userDir) {
        return Paths.get(userDir).toAbsolutePath().normalize().toString();
    }
}
