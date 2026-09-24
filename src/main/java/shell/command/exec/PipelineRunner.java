package shell.command.exec;

import shell.cli.Pipeline;

import java.io.IOException;
import java.util.List;

/**
 * Runs a multi-stage {@link Pipeline}: starts every stage, wires each stage's stdout to the
 * next stage's stdin, then waits on all of them (like bash).
 */
public class PipelineRunner {

    public int run(Pipeline pipeline) {
        List<ProcessBuilder> processBuilders = getProcessBuilderList(pipeline);

        try {
            List<Process> processList = ProcessBuilder.startPipeline(processBuilders);
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
    private static List<ProcessBuilder> getProcessBuilderList(Pipeline pipeline) {
        List<ProcessBuilder> processBuilders = pipeline.stages()
                .stream()
                .map(ExternalCommandRunner::getProcessBuilder)
                .toList();

        int last = processBuilders.size() - 1;
        for (int i = 0; i <= last; i++) {
            ProcessBuilder processBuilder = processBuilders.get(i);
            if (i > 0) {
                processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
            }
            if (i < last) {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            }
        }
        return processBuilders;
    }
}
