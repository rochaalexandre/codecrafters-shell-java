package shell.command.exec;

import shell.cli.ParsedLine;
import shell.cli.Pipeline;
import shell.command.CommandResolver;
import shell.command.Resolved;
import shell.io.ExecContext;
import shell.io.Stream;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Starts external commands together so producers and consumers can run concurrently.
 * Built-ins currently have no stdin: a leading built-in supplies output to the first
 * process, and a trailing built-in runs independently of the preceding process's output.
 * Built-ins in the middle of a pipeline are not supported.
 */
public class PipelineRunner {

    private final CommandResolver resolver;

    public PipelineRunner(CommandResolver resolver) {
        this.resolver = resolver;
    }

    public int run(Pipeline pipeline) {
        List<Stage> stages = resolveStages(pipeline);
        List<ProcessBuilder> externalCommands = configureExternalCommands(stages);

        try {
            // Start every consumer before a built-in producer writes into its pipe.
            List<Process> processes = ProcessBuilder.startPipeline(externalCommands);
            runLeadingBuiltin(stages.getFirst(), processes);
            runTrailingBuiltin(stages.getLast());
            waitForAll(processes);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    private List<Stage> resolveStages(Pipeline pipeline) {
        return pipeline.stages().stream()
                .map( line -> new Stage(line, resolver.resolve(line)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * startPipeline wires adjacent external processes itself. We configure only their
     * boundaries: terminal at the beginning, pipes between commands, and the final
     * command's terminal/file destination at the end.
     */
    private List<ProcessBuilder> configureExternalCommands(List<Stage> stages) {
        List<ProcessBuilder> commands = new ArrayList<>();

        for (int index = 0; index < stages.size(); index++) {
            Stage stage = stages.get(index);
            if (stage.isNotExternalCommand()) {
                continue;
            }

            boolean hasPreviousStage = index > 0;
            boolean hasNextStage = (index + 1) < stages.size();

            ProcessBuilder process = ExternalCommandRunner.getProcessBuilder(stage.line());
            if (hasPreviousStage) {
                process.redirectInput(Redirect.PIPE);
            }
            if (hasNextStage) {
                Stage nextStage = stages.get(index + 1);
                // Built-ins do not read stdin, so their incoming output is discarded.
                Redirect output = nextStage.isBuiltin() ? Redirect.DISCARD : Redirect.PIPE;

                process.redirectOutput(output);
            }
            commands.add(process);
        }
        return commands;
    }

    private void runLeadingBuiltin(Stage firstStage, List<Process> processes) {
        if (firstStage.command() instanceof Resolved.BuiltinCommand(var builtin)) {
            Process consumer = processes.getFirst();
            try (ExecContext context = new ExecContext(
                    Stream.toPipe(consumer.getOutputStream()), Stream.console(System.err))) {
                builtin.run(firstStage.line(), context);
            } // Closing the pipe sends EOF to the consumer, allowing wc/head/etc. to finish.
        }
    }

    private void runTrailingBuiltin(Stage lastStage) throws IOException {
        if (lastStage.command() instanceof Resolved.BuiltinCommand(var builtin)) {
            try (ExecContext context = ExecContext.open(lastStage.line())) {
                builtin.run(lastStage.line(), context);
            }
        }
    }

    private void waitForAll(List<Process> processes) throws InterruptedException {
        for (Process process : processes) {
            process.waitFor();
        }
    }

    /** Keeps the parsed command and its resolution together throughout execution. */
    private record Stage(ParsedLine line, Resolved command) {
        boolean isBuiltin() {
            return command instanceof Resolved.BuiltinCommand;
        }
        boolean isNotExternalCommand() {
            return !(command instanceof Resolved.ExternalCommand);
        }
    }
}
