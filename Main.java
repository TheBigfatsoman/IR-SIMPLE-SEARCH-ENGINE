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
        System.out.println("Documents loaded : " + indexer.getN());
        System.out.printf ("Average doc len  : %.2f tokens%n", indexer.getAvgdl());
        System.out.println("Vocabulary size  : " + indexer.getVocabulary().size() + " terms");

        BIMEngine bimEngine = new BIMEngine(indexer);

        // 2. Extract the dictionary (all unique terms) from the inverted index
        Set<String> dictionary = indexer.getInvertedIndex().keySet();
        System.out.println(
            "Index built successfully!"
        );

        Scanner sc = new Scanner(System.in);

        System.out.print("\nPress Enter to continue to menu...");
        sc.nextLine();

        while (true) {
            System.out.print(
                """
                \n
                =========================================
                List Command:
                =========================================
                Task 1:
                1. Lihat Isi Inverted Index
                2. BOOLEAN QUERY (dengan Tolerant Search)
                =========================================
                Task 2:
                3. BIM Search
                4. TP (Edit this later)
                5. BM25 (Edit this later)
                6. BM10 (Edit this later)
                =========================================
                0. Exit
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
                    System.out.print("\nPress Enter to return to menu...");
                    sc.nextLine();
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
                    System.out.print("\nPress Enter to return to menu...");
                    sc.nextLine();
                    break;
                case 3:
                    System.out.print("Enter query: ");
                    String query = sc.nextLine();
                    System.out.print("Top-k results: ");
                    int k = parseIntSafe(sc.nextLine(), 10);

                    List<BIMEngine.ScoredDoc> res = bimEngine.search(query, k);
                    printRankedResults("BIM Search", res);

                    System.out.print("\nPress Enter to return to menu...");
                    sc.nextLine();
                    break;

                // 4 and 5 for peran 2
                case 4:
                    // Keep at bottom
                    System.out.print("\nPress Enter to return to menu...");
                    sc.nextLine();
                    break;
                case 5:
                    // Keep at bottom
                    System.out.print("\nPress Enter to return to menu...");
                    sc.nextLine();
                    break;

                // 6 for peran 3
                case 6:
                    // Keep at bottom
                    System.out.print("\nPress Enter to return to menu...");
                    sc.nextLine();
                    break;
   
                case 0:
                    System.out.println("Exiting...");
                    sc.close();
                    return;
                default:
                    System.out.println("Command tidak ditemukan");
                    break;
            }
        }
    }

    private static void printRankedResults(String modelName, List<BIMEngine.ScoredDoc> results) {
        System.out.println("\n" + modelName + " - Top " + results.size() + " results:");
        if (results.isEmpty()) {
            System.out.println("(no results found)");
            return;
        }
        System.out.printf("%-4s  %-10s  %s%n", "Rank", "Doc ID", "Score (RSV)");
        System.out.println("================================");
        for (int i = 0; i < results.size(); i++) {
            BIMEngine.ScoredDoc sd = results.get(i);
            System.out.printf("%-4d  Doc-%04d    %.6f%n", i + 1, sd.docId(), sd.score());
        }
    }
 
    private static int parseIntSafe(String s, int defaultVal) {
        try { 
            return Integer.parseInt(s.trim()); 
        }
        catch (NumberFormatException e) { 
            return defaultVal; 
        }
    }

}
