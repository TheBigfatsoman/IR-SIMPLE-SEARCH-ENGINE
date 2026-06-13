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
     * @param queryTerms list of query to rank
     * @return Map of the document number to it's score sorted high to low
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
}
