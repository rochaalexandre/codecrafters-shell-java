import java.util.List;
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
            String[] parts = input.split(" ", 2);
            String cmd = parts[0];
            String userArgs = getUserArgs(parts);

            String output = "";
            if (input.equals(EXIT)) {
                break;
            } else if (cmd.equals(ECHO)) {
                output = input.replace("echo ", "");
            } else if (cmd.equals(TYPE)) {
                if (BUILT_IN_COMMANDS.contains(userArgs)) {
                    output = userArgs + " is a shell builtin";
                } else  if (!BUILT_IN_COMMANDS.contains(userArgs)) {
                    output = userArgs + ": not found";
                }
            } else {
                output = cmd + ": command not found";
            }

            System.out.println(output);
        }
    }

    private static String getUserArgs(String[] parts) {
        boolean hasArgs = parts.length > 1 && !parts[1].isBlank();
        String userArgs = hasArgs ? parts[1] : "";
        return userArgs;
    }
}
