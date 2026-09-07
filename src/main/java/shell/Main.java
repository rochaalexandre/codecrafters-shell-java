package shell;

import shell.cli.InputParser;
import shell.cli.ParsedLine;
import shell.command.Builtin;
import shell.command.BuiltinRegistry;
import shell.env.PathResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
            if (output.isEmpty()) {
                continue;
            }

            if (line.hasStdoutRedirect()) {
                redirectOutputToFile(line, output.get());
            } else {
                System.out.println(output.get());
            }

        }
    }

    private static Optional<String> getOutput(ParsedLine line) throws Exception {
        Optional<Builtin> builtin = BUILTIN_REGISTRY.getBuiltin(line.command());
        if (builtin.isPresent()) {
            return builtin.get().run(line);
        }

        if (PATH_RESOLVER.findExecutable(line.command()).isPresent()) {
            executeProgram(line.command(), line);
            return Optional.empty();
        }
        return Optional.of(line.command() + ": command not found");
    }

    private static void executeProgram(String command, ParsedLine line) throws Exception {
        List<String> commandList = new ArrayList<>();
        commandList.add(command);
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
            proc.waitFor();
        }
    }

    private static void redirectOutputToFile(ParsedLine line, String output) throws IOException {
        Path filePath = Path.of(line.stdoutTarget());
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }
        if (Files.notExists(filePath)) {
            Files.createFile(filePath);
        }
        Files.writeString(filePath, output + "\n");
    }
}
