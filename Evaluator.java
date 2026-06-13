import java.io.*;
import java.util.*;

/**
 * Peran 1 - IR Evaluation Module
 *
 * Computes standard IR evaluation metrics against the Cranfield qrel file:
 *
 *   - Precision        = |retrieved ∩ relevant| / |retrieved|
 *   - Recall           = |retrieved ∩ relevant| / |relevant|
 *   - Precision@K      = precision computed at cutoff K (e.g. K=5,10,20)
 *   - 11-Point Average = mean precision at recall levels 0.0,0.1,...,1.0
 *                        (standard TREC interpolated precision)
 *
 * Usage:
 *   Evaluator eval = new Evaluator("data/cranqrel", "data/cran.qry");
 *   eval.evaluate(indexer, bimEngine, "BIM", 10);
 */
public class Evaluator {

    /** Generic (docId, score) pair used by evaluate(), independent of any specific engine. */
    public record ScoredDoc(int docId, double score) {}

    // ── qrel: queryId → set of relevant docIds ────────────────────────────────
    private final Map<Integer, Set<Integer>> qrels = new HashMap<>();

    // ── queries: queryId → query text ────────────────────────────────────────
    private final Map<Integer, String> queries = new HashMap<>();

    // K values used for Precision@K
    private static final int[] K_VALUES = {5, 10, 20};

    // 11 recall levels for interpolated average precision
    private static final double[] RECALL_LEVELS =
        {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};

    // ── constructor ───────────────────────────────────────────────────────────

    public Evaluator(String qrelPath, String qryPath) {
        loadQrels(qrelPath);
        loadQueries(qryPath);
    }

    // ── file parsers ──────────────────────────────────────────────────────────

    /**
     * cranqrel format: "queryId docId grade"  (one per line)
     * All grades (1-4) are treated as relevant (standard Cranfield practice).
     */
    private void loadQrels(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                int qId  = Integer.parseInt(parts[0]);
                int docId = Integer.parseInt(parts[1]);
                // grade in parts[2] — all grades treated as relevant
                qrels.computeIfAbsent(qId, k -> new HashSet<>()).add(docId);
            }
        } catch (IOException e) {
            System.err.println("Error reading qrel file: " + e.getMessage());
        }
    }

    /**
     * cran.qry format: same .I / .W structure as cran.all.1400
     */
    private void loadQueries(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            int currentId = -1;
            StringBuilder sb = new StringBuilder();
            boolean inW = false;

            while ((line = br.readLine()) != null) {
                if (line.startsWith(".I")) {
                    if (currentId != -1 && sb.length() > 0) {
                        queries.put(currentId, sb.toString().trim());
                    }
                    currentId = Integer.parseInt(line.substring(2).trim());
                    sb = new StringBuilder();
                    inW = false;
                } else if (line.startsWith(".W")) {
                    inW = true;
                } else if (inW) {
                    sb.append(line).append(" ");
                }
            }
            if (currentId != -1 && sb.length() > 0) {
                queries.put(currentId, sb.toString().trim());
            }
        } catch (IOException e) {
            System.err.println("Error reading query file: " + e.getMessage());
        }
    }

    // ── public API ────────────────────────────────────────────────────────────

    /**
     * Evaluate any ranking model across all queries that have qrel judgments.
     * Prints averages and the 11-point interpolated precision table to stdout.
     * Optionally prints the full per-query table.
     *
     * @param engine     a (query, topK) -> List&lt;ScoredDoc&gt; search function
     * @param modelName  display name (e.g. "BIM", "BM25", "Two-Poisson")
     * @param topK       max documents to retrieve per query
     * @param printTable whether to print the full per-query table
     */
    public void evaluate(ScoredSearch engine, String modelName, int topK, boolean printTable) {
        System.out.println("\n" + "=".repeat(65));
        System.out.println("  EVALUASI MODEL: " + modelName);
        System.out.println("=".repeat(65));

        List<Double> allPrecisions   = new ArrayList<>();
        List<Double> allRecalls      = new ArrayList<>();
        Map<Integer, List<Double>> allPrecAtK = new LinkedHashMap<>();
        for (int k : K_VALUES) allPrecAtK.put(k, new ArrayList<>());
        List<double[]> allInterpolated = new ArrayList<>(); // 11 points per query

        // Queries that have both a text and a qrel judgment
        List<Integer> evalQueryIds = new ArrayList<>(queries.keySet());
        evalQueryIds.retainAll(qrels.keySet());
        Collections.sort(evalQueryIds);

        if (printTable) {
            System.out.printf("%-8s %-10s %-10s", "QueryID", "Precision", "Recall");
            for (int k : K_VALUES) System.out.printf(" P@%-4d", k);
            System.out.println();
            System.out.println("-".repeat(65));
        }

        for (int qId : evalQueryIds) {
            String queryText      = queries.get(qId);
            Set<Integer> relevant = qrels.get(qId);

            // Retrieve ranked list via the generic search function
            List<ScoredDoc> ranked = engine.search(queryText, topK);
            List<Integer> retrievedIds = new ArrayList<>();
            for (ScoredDoc sd : ranked) retrievedIds.add(sd.docId());

            // Compute metrics
            double precision   = precision(retrievedIds, relevant);
            double recall      = recall(retrievedIds, relevant);
            double[] precAtK   = precisionAtK(retrievedIds, relevant, K_VALUES);
            double[] interp    = interpolated11Point(retrievedIds, relevant);

            allPrecisions.add(precision);
            allRecalls.add(recall);
            for (int i = 0; i < K_VALUES.length; i++) {
                allPrecAtK.get(K_VALUES[i]).add(precAtK[i]);
            }
            allInterpolated.add(interp);

            if (printTable) {
                System.out.printf("%-8d %-10.4f %-10.4f", qId, precision, recall);
                for (double pk : precAtK) System.out.printf(" %-6.4f", pk);
                System.out.println();
            }
        }

        // ── Averages ─────────────────────────────────────────────────────────
        if (printTable) System.out.println("-".repeat(65));
        System.out.printf("%-8s %-10.4f %-10.4f",
            "AVG", mean(allPrecisions), mean(allRecalls));
        for (int k : K_VALUES) {
            System.out.printf(" %-6.4f", mean(allPrecAtK.get(k)));
        }
        System.out.println();

        // ── 11-Point Interpolated Average Precision ───────────────────────────
        System.out.println("\n11-Point Interpolated Average Precision (" + modelName + "):");
        System.out.printf("%-8s", "Recall");
        for (double r : RECALL_LEVELS) System.out.printf(" %4.1f ", r);
        System.out.println();
        System.out.printf("%-8s", "Prec");
        double[] avgInterp = averageInterpolated(allInterpolated);
        for (double v : avgInterp) System.out.printf(" %.3f", v);
        System.out.println();
        System.out.printf("Mean Average Precision (MAP) = %.4f%n", mean(allPrecisions));
        System.out.println("=".repeat(65));
    }

    // ── metric computations ───────────────────────────────────────────────────

    /**
     * Precision = |retrieved ∩ relevant| / |retrieved|
     */
    public double precision(List<Integer> retrieved, Set<Integer> relevant) {
        if (retrieved.isEmpty()) return 0.0;
        int hits = 0;
        for (int docId : retrieved) if (relevant.contains(docId)) hits++;
        return (double) hits / retrieved.size();
    }

    /**
     * Recall = |retrieved ∩ relevant| / |relevant|
     */
    public double recall(List<Integer> retrieved, Set<Integer> relevant) {
        if (relevant.isEmpty()) return 0.0;
        int hits = 0;
        for (int docId : retrieved) if (relevant.contains(docId)) hits++;
        return (double) hits / relevant.size();
    }

    /**
     * Precision@K for each value in ks[].
     * Computes precision using only the top-k retrieved documents.
     */
    public double[] precisionAtK(List<Integer> retrieved, Set<Integer> relevant, int[] ks) {
        double[] results = new double[ks.length];
        for (int i = 0; i < ks.length; i++) {
            int k = ks[i];
            List<Integer> topK = retrieved.subList(0, Math.min(k, retrieved.size()));
            results[i] = precision(topK, relevant);
        }
        return results;
    }

    /**
     * 11-Point Interpolated Precision.
     *
     * For each recall level r in {0.0, 0.1, ..., 1.0}:
     *   interpolated precision = max precision at any recall >= r
     *
     * This is the standard method used in the Cranfield/TREC evaluations.
     */
    public double[] interpolated11Point(List<Integer> retrieved, Set<Integer> relevant) {
        double[] result = new double[11];
        if (relevant.isEmpty() || retrieved.isEmpty()) return result;

        int totalRelevant = relevant.size();
        int hits = 0;

        // Build precision-recall curve: at each rank position
        // store (recall, precision) pairs
        List<double[]> curve = new ArrayList<>();
        curve.add(new double[]{0.0, 1.0}); // start point

        for (int rank = 0; rank < retrieved.size(); rank++) {
            if (relevant.contains(retrieved.get(rank))) {
                hits++;
                double r = (double) hits / totalRelevant;
                double p = (double) hits / (rank + 1);
                curve.add(new double[]{r, p});
            }
        }

        // Interpolate: for each recall level, take max precision at recall >= level
        for (int i = 0; i < RECALL_LEVELS.length; i++) {
            double recallLevel = RECALL_LEVELS[i];
            double maxPrec = 0.0;
            for (double[] point : curve) {
                if (point[0] >= recallLevel - 1e-9) {
                    maxPrec = Math.max(maxPrec, point[1]);
                }
            }
            result[i] = maxPrec;
        }
        return result;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private double mean(List<Double> vals) {
        if (vals.isEmpty()) return 0.0;
        double sum = 0;
        for (double v : vals) sum += v;
        return sum / vals.size();
    }

    private double[] averageInterpolated(List<double[]> all) {
        double[] avg = new double[11];
        if (all.isEmpty()) return avg;
        for (double[] row : all) {
            for (int i = 0; i < 11; i++) avg[i] += row[i];
        }
        for (int i = 0; i < 11; i++) avg[i] /= all.size();
        return avg;
    }

    // ── getters for external use (e.g. report generation) ────────────────────

    public Map<Integer, Set<Integer>> getQrels()   { return qrels; }
    public Map<Integer, String>       getQueries() { return queries; }
    public int[]                      getKValues() { return K_VALUES; }

    // ── standalone test ───────────────────────────────────────────────────────

    public static void main(String[] args) {
        Indexer indexer = new Indexer();
        indexer.invertedIndex();
        System.out.println("Index built. N=" + indexer.getN());

        BIMEngine bim = new BIMEngine(indexer);
        Evaluator eval = new Evaluator("data/cranqrel", "data/cran.qry");

        System.out.println("Queries loaded : " + eval.getQueries().size());
        System.out.println("Qrel entries   : " + eval.getQrels().size() + " queries with judgments");

        eval.evaluate((q, k) -> {
            List<ScoredDoc> out = new ArrayList<>();
            for (BIMEngine.ScoredDoc sd : bim.search(q, k)) out.add(new ScoredDoc(sd.docId(), sd.score()));
            return out;
        }, "BIM", 10, true);
    }
}