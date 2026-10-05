package shell.command.exec;

import shell.cli.ParsedLine;
import shell.cli.Redirect;
import shell.command.env.VariablesManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ExternalCommandRunner {
    private static VariablesManager variablesManager = null;

    public ExternalCommandRunner(VariablesManager variablesManager) {
        ExternalCommandRunner.variablesManager = variablesManager;
    }

    public int run(ParsedLine line) {
        ProcessBuilder pb = getProcessBuilder(line);
        try (Process proc = pb.start()) {
            return proc.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Process runInBackground(ParsedLine line) {
        try  {
            ProcessBuilder pb = getProcessBuilder(line);
            return pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ProcessBuilder getProcessBuilder(ParsedLine line) {
        List<String> commandList = new ArrayList<>();
        commandList.add(line.command());
        commandList.addAll(getAgrsWithReplacedEnv(line));
        ProcessBuilder pb = new ProcessBuilder(commandList);
        pb.directory(new File(System.getProperty("user.dir")));
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(target(line.stdout()));
        pb.redirectError(target(line.stderr()));
        return pb;
    }

    private static List<String> getAgrsWithReplacedEnv(ParsedLine line) {
        List<String> args = line.args();
        if (variablesManager != null) {
            args = args.stream().map(arg -> variablesManager.replaceVariables(arg))
                    .filter(Predicate.not(String::isEmpty)).toList();
        }
        return args;
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
