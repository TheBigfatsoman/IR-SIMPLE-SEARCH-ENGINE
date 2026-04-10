import java.util.*;

public class MainTest {

    public static void main(String[] args) {

        Set<String> dictionary = DictionaryLoader.loadDictionary("data.txt");

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Search Engine (Tolerant Mode) ===");

        while (true) {
            System.out.print("\nEnter query: ");
            String query = scanner.nextLine();

            String fixedQuery = TolerantSearch.processQuery(query, dictionary);

            System.out.println("Corrected Query: " + fixedQuery);
        }
    }
}