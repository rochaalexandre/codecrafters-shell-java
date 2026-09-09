package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.io.ExecContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CdBuiltin implements Builtin {
    @Override
    public String name() {
        return "cd";
    }

    @Override
    public int run(ParsedLine line, ExecContext context) {
        Path path = getPath(line.args());
        if (Files.notExists(path)) {
            context.err().println("cd: " + line.args() + ": No such file or directory");
        } else {
            System.setProperty("user.dir", path.toAbsolutePath().toString());
        }
        return 0;
    }

    private static Path getPath(String args) {
        if (args.startsWith("./") || args.startsWith("../")) {
            return Paths.get(System.getProperty("user.dir"), args);
        }
        if (args.equals("~")) {
            String homePath = System.getenv("HOME");
            return Paths.get(homePath);
        }
        return Paths.get(args);
    }
}
