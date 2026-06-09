import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwoPoisson {
    private final Indexer indexer;
    private final Tokenizer tokenizer;
    
    // Standard BM25 parameters derived from the 2-Poisson model
    private final double k1 = 1.2; // Controls TF saturation (usually between 1.2 and 2.0)
    private final double b = 0.75; // Controls document length normalization

    public TwoPoisson(Indexer indexer) {
        this.indexer = indexer;
        this.tokenizer = new Tokenizer(); // Reusing the same Tokenizer from your codebase
    }

    /**
     * Retrieves and ranks documents based on the Two-Poisson (BM25) model.
     * * @param query The raw string query.
     * @param topK The number of top relevant documents to return.
     * @return A list of Map Entries containing DocID and its relevance score, sorted highest to lowest.
     */
    public List<Map.Entry<Integer, Double>> retrieve(String query, int topK) {
        List<String> queryTokens = tokenizer.tokenize(query);
        Map<Integer, Double> docScores = new HashMap<>();
        
        int N = indexer.getN();
        double avgdl = indexer.getAvgdl();

        for (String term : queryTokens) {
            int df = indexer.getDf(term);
            
            // Skip terms that don't exist in the corpus
            if (df == 0) continue; 

            // Calculate IDF for the term (assuming no known relevant documents)
            double idf = Math.log((N - df + 0.5) / (df + 0.5) + 1.0);

            // Fetch the postings list for this term
            Map<Integer, Integer> postings = indexer.getPostings(term);
            
            for (Map.Entry<Integer, Integer> entry : postings.entrySet()) {
                int docId = entry.getKey();
                int tf = entry.getValue();
                int docLen = indexer.getDocLength(docId);

                // Two-Poisson approximation for Term Frequency
                double numerator = tf * (k1 + 1);
                double denominator = tf + k1 * (1 - b + b * (docLen / avgdl));
                double tfScore = numerator / denominator;

                // Final term score for this document
                double termScore = idf * tfScore;

                // Accumulate the score for the document
                docScores.merge(docId, termScore, Double::sum);
            }
        }

        // Sort the documents by score in descending order
        List<Map.Entry<Integer, Double>> sortedScores = new ArrayList<>(docScores.entrySet());
        sortedScores.sort(Map.Entry.<Integer, Double>comparingByValue().reversed());

        // Return only the top K results
        return sortedScores.subList(0, Math.min(topK, sortedScores.size()));
    }

    public static void main(String[] args) {
        // Example usage
        Indexer indexer = new Indexer();
        // Assume indexer is already built with documents and terms
        TwoPoisson twoPoisson = new TwoPoisson(indexer);
        
        String query = "turbulent flow in pipes";
        List<Map.Entry<Integer, Double>> results = twoPoisson.retrieve(query, 10);
        
        System.out.println("Top relevant documents:");
        for (Map.Entry<Integer, Double> entry : results) {
            System.out.println("DocID: " + entry.getKey() + ", Score: " + entry.getValue());
        }
    }
}