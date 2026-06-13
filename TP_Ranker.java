import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TP_Ranker {

    private final Indexer indexer;
    private final TP_Estimator estimator;

    public TP_Ranker(Indexer indexer) {
        this.indexer = indexer;
        this.estimator = new TP_Estimator(indexer);
    }

    public double score(String term, int docId) {
        int tf = indexer.getTf(term, docId);
        if (tf == 0) return 0.0;

        TP_Param p = estimator.estimate(term);
        double lambda1 = p.getLambda1();
        double lambda2 = p.getLambda2();
        double pi = p.getPi();

        return Math.log(pi / (1.0 - pi)) + tf * Math.log(lambda1 / lambda2) - (lambda1 - lambda2);
    }

    /**
     * Ranks all documents for the given query terms using the Two-Poisson model.
     *
     * @param queryTerms list of tokenized query terms
     * @return Map of docId -> score, sorted descending (full ranking)
     */
    public Map<Integer, Double> rank(List<String> queryTerms) {
        Map<Integer, Double> scores = new HashMap<>();
        for (Integer docId : indexer.getDocIds()) {
            double total = 0.0;
            for (String term : queryTerms) {
                total += score(term, docId);
            }
            if (total > 0) {
                scores.put(docId, total);
            }
        }
        return TP_Util.sort(scores);
    }

    /**
     * Ranks documents and returns only the top-k results as ScoredDoc list,
     * matching the BIMEngine / BM11Engine style for use with Main and Evaluator.
     *
     * @param query raw query string (will be tokenized)
     * @param topK  number of top results to return
     */
    public List<ScoredDoc> search(String query, int topK) {
        List<String> queryTerms = new Tokenizer().tokenize(query);
        Map<Integer, Double> ranked = rank(queryTerms);

        List<ScoredDoc> result = new ArrayList<>();
        for (Map.Entry<Integer, Double> e : ranked.entrySet()) {
            result.add(new ScoredDoc(e.getKey(), e.getValue()));
            if (result.size() >= topK) break;
        }
        return result;
    }

    /** A document id paired with its Two-Poisson score. */
    public record ScoredDoc(int docId, double score) {}
}