import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String userCommand = scanner.nextLine();

            if (userCommand.equals("exit")) {
                break;
            } else if (userCommand.startsWith("echo")) {
                System.out.print(userCommand.replace("echo ", ""));
            } else {
                System.out.printf("%s: command not found", userCommand);
            }
            System.out.println();
        }
    }
}
