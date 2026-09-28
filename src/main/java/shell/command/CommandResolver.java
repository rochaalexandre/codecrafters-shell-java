package shell.command;

import shell.cli.ParsedLine;
import shell.command.builtin.Builtin;
import shell.command.builtin.BuiltinRegistry;
import shell.env.PathResolver;

import java.util.Optional;

/**
 * Answers only "what is this command?" — never runs it, so each caller (a single command, a
 * pipeline stage) decides where its I/O goes.
 */
public class CommandResolver {

    private final BuiltinRegistry builtinRegistry;
    private final PathResolver pathResolver;

    public CommandResolver(BuiltinRegistry builtinRegistry, PathResolver pathResolver) {
        this.builtinRegistry = builtinRegistry;
        this.pathResolver = pathResolver;
    }

    public Resolved resolve(ParsedLine line) {
        Optional<Builtin> builtin = builtinRegistry.getBuiltin(line.command());
        if (builtin.isPresent()) {
            return new Resolved.BuiltinCommand(builtin.get());
        }
        if (pathResolver.findExecutable(line.command()).isPresent()) {
            return new Resolved.ExternalCommand();
        }
        return new Resolved.NotFound();
    }
}
