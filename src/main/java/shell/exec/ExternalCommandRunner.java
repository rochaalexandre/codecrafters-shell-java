package shell.exec;

import shell.cli.ParsedLine;
import shell.command.ExecContext;

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
        if (line.hasStdoutRedirect()) {
            pb.redirectOutput(new File(line.stdoutTarget()));
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        }
        else {
            pb.inheritIO();
        }
        try (Process proc = pb.start()) {
            return proc.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    };
}
