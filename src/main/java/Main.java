import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static final String ECHO = "echo";
    public static final String EXIT = "exit";
    public static final String TYPE = "type";
    public static final List<String> BUILT_IN_COMMANDS = List.of(ECHO, TYPE, EXIT);

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            String cmd = getCmd(input);
            String userArgs = getUserArgs(input);
            if (cmd.equals(EXIT)) {
                break;
            }
            Optional<String> output = getOutput(input, cmd, userArgs);
            output.ifPresent(System.out::println);
        }
    }

    private static Optional<String> getOutput(String input, String cmd, String userArgs) throws Exception {
        String output = null;
        if (cmd.equals(ECHO)) {
            output = userArgs.replace("echo ", "");
        } else if (cmd.equals(TYPE)) {
            output = getCommandType(userArgs);
        } else {
            if (findExecutable(cmd).isPresent()) {
               executeProgram(cmd, userArgs);
            } else {
                output = cmd + ": command not found";
            }
        }
        return Optional.ofNullable(output);
    }

    private static void executeProgram(String cmd, String userArgs) throws Exception {
        List<String> cmdList = new ArrayList<>();
        cmdList.add(cmd);
        if (!userArgs.isBlank()) {
            cmdList.addAll(Arrays.asList(userArgs.split(" ")));
        }
        ProcessBuilder pb = new ProcessBuilder(cmdList);
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

        return findExecutable(userArgs).map(path -> userArgs + " is " + path).orElseGet(() -> userArgs + ": not found");
    }

    private static Optional<Path> findExecutable(String userArgs) {
        String envPath = System.getenv("PATH");
        for (String dir : envPath.split(File.pathSeparator)) {
            Path file = Paths.get(dir, userArgs);
            if (Files.exists(file) && Files.isExecutable(file)) {
                return Optional.of(file);
            }
        }
        return Optional.empty();
    }

    private static String getCmd(String input) {
        String[] parts = input.split(" ", 2);
        return parts[0];
    }

    private static String getUserArgs(String input) {
        String[] parts = input.split(" ", 2);
        boolean hasArgs = parts.length > 1 && !parts[1].isBlank();
        return hasArgs ? parts[1] : "";
    }
}
