import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TP_Ranker {

    private final Indexer indexer;
    private final double k = 1.5; // Konstanta k sesuai aturan slide (1 <= k < 2)

    public TP_Ranker(Indexer indexer) {
        this.indexer = indexer;
    }

    public double score(String term, int docId) {
        int tf = indexer.getTf(term, docId);
        if (tf == 0) return 0.0;

        int N = indexer.getN();
        int df = indexer.getDf(term);

        // Menggunakan standard IDF sebagai w_t (bobot term)
        double wt = Math.log((N - df + 0.5) / (df + 0.5) + 1.0);

        // Rumus sesuai Slide Two Poisson Model (2):
        // rel = (f_{t,D} * (k + 1) * w_t) / (f_{t,D} + k)
        return (tf * (k + 1) * wt) / (tf + k);
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
        return TP_Util.sort(scores);
    }

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

    public record ScoredDoc(int docId, double score) {}
}