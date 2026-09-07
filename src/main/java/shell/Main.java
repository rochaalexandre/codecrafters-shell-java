package shell;

import shell.cli.InputParser;
import shell.cli.ParsedLine;
import shell.command.Builtin;
import shell.command.BuiltinRegistry;
import shell.command.ExecContext;
import shell.env.PathResolver;
import shell.exec.ExternalCommandRunner;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {

    public static final String EXIT = "exit";

    private static final InputParser PARSER = new InputParser();
    private static final PathResolver PATH_RESOLVER = new PathResolver();
    private static final BuiltinRegistry BUILTIN_REGISTRY = new BuiltinRegistry();
    private static final ExternalCommandRunner EXTERNAL_COMMAND_RUNNER = new ExternalCommandRunner();

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            ParsedLine line = PARSER.parse(input);
            if (line.isCommand(EXIT)) {
                break;
            }

            dispatch(line);

        }
    }

    private static void dispatch(ParsedLine line) throws Exception {
        Optional<Builtin> builtin = BUILTIN_REGISTRY.getBuiltin(line.command());
        if (builtin.isPresent()) {
            PrintStream outputStream = getOutputStream(line);
            builtin.get().run(line,  new ExecContext(outputStream));
            outputStream.close();
        } else if (PATH_RESOLVER.findExecutable(line.command()).isPresent()) {
            EXTERNAL_COMMAND_RUNNER.run(line);
        } else {
            System.out.println(line.command() + ": command not found");
        }
    }


    private static PrintStream getOutputStream(ParsedLine line) throws IOException {
        PrintStream out = System.out;
        if (line.hasStdoutRedirect()) {
            Path p = Path.of(line.stdoutTarget());
            if (p.getParent() != null) {
                Files.createDirectories(p.getParent());
            }
            out = new PrintStream(Files.newOutputStream(p));
        }
        return out;
    }
}
