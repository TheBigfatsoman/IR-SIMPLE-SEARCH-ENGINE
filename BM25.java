import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BM25 {
    private final Indexer indexer;

    private double k1 = 1.2;
    private double b = 0.75;

    public BM25(Indexer indexer) {
        this.indexer = indexer;
    }

    public void setK1(double k1) {
        this.k1 = k1;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double score(String term, int docId) {
        int tf = indexer.getTf(term, docId);

        if (tf == 0) return 0.0;

        int N = indexer.getN();
        int df = indexer.getDf(term);

        int dl = indexer.getDocLength(docId);
        double avgdl = indexer.getAvgdl();

        double idf = Math.log((N - df + 0.5) / (df + 0.5) + 1);

        double norm = tf + k1 * (1 -b + b * (dl / avgdl));
        return idf * ((tf * (k1 + 1)) / norm);
    }

    /**
     * Ranks the documents based on the input using BM25
     * @param queryTerms list of the query
     * @return Map of the docID to the score to the query
     */
    public Map<Integer, Double> rank (List<String> queryTerms) {
        Map<Integer, Double> scores = new HashMap<>();

        for (Integer docId : indexer.getDocIds()) {
            double score = 0.0;
            for (String term : queryTerms) {
                score += score(term, docId);
            }

            if (score > 0) {
                scores.put(docId, score);
            }
        }

        return sort(scores);
    }

    private Map<Integer, Double> sort (Map<Integer, Double> scores) {
        List<Map.Entry<Integer, Double>> entries = new ArrayList<>(scores.entrySet());
        entries.sort((a,b) -> Double.compare(b.getValue(), a.getValue()));

        Map<Integer, Double> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
