import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Indexer {

    // From Tugas 1
    private final Map<String, List<Integer>> invertedIndex = new HashMap<>();
    private final CranfieldParser parser = new CranfieldParser();
    private final Tokenizer tokenizer = new Tokenizer();

    public Map<String, List<Integer>> getInvertedIndex() {
        return invertedIndex;
    }

    //For Tugas 2
    private final Map<String, Map<Integer,Integer>> tfIndex = new HashMap<>();
    private final Map<String, Integer> df = new HashMap<>();
    private final Map<Integer, Integer> docLength = new HashMap<>();
    private final List<Integer> docIds = new ArrayList<>();

    private double avgdl = 0.0;
    private int N = 0;

    // Getters
    public int getTf(String term, int docId) {
        return tfIndex.getOrDefault(term, Collections.emptyMap()).getOrDefault(docId, 0);
    }
 
    public Map<Integer, Integer> getPostings(String term) {
        return tfIndex.getOrDefault(term, Collections.emptyMap());
    }
 
    public int getDf(String term) {
        return df.getOrDefault(term, 0);
    }
 
    public int getDocLength(int docId) {
        return docLength.getOrDefault(docId, 0);
    }
 
    public double getAvgdl() { 
        return avgdl; 
    }
 
    public int getN() { 
        return N; 
    }
 
    public List<Integer> getDocIds() { 
        return Collections.unmodifiableList(docIds); 
    }
 
    public Set<String> getVocabulary() { 
        return tfIndex.keySet(); 
    }
    
    public void invertedIndex() {
        // Path to the Cranfield dataset
        List<Document> documents = parser.parseFile("data/cran.all.1400"); 

        if (documents.isEmpty()) {
            System.out.println("Warning: No documents were parsed. Check the file path and format.");
            return;
        }

        long totalTokens = 0;

        for (Document doc : documents) {
            // parse numeric id from "Doc-XXXX"
            int docId = Integer.parseInt(doc.id().substring(4));
            docIds.add(docId);
 
            List<String> tokens = tokenizer.tokenize(doc.content());
            int len = tokens.size();
            docLength.put(docId, len);
            totalTokens += len;
 
            // count tf per term in this document
            Map<String, Integer> termCounts = new HashMap<>();
            for (String token : tokens) {
                termCounts.merge(token, 1, Integer::sum);
            }
 
            // update tfIndex, df, and the boolean invertedIndex
            for (Map.Entry<String, Integer> e : termCounts.entrySet()) {
                String term = e.getKey();
                int    freq  = e.getValue();
 
                // tfIndex
                tfIndex.computeIfAbsent(term, k -> new HashMap<>())
                        .put(docId, freq);
 
                // df
                df.merge(term, 1, Integer::sum);
 
                // boolean index (Task 1 compat)
                invertedIndex.computeIfAbsent(term, k -> new ArrayList<>())
                             .add(docId);
            }

            N = docIds.size();

            if(N == 0){
                avgdl = 0.0;
            }
            else{
                avgdl = (double) totalTokens / N;
            }
        }

    }

}
