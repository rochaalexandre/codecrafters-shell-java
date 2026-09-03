package shell.command;

import shell.cli.ParsedLine;

import java.util.Optional;

public class EchoBuiltin implements Builtin {
    @Override
    public String name() {
        return "echo";
    }

    @Override
    public Optional<String> run(ParsedLine line) {
        return Optional.of(line.args().replace("echo ", ""));
    }
}
