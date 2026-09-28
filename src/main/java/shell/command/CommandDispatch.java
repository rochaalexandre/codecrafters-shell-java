package shell.command;

import shell.cli.ParsedLine;
import shell.cli.Pipeline;
import shell.command.builtin.Builtin;
import shell.command.builtin.BuiltinRegistry;
import shell.command.exec.ExternalCommandRunner;
import shell.command.exec.PipelineRunner;
import shell.command.job.Job;
import shell.command.job.JobManager;
import shell.env.PathResolver;
import shell.io.ExecContext;

import java.io.IOException;

public class CommandDispatch {


    private final CommandResolver resolver;
    private final ExternalCommandRunner externalRunner;
    private final PipelineRunner pipelineRunner;
    private final JobManager jobManager;

    public CommandDispatch(PathResolver pathResolver, BuiltinRegistry builtinRegistry, JobManager registry) {
        this(new CommandResolver(builtinRegistry, pathResolver), registry);
    }

    private CommandDispatch(CommandResolver resolver, JobManager jobManager) {
        this(resolver, new ExternalCommandRunner(), new PipelineRunner(resolver), jobManager);
    }

    public CommandDispatch(CommandResolver resolver, ExternalCommandRunner externalRunner, PipelineRunner pipelineRunner, JobManager jobManager) {
        this.resolver = resolver;
        this.externalRunner = externalRunner;
        this.pipelineRunner = pipelineRunner;
        this.jobManager = jobManager;
    }

    public void dispatch(Pipeline pipeline) throws IOException {
        if (pipeline.isSingleStage()) {
            dispatch(pipeline.stages().getFirst());
        }
        else {
            pipelineRunner.run(pipeline);
        }
    }

    private void dispatch(ParsedLine line) throws IOException {
        try (ExecContext context = ExecContext.from(line)) {
            dispatch(line, context);
        }
    }

    private void dispatch(ParsedLine line, ExecContext context) {
        if (line.runInBackground()) {
            Process process = externalRunner.runInBackground(line);

            String command = line.command();
            if (line.args() != null && !line.args().isEmpty()) {
                command = command.concat(" ").concat(line.args());
            }

            Job job = jobManager.add(process, command);
            context.out().printf("[%s] %s%n", job.number(), job.pid());
            return;
        }

        switch (resolver.resolve(line)) {
            case Resolved.BuiltinCommand(Builtin builtin) -> builtin.run(line, context);
            case Resolved.ExternalCommand() -> externalRunner.run(line);
            case Resolved.NotFound() -> context.out().println(line.command() + ": command not found");
        }
    }
}
