package shell.command;

import shell.cli.ParsedLine;

import java.util.Optional;

public interface Builtin {
    String name();
    Optional<String> run(ParsedLine line);
}
