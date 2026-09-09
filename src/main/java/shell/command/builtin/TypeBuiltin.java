package shell.command.builtin;

import shell.cli.ParsedLine;
import shell.env.PathResolver;
import shell.io.ExecContext;

import static shell.command.builtin.BuiltinRegistry.EXIT;

public class TypeBuiltin implements Builtin {

    private final BuiltinRegistry registry;
    private final PathResolver pathResolver;

    public TypeBuiltin(BuiltinRegistry registry, PathResolver pathResolver) {
        this.registry = registry;
        this.pathResolver = pathResolver;
    }

    @Override
    public String name() {
        return "type";
    }

    @Override
    public int run(ParsedLine line, ExecContext context) {
        context.out().println(getCommandType(line.args()));
        return 0;
    }

    private String getCommandType(String userArgs) {
        if (registry.isBuiltin(userArgs) || EXIT.equals(userArgs)) {
            return userArgs + " is a shell builtin";
        }

        return pathResolver.findExecutable(userArgs)
                .map(path -> userArgs + " is " + path)
                .orElseGet(() -> userArgs + ": not found");
    }
}
