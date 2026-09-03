package shell.command;

import shell.cli.ParsedLine;
import shell.env.PathResolver;

import java.util.Optional;

import static shell.Main.EXIT;

public class TypeBuiltin implements Builtin {
    private static final PathResolver PATH_RESOLVER = new PathResolver();

    private final BuiltinRegistry registry;

    public TypeBuiltin(BuiltinRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String name() {
        return "type";
    }

    @Override
    public Optional<String> run(ParsedLine line) {
        return Optional.of(getCommandType(line.args()));
    }

    private String getCommandType(String userArgs) {
        if (registry.isBuiltin(userArgs) || EXIT.equals(userArgs)) {
            return userArgs + " is a shell builtin";
        }

        return PATH_RESOLVER.findExecutable(userArgs)
                .map(path -> userArgs + " is " + path)
                .orElseGet(() -> userArgs + ": not found");
    }
}
