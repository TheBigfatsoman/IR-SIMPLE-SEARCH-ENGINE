import java.util.*;

public class BM11Engine {
    private final Indexer indexer;
    private final double k;

    public record ScoredDoc(int docId, double score) {}

    public BM11Engine(Indexer indexer, double k) {
        this.indexer = indexer;
        this.k = k;
    }

    // Default constructor dengan nilai k yang umum = 1.5
    public BM11Engine(Indexer indexer) {
        this(indexer, 1.5);
    }

    public List<ScoredDoc> search(String query, int topK) {
        List<String> queryTerms = new Tokenizer().tokenize(query);
        int N = indexer.getN();
        double lAvg = indexer.getAvgdl();

        // R dan r_t diasumsikan 0 karena tidak ada informasi relevance feedback awal
        // (ad-hoc retrieval)
        int R = 0;
        int rt = 0;

        // Menghitung bobot term (w_t)
        Map<String, Double> termWeights = new HashMap<>();
        for (String term : queryTerms) {
            if (termWeights.containsKey(term))
                continue;

            int df = indexer.getDf(term);
            if (df == 0)
                continue;

            // Menggunakan rumus dari slide:
            // w_t = log10( ((r_t + 0.5) * (N - R + 1)) / ((R + 1) * (N_t - r_t + 0.5)) )
            double pembilang = (rt + 0.5) * (N - R + 1);
            double penyebut = (R + 1.0) * (df - rt + 0.5);

            double wt = Math.log10(pembilang / penyebut);

            // Jika nilai wt menjadi negatif (karena kata sangat umum, df > separuh N),
            // beri batas bawah 0 agar tidak mengurangi skor.
            if (wt < 0)
                wt = 0;

            termWeights.put(term, wt);
        }

        // Mengakumulasi skor per dokumen
        Map<Integer, Double> scores = new HashMap<>();
        for (Map.Entry<String, Double> tw : termWeights.entrySet()) {
            String term = tw.getKey();
            double wt = tw.getValue();

            // Jika bobot 0, lewati
            if (wt == 0)
                continue;

            for (Map.Entry<Integer, Integer> posting : indexer.getPostings(term).entrySet()) {
                int docId = posting.getKey();
                int ftD = posting.getValue(); // frekuensi term dalam dokumen

                int ld = indexer.getDocLength(docId); // panjang dokumen tersebut

                // Rumus BM11: (f_{t,D} * (k + 1) * w_t) / (f_{t,D} + (k * l_d) / l_{avg})
                double pembilang = ftD * (k + 1) * wt;
                double penyebut = ftD + (k * ld) / lAvg;

                double termScore = pembilang / penyebut;

                // Akumulasi skor (implementasi Sigma penjumlahan secara tradisional)
                if (scores.containsKey(docId)) {
                    double skorLama = scores.get(docId);
                    scores.put(docId, skorLama + termScore);
                } else {
                    scores.put(docId, termScore);
                }
            }
        }

        // Sort descending berdasar skor
        List<ScoredDoc> ranked = new ArrayList<>(scores.size());
        for (Map.Entry<Integer, Double> e : scores.entrySet()) {
            ranked.add(new ScoredDoc(e.getKey(), e.getValue()));
        }
        ranked.sort(new Comparator<ScoredDoc>() {
            @Override
            public int compare(ScoredDoc d1, ScoredDoc d2) {
                // Urutkan dari skor tertinggi ke terendah (descending)
                return Double.compare(d2.score(), d1.score());
            }
        });

        return ranked.subList(0, Math.min(topK, ranked.size()));
    }
}
