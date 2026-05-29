import java.util.HashMap;
import java.util.Map;

public class TwoPoisson {
    private static class TermParameters {
        double lambda1; // For relevant documents
        double lambda2; // For non-relevant documents
        double pi;

        public TermParameters(double lambda1, double lambda2, double pi) {
            this.lambda1 = lambda1;
            this.lambda2 = lambda2;
            this.pi = pi;
        }
    }

    private final Map<String, TermParameters> modelParams = new HashMap<>();
    
    /**
     * Computes the parameters for the Two-Poisson model based on the indexer.
     * @param indexer an already populated Indexer instance
     * @return void
     */
    public void computeModelParameters(Indexer indexer) {
        int n = indexer.getN();

        for (String term : indexer.getVocabulary()) {
            Map<Integer, Integer> postings = indexer.getPostings(term);
            int df = indexer.getDf(term);
        }
    }

    public void search(String query) {

    }

    private void calcProbability() {

    }
}
