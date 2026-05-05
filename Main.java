import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        System.out.println(
            "Initializing system and building Inverted Index..."
        );

        // 1. Initialize Peran 1's Indexer
        Indexer indexer = new Indexer();
        indexer.invertedIndex();
        System.out.println(
            "Documents loaded: " + indexer.getInvertedIndex().size()
        );

        // 2. Extract the dictionary (all unique terms) from the inverted index
        Set<String> dictionary = indexer.getInvertedIndex().keySet();
        System.out.println(
            "Index built successfully! Vocabulary size: " +
                dictionary.size() +
                " terms."
        );

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print(
                """
                \nList Command:
                1. Lihat Isi Inverted Index
                2. BOOLEAN QUERY (dengan Tolerant Search)
                3. Exit
                Input Command:
                """
            );

            int command = sc.nextInt();
            sc.nextLine(); // consume the newline character

            switch (command) {
                case 1:
                    System.out.println("Menampilkan Inverted Index (Sample):");
                    // Print just a few to avoid flooding the console
                    indexer
                        .getInvertedIndex()
                        .entrySet()
                        .stream()
                        .limit(10)
                        .forEach(entry ->
                            System.out.println(
                                entry.getKey() + " -> " + entry.getValue()
                            )
                        );
                    break;
                case 2:
                    System.out.print("Masukkan Query: ");
                    String rawQuery = sc.nextLine();

                    // 3. Pass the raw query through Peran 3's Tolerant Search
                    String correctedQuery = TolerantSearch.processQuery(
                        rawQuery,
                        dictionary
                    );

                    System.out.println("Original Query  : " + rawQuery);
                    System.out.println("Corrected Query : " + correctedQuery);

                    // 4. Execute the Boolean Engine (Peran 2)
                    BooleanEngine engine = new BooleanEngine(
                        indexer.getInvertedIndex(),
                        correctedQuery
                    );
                    List<Integer> results = engine.getResults();

                    if (results.isEmpty()) {
                        System.out.println(
                            "No documents found matching the query."
                        );
                    } else {
                        System.out.println(
                            "Documents found (" +
                                results.size() +
                                "): " +
                                results
                        );
                    }
                    break;
                case 3:
                    System.out.println("Exiting...");
                    sc.close();
                    return;
                default:
                    System.out.println("Command tidak ditemukan");
                    break;
            }
        }
    }
}
