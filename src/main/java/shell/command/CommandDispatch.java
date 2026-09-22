package shell.command;

import shell.cli.ParsedLine;
import shell.command.builtin.Builtin;
import shell.command.builtin.BuiltinRegistry;
import shell.command.exec.ExternalCommandRunner;
import shell.command.job.Job;
import shell.command.job.JobManager;
import shell.env.PathResolver;
import shell.io.ExecContext;

import java.io.IOException;
import java.util.Optional;

public class CommandDispatch {


    private final PathResolver pathResolver;
    private final ExternalCommandRunner externalRunner;
    private final BuiltinRegistry builtinRegistry;
    private final JobManager jobManager;

    public CommandDispatch(PathResolver pathResolver, BuiltinRegistry builtinRegistry, JobManager registry) {
        this(pathResolver, new ExternalCommandRunner(), builtinRegistry, registry);
    }

    public CommandDispatch(PathResolver pathResolver, ExternalCommandRunner externalRunner, BuiltinRegistry builtinRegistry, JobManager jobManager) {
        this.pathResolver = pathResolver;
        this.externalRunner = externalRunner;
        this.builtinRegistry = builtinRegistry;
        this.jobManager = jobManager;
    }

    public void dispatch(ParsedLine line) throws IOException {
        try (ExecContext context = ExecContext.from(line)) {
            dispatch(line, context);
        }
    }

    private void dispatch(ParsedLine line, ExecContext context) {
        if (line.runInBackground()) {
            Process process = externalRunner.runInBackground(line);

            String command = line.command();
            if (line.args() != null && !line.args().isEmpty() ) {
                command = command.concat(" ").concat(line.args());
            }

            Job job = jobManager.add(process, command);
            context.out().printf("[%s] %s%n", job.number(), job.pid());
            return;
        }

        Optional<Builtin> builtin = builtinRegistry.getBuiltin(line.command());
        if (builtin.isPresent()) {
            builtin.get().run(line, context);
        }
        else {
            if (pathResolver.findExecutable(line.command()).isPresent()) {
                externalRunner.run(line);
            }
            else {
                context.out().println(line.command() + ": command not found");
            }
        }
    }
}
