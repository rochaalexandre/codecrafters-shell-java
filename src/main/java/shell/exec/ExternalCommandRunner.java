package shell.exec;

import shell.cli.ParsedLine;
import shell.cli.Redirect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExternalCommandRunner {
    public int run(ParsedLine line) {
        List<String> commandList = new ArrayList<>();
        commandList.add(line.command());
        String userArgs = line.args();
        if (!userArgs.isBlank()) {
            commandList.addAll(Arrays.asList(userArgs.split(" ")));
        }
        ProcessBuilder pb = new ProcessBuilder(commandList);
        pb.directory(new File(System.getProperty("user.dir")));
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(target(line.stdout()));
        pb.redirectError(target(line.stderr()));
        try (Process proc = pb.start()) {
            return proc.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** A file destination for {@code redirect}, or the parent terminal when it is {@code null}. */
    private static ProcessBuilder.Redirect target(Redirect redirect) {
        if (redirect == null) {
            return ProcessBuilder.Redirect.INHERIT;
        }
        File file = new File(redirect.target());
        return redirect.append()
                ? ProcessBuilder.Redirect.appendTo(file)
                : ProcessBuilder.Redirect.to(file);
    }
}
