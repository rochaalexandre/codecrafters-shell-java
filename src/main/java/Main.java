import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {

        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("$ ");

            String userCommand = scanner.nextLine();
            System.out.printf("%s: command not found", userCommand);
            scanner.close();
        }
    }
}
