import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.print("""
                List Command:
                1. Buat Inverted Index
                2. BOOLEAN QUERY
                Input Command:
                        """);
        Scanner sc = new Scanner(System.in);
        int command = sc.nextInt();
        sc.nextLine(); // consume the newline character left by nextInt()
        switch (command) {
            case 1:
                System.out.println("Buat Inverted Index");
                break;
            case 2:
                System.out.println("BOOLEAN QUERY");
                String query = sc.nextLine();
                System.out.println("Query is " + query);
                break;
            default:
                System.out.println("Command tidak ditemukan");
                break;
        }
    }
}
