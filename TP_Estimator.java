import java.util.Map;

public class TP_Estimator {
    private final Indexer indexer;

    public TP_Estimator(Indexer indexer) {
        this.indexer = indexer;
    }

    public TP_Param estimate(String term) {
        Map<Integer, Integer> postings = indexer.getPostings(term);

        int n = indexer.getN();

        if (postings.isEmpty()) {
            return new TP_Param(0.01, 0.001, 0.0001);
        }

        int df = postings.size();

        int tfTotal = 0;

        for (int tf : postings.values()) tfTotal += tf;

        double lambda1 = (double) tfTotal / df;
        double lambda2 = (double) tfTotal / n;
        double pi = (double) df / n;

        return new TP_Param(lambda1, lambda2, pi);
    }
}
