import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

        return sort(scores);
    }

    private Map<Integer, Double> sort(Map<Integer, Double> scores) {
        List<Map.Entry<Integer, Double>> entries = new ArrayList<>(scores.entrySet());

        entries.sort((a,b) -> Double.compare(b.getValue(), a.getValue()));

        Map<Integer, Double> result = new LinkedHashMap<>();

        for (Map.Entry<Integer, Double> e : entries) {
            result.put(e.getKey(), e.getValue());
        }

        return result;
    }
}
