import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("$ ");

        String userName = scanner.nextLine();
        System.out.println( userName + " command not found");
    }
}
