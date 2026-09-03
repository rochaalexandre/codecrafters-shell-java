package shell;

import shell.cli.InputParser;
import shell.cli.ParsedLine;
import shell.env.PathResolver;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static final String ECHO = "echo";
    public static final String EXIT = "exit";
    public static final String TYPE = "type";
    public static final String PWD = "pwd";
    public static final List<String> BUILT_IN_COMMANDS = List.of(ECHO, TYPE, EXIT, PWD);

    private static final InputParser PARSER = new InputParser();
    private static final PathResolver PATH_RESOLVER = new PathResolver();

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
        String output = null;
        if (line.isCommand(ECHO)) {
            output = line.args().replace("echo ", "");
        } else if (line.isCommand(TYPE)) {
            output = getCommandType(line.args());
        } else if (line.isCommand(PWD)) {
            output = Paths.get("").toAbsolutePath().normalize().toString();
        } else {
            if (PATH_RESOLVER.findExecutable(line.command()).isPresent()) {
                executeProgram(line.command(), line.args());
            } else {
                output = line.command() + ": command not found";
            }
        }
        return Optional.ofNullable(output);
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

    private static String getCommandType(String userArgs) {
        if (BUILT_IN_COMMANDS.contains(userArgs)) {
            return userArgs + " is a shell builtin";
        }

        return PATH_RESOLVER.findExecutable(userArgs)
                .map(path -> userArgs + " is " + path)
                .orElseGet(() -> userArgs + ": not found");
    }
}
