import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

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

            String output = getOutput(input, cmd, userArgs);

            System.out.println(output);
        }
    }

    private static String getOutput(String input, String cmd, String userArgs) {
        String output = "";
        if (cmd.equals(ECHO)) {
            output = userArgs.replace("echo ", "");
        } else if (cmd.equals(TYPE)) {
            if (BUILT_IN_COMMANDS.contains(userArgs)) {
                output = userArgs + " is a shell builtin";
            } else {
                Optional<Path> exe = findExecutable(userArgs);
                output = exe.map(path -> userArgs + " is " + path).orElseGet(() -> userArgs + ": not found");
            }
        } else {
            output = cmd + ": command not found";
        }
        return output;
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
