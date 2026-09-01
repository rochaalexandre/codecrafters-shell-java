import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String userCommand = scanner.nextLine();

            switch (userCommand) {
                case "exit":
                    break;
                case "echo":
                    System.out.printf("%s: command not found", userCommand);
                default:
                    System.out.printf("%s: command not found", userCommand);
            }
            System.out.println();
        }
    }
}
