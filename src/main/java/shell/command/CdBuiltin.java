package shell.command;

import shell.cli.ParsedLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class CdBuiltin implements Builtin {
    @Override
    public String name() {
        return "cd";
    }

    @Override
    public Optional<String> run(ParsedLine line) {
        Path path;
        if (line.args().startsWith("./")) {
            path = Paths.get(System.getProperty("user.dir"), line.args());
        } else if (line.args().startsWith("../")) {
            Path currentDir = Paths.get(System.getProperty("user.dir"));
            for (String index : line.args().split("/")) {
                currentDir = currentDir.getParent();
            }
            path = currentDir.getParent();
        } else   {
            path = Paths.get(line.args());
        }

        if (Files.notExists(path)) {
            return Optional.of("cd: "+line.args()+": No such file or directory");
        } else {
            System.setProperty("user.dir", path.toAbsolutePath().toString());
        }
        return Optional.empty();
    }
}
