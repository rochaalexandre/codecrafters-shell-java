package shell;

import shell.cli.InputParser;
import shell.cli.ParsedLine;
import shell.command.Builtin;
import shell.command.BuiltinRegistry;
import shell.env.PathResolver;

import java.io.File;
import java.util.*;

public class Main {

    public static final String EXIT = "exit";

    private static final InputParser PARSER = new InputParser();
    private static final PathResolver PATH_RESOLVER = new PathResolver();
    private static final BuiltinRegistry BUILTIN_REGISTRY = new BuiltinRegistry();

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            ParsedLine line = PARSER.parse(input);
            if (line.isCommand(EXIT)) {
                break;
            }
            Optional<String> output = getOutput(line);
            output.ifPresent(System.out::println);
        }
    }

    private static Optional<String> getOutput(ParsedLine line) throws Exception {
        Optional<Builtin> builtin = BUILTIN_REGISTRY.getBuiltin(line.command());
        if (builtin.isPresent()) {
            return builtin.get().run(line);
        }

        if (PATH_RESOLVER.findExecutable(line.command()).isPresent()) {
            executeProgram(line.command(), line.args());
            return Optional.empty();
        }
        return Optional.of(line.command() + ": command not found");
    }

    private static void executeProgram(String command, String userArgs) throws Exception {
        List<String> commandList = new ArrayList<>();
        commandList.add(command);
        if (!userArgs.isBlank()) {
            commandList.addAll(Arrays.asList(userArgs.split(" ")));
        }
        ProcessBuilder pb = new ProcessBuilder(commandList);
        pb.directory(new File(System.getProperty("user.dir")));
        pb.inheritIO();
        try (Process proc = pb.start()) {
            proc.waitFor();
        }
    }
}
