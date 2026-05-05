import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Indexer {

    private final Map<String, List<Integer>> invertedIndex = new HashMap<>();
    private final CranfieldParser parser = new CranfieldParser();
    private final Tokenizer tokenizer = new Tokenizer();

    public Map<String, List<Integer>> getInvertedIndex() {
        return invertedIndex;
    }
    
    public void invertedIndex() {
        // Path to the Cranfield dataset
        List<Document> documents = parser.parseFile("data/cran.all.1400"); 

        if (documents.isEmpty()) {
            System.out.println("Warning: No documents were parsed. Check the file path and format.");
            return;
        }

        while (!documents.isEmpty()) {
            Document doc = documents.remove(0);
            int docId = Integer.parseInt(doc.id().substring(4));
            
            // Use a Set to get unique tokens for THIS document
            Set<String> uniqueTokens = new HashSet<>(tokenizer.tokenize(doc.content()));
            
            uniqueTokens.forEach(token -> {
                invertedIndex.computeIfAbsent(token, k -> new ArrayList<>()).add(docId);
            });
        }
    }

    public static void main(String[] args) {
        Indexer indexer = new Indexer();
        indexer.invertedIndex();
        System.out.println("Vocabulary size: " + indexer.getInvertedIndex().size());
        // Print sample
        indexer.getInvertedIndex().entrySet().stream().limit(5).forEach(System.out::println);
    }
}
