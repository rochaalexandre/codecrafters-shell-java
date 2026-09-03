package shell.command;

import shell.cli.ParsedLine;

import java.nio.file.Paths;
import java.util.Optional;

public class PwdBuiltin implements Builtin {
    @Override
    public String name() {
        return "pwd";
    }

    @Override
    public Optional<String> run(ParsedLine line) {
        return Optional.of(Paths.get("").toAbsolutePath().normalize().toString());
    }
}
