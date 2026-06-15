import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BM25 {

    private final Indexer indexer;
    private double k1 = 1.2; // Parameter k dari Two-Poisson / BM
    private double b = 0.75; // Nilai b default yang umum dipakai sesuai slide

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

        // w_t menggunakan standard Robertson-Spärck Jones / BM25 IDF
        double wt = Math.log((N - df + 0.5) / (df + 0.5) + 1.0);

        // Rumus penyebut eksak sesuai slide Intro (2) BM25:
        // f_{t,D} + ( (k1 * l_d) / l_avg ) * b + k1 * (1 - b)
        double bm11Component = ((k1 * dl) / avgdl) * b;
        double twoPoissonComponent = k1 * (1 - b);
        double norm = tf + bm11Component + twoPoissonComponent;

        // Pembilang: f_{t,D} * (k1 + 1) * w_t
        return (tf * (k1 + 1) * wt) / norm;
    }

    public Map<Integer, Double> rank(List<String> queryTerms) {
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

    private Map<Integer, Double> sort(Map<Integer, Double> scores) {
        List<Map.Entry<Integer, Double>> entries = new ArrayList<>(scores.entrySet());
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        Map<Integer, Double> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public record ScoredDoc(int docId, double score) {}
}