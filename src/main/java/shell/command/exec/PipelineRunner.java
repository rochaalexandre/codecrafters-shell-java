package shell.command.exec;

import shell.cli.ParsedLine;
import shell.cli.Pipeline;
import shell.command.CommandResolver;
import shell.command.Resolved;
import shell.command.builtin.Builtin;
import shell.io.ExecContext;
import shell.io.Stream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs a multi-stage {@link Pipeline}: starts every stage, wires each stage's stdout to the
 * next stage's stdin, then waits on all of them (like bash).
 */
public class PipelineRunner {

    private final CommandResolver resolver;

    public PipelineRunner(CommandResolver resolver) {
        this.resolver = resolver;
    }

    public int run(Pipeline pipeline) {
        List<ProcessBuilder> processBuilders = getProcessBuilderList(pipeline);

        try {
            List<Process> processList = ProcessBuilder.startPipeline(processBuilders);


            ParsedLine first = pipeline.stages().getFirst();
            if (resolver.resolve(first) instanceof Resolved.BuiltinCommand(Builtin builtin)) {
                try (ExecContext context = new ExecContext(
                        Stream.toPipe(processList.getFirst().getOutputStream()),   // ← wc's stdin
                        Stream.console(System.err))) {
                    builtin.run(first, context);
                }                                                    // ← close() here = EOF for wc
            }

            // A builtin at the end ignores stdin, so it just writes to the terminal (or its own
            // redirect); the stage feeding it had its output discarded in getProcessBuilderList.
            ParsedLine lastStage = pipeline.stages().getLast();
            if (resolver.resolve(lastStage) instanceof Resolved.BuiltinCommand(Builtin builtin)) {
                try (ExecContext context = ExecContext.open(lastStage)) {
                    builtin.run(lastStage, context);
                }
            }

            for (Process process : processList) {
                process.waitFor();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    /**
     * Builds one {@link ProcessBuilder} per stage and wires them for
     * {@link ProcessBuilder#startPipeline}: every inner boundary becomes a {@code PIPE}.
     *
     * <p>The two outer ends are left exactly as {@link ExternalCommandRunner#getProcessBuilder}
     * set them, so the first stage still reads the terminal and the last stage still honours
     * its own {@code >} / {@code >>} (or the terminal when there is none).
     */
    private List<ProcessBuilder> getProcessBuilderList(Pipeline pipeline) {

        int last = pipeline.stages().size() - 1;
        List<ProcessBuilder> processBuilders = new ArrayList<>();
        for (int i = 0; i <= last; i++) {
            ParsedLine stage = pipeline.stages().get(i);
            if (resolver.resolve(stage) instanceof Resolved.ExternalCommand) {
                ProcessBuilder processBuilder = ExternalCommandRunner.getProcessBuilder(stage);
                if (i > 0) {
                    processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
                }
                if (i < last) {
                    // Builtins never read stdin, so output headed into one has nowhere to go.
                    boolean nextIsBuiltin = resolver.resolve(pipeline.stages().get(i + 1))
                            instanceof Resolved.BuiltinCommand;
                    processBuilder.redirectOutput(nextIsBuiltin
                            ? ProcessBuilder.Redirect.DISCARD
                            : ProcessBuilder.Redirect.PIPE);
                }
                processBuilders.add(processBuilder);
            }
        }

        return processBuilders;
    }
}
