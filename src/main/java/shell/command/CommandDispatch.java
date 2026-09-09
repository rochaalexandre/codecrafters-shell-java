package shell.command;

import shell.cli.ParsedLine;
import shell.command.builtin.Builtin;
import shell.command.builtin.BuiltinRegistry;
import shell.command.exec.ExternalCommandRunner;
import shell.env.PathResolver;
import shell.io.ExecContext;

import java.io.IOException;
import java.util.Optional;

public class CommandDispatch {


    private final PathResolver pathResolver;
    private final ExternalCommandRunner externalRunner;
    private final BuiltinRegistry builtinRegistry;

    public CommandDispatch(PathResolver pathResolver, BuiltinRegistry builtinRegistry) {
        this(pathResolver, new ExternalCommandRunner(), builtinRegistry);
    }

    public CommandDispatch(PathResolver pathResolver, ExternalCommandRunner externalRunner, BuiltinRegistry builtinRegistry) {
        this.pathResolver = pathResolver;
        this.externalRunner = externalRunner;
        this.builtinRegistry = builtinRegistry;
    }

    public void dispatch(ParsedLine line) throws IOException {
        Optional<Builtin> builtin = builtinRegistry.getBuiltin(line.command());
        if (builtin.isPresent()) {
            try (ExecContext context = ExecContext.from(line)) {
                builtin.get().run(line, context);
            }
        }
        else {
            if (pathResolver.findExecutable(line.command()).isPresent()) {
                externalRunner.run(line);
            }
            else {
                System.out.println(line.command() + ": command not found");
            }
        }
    }
}
