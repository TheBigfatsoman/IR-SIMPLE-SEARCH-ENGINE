import java.util.*;

public class BIMEngine {
    private final Indexer indexer;
 
    public record ScoredDoc(int docId, double score) {}

    public BIMEngine(Indexer indexer) {
        this.indexer = indexer;
    }

    private double logOdds(double p) {
        return Math.log(p / (1.0 - p));
    }

    public List<ScoredDoc> search(String query, int topK) {
        List<String> queryTerms = new Tokenizer().tokenize(query);
        int N = indexer.getN();

        Map<String, Double> termWeights = new HashMap<>();
        for (String term : queryTerms) {
            if (termWeights.containsKey(term)) continue;

            int df = indexer.getDf(term);
            if (df == 0) continue;

            double pt = 0.5;
            double ut = (df + 0.5) / (N + 1.0);
            double ct = logOdds(pt) - logOdds(ut);
            termWeights.put(term, ct);
        }

        // Accumulate RSV per document
        Map<Integer, Double> scores = new HashMap<>();
        for (Map.Entry<String, Double> tw : termWeights.entrySet()) {
            for (Map.Entry<Integer, Integer> posting : indexer.getPostings(tw.getKey()).entrySet()) {
                scores.merge(posting.getKey(), tw.getValue(), Double::sum);
            }
        }

        // Sort descending and return top-k
        List<ScoredDoc> ranked = new ArrayList<>(scores.size());
        for (Map.Entry<Integer, Double> e : scores.entrySet()) {
            ranked.add(new ScoredDoc(e.getKey(), e.getValue()));
        }
        ranked.sort(Comparator.comparingDouble(ScoredDoc::score).reversed());

        return ranked.subList(0, Math.min(topK, ranked.size()));
    }
}
