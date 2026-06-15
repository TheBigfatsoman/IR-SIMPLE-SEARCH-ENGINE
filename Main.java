import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        System.out.println(
                "Initializing system and building Inverted Index...");

        // 1. Initialize Peran 1's Indexer
        Indexer indexer = new Indexer();
        indexer.invertedIndex();
        System.out.println("Documents loaded : " + indexer.getN());
        System.out.printf("Average doc len  : %.2f tokens%n", indexer.getAvgdl());
        System.out.println("Vocabulary size  : " + indexer.getVocabulary().size() + " terms");

        BIMEngine bimEngine = new BIMEngine(indexer);
        TP_Ranker tpRanker  = new TP_Ranker(indexer);
        BM25 bm25           = new BM25(indexer);
        BM11Engine bm11Engine = new BM11Engine(indexer);

        Evaluator evaluator = new Evaluator("data/cranqrel", "data/cran.qry");

        // 2. Extract the dictionary (all unique terms) from the inverted index
        Set<String> dictionary = indexer.getInvertedIndex().keySet();
        System.out.println("Index built successfully!");

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
                            Task 2 - Search:
                            3. BIM Search
                            4. TP Search
                            5. BM25 Search
                            6. BM11 Search
                            =========================================
                            Task 3 - Evaluation:
                            7. Evaluate BIM
                            8. Evaluate TP
                            9. Evaluate BM25
                            10. Evaluate BM11
                            =========================================
                            0. Exit
                            Input Command:
                            """);

            int command;
            try {
                command = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (command) {
                case 1:
                    System.out.println("Menampilkan Inverted Index (Sample):");
                    indexer
                            .getInvertedIndex()
                            .entrySet()
                            .stream()
                            .limit(10)
                            .forEach(entry -> System.out.println(
                                    entry.getKey() + " -> " + entry.getValue()));
                    System.out.print("\nPress Enter to return to menu...");
                    sc.nextLine();
                    break;

                case 2: {
                    System.out.print("Masukkan Query: ");
                    String rawQuery = sc.nextLine();

                    String correctedQuery = TolerantSearch.processQuery(rawQuery, dictionary);

                    System.out.println("Original Query  : " + rawQuery);
                    System.out.println("Corrected Query : " + correctedQuery);

                    BooleanEngine engine = new BooleanEngine(indexer.getInvertedIndex(), correctedQuery);
                    List<Integer> results = engine.getResults();

                    if (results.isEmpty()) {
                        System.out.println("No documents found matching the query.");
                    } else {
                        System.out.println("Documents found (" + results.size() + "): " + results);
                    }
                    System.out.print("\nPress Enter to return to menu...");
                    sc.nextLine();
                    break;
                }

                case 3: {
                    System.out.print("Enter query: ");
                    String query = sc.nextLine();
                    System.out.print("Top-k results: ");
                    int k = parseIntSafe(sc.nextLine(), 10);

                    List<BIMEngine.ScoredDoc> res = bimEngine.search(query, k);
                    printRankedResults("BIM Search", res, BIMEngine.ScoredDoc::docId, BIMEngine.ScoredDoc::score);

                    System.out.print("\nPress Enter to return to menu...");
                    sc.nextLine();
                    break;
                }

                case 4: {
                    System.out.print("Enter query: ");
                    String query = sc.nextLine();
                    System.out.print("Top-k results: ");
                    int k = parseIntSafe(sc.nextLine(), 10);

                    List<TP_Ranker.ScoredDoc> res = tpRanker.search(query, k);
                    printRankedResults("TP Search", res, TP_Ranker.ScoredDoc::docId, TP_Ranker.ScoredDoc::score);

                    System.out.print("\nPress Enter to return to menu...");
                    sc.nextLine();
                    break;
                }

                case 5: {
                    System.out.print("Enter query: ");
                    String query = sc.nextLine();
                    System.out.print("Top-k results: ");
                    int k = parseIntSafe(sc.nextLine(), 10);

                    List<BM25.ScoredDoc> res = bm25.search(query, k);
                    printRankedResults("BM25 Search", res, BM25.ScoredDoc::docId, BM25.ScoredDoc::score);

                    System.out.print("\nPress Enter to return to menu...");
                    sc.nextLine();
                    break;
                }

                case 6: {
                    System.out.print("Enter query: ");
                    String query = sc.nextLine();
                    System.out.print("Top-k results: ");
                    int k = parseIntSafe(sc.nextLine(), 10);

                    List<BM11Engine.ScoredDoc> res = bm11Engine.search(query, k);
                    printRankedResults("BM11 Search", res, BM11Engine.ScoredDoc::docId, BM11Engine.ScoredDoc::score);

                    System.out.print("\nPress Enter to return to menu...");
                    sc.nextLine();
                    break;
                }

                case 7: {
                    System.out.print("Top-k for evaluation (default 10): ");
                    int k = parseIntSafe(sc.nextLine(), 10);
                    boolean printTable = askPrintTable(sc);

                    evaluator.evaluate((q, topK) -> toScored(bimEngine.search(q, topK), BIMEngine.ScoredDoc::docId, BIMEngine.ScoredDoc::score),
                            "BIM", k, printTable);

                    System.out.print("\nPress Enter to return to menu...");
                    sc.nextLine();
                    break;
                }

                case 8: {
                    System.out.print("Top-k for evaluation (default 10): ");
                    int k = parseIntSafe(sc.nextLine(), 10);
                    boolean printTable = askPrintTable(sc);

                    evaluator.evaluate((q, topK) -> toScored(tpRanker.search(q, topK), TP_Ranker.ScoredDoc::docId, TP_Ranker.ScoredDoc::score),
                            "Two-Poisson", k, printTable);

                    System.out.print("\nPress Enter to return to menu...");
                    sc.nextLine();
                    break;
                }

                case 9: {
                    System.out.print("Top-k for evaluation (default 10): ");
                    int k = parseIntSafe(sc.nextLine(), 10);
                    boolean printTable = askPrintTable(sc);

                    evaluator.evaluate((q, topK) -> toScored(bm25.search(q, topK), BM25.ScoredDoc::docId, BM25.ScoredDoc::score),
                            "BM25", k, printTable);

                    System.out.print("\nPress Enter to return to menu...");
                    sc.nextLine();
                    break;
                }

                case 10: {
                    System.out.print("Top-k for evaluation (default 10): ");
                    int k = parseIntSafe(sc.nextLine(), 10);
                    boolean printTable = askPrintTable(sc);

                    evaluator.evaluate((q, topK) -> toScored(bm11Engine.search(q, topK), BM11Engine.ScoredDoc::docId, BM11Engine.ScoredDoc::score),
                            "BM11", k, printTable);

                    System.out.print("\nPress Enter to return to menu...");
                    sc.nextLine();
                    break;
                }

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

    // ── helpers ───────────────────────────────────────────────────────────────

    private static boolean askPrintTable(Scanner sc) {
        System.out.print("Print per-query table? (y/n): ");
        return sc.nextLine().trim().equalsIgnoreCase("y");
    }

    /**
     * Generic printer for any (docId, score) record type.
     * Pass method references for the docId() and score() accessors.
     */
    private static <T> void printRankedResults(
            String modelName,
            List<T> results,
            java.util.function.ToIntFunction<T> docIdFn,
            java.util.function.ToDoubleFunction<T> scoreFn) {

        System.out.println("\n" + modelName + " - Top " + results.size() + " results:");
        if (results.isEmpty()) {
            System.out.println("(no results found)");
            return;
        }
        System.out.printf("%-4s  %-10s  %s%n", "Rank", "Doc ID", "Score");
        System.out.println("================================");
        for (int i = 0; i < results.size(); i++) {
            T sd = results.get(i);
            System.out.printf("%-4d  Doc-%04d    %.6f%n", i + 1, docIdFn.applyAsInt(sd), scoreFn.applyAsDouble(sd));
        }
    }

    /**
     * Converts any (docId, score) record list into Evaluator.ScoredDoc list.
     */
    private static <T> List<Evaluator.ScoredDoc> toScored(
            List<T> results,
            java.util.function.ToIntFunction<T> docIdFn,
            java.util.function.ToDoubleFunction<T> scoreFn) {

        List<Evaluator.ScoredDoc> out = new ArrayList<>();
        for (T sd : results) {
            out.add(new Evaluator.ScoredDoc(docIdFn.applyAsInt(sd), scoreFn.applyAsDouble(sd)));
        }
        return out;
    }

    private static int parseIntSafe(String s, int defaultVal) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}