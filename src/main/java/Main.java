import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            String cmd = input.split(" ", 2)[0];

            if (input.equals("exit")) {
                break;
            }
            String output = switch (cmd) {
                case "echo" -> input.replace("echo ", "");
                default -> cmd +": command not found";
            };

            System.out.println(output);
        }
    }
}
