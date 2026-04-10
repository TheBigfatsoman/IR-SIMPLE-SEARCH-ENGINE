import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Indexer {

    private final Map<String, List<Integer>> invertedIndex = new HashMap<>();

    private void tokenization() {

    }
    
    private void indexDocument() {

    }

    public static void main(String[] args) {
        CranfieldParser parser = new CranfieldParser();
        List<Document> docs = parser.parseFile("data/cran.all.1400");
        System.out.println("Successfully parsed " + docs.size() + " documents.");
        
        // Show the first document's content
        if (!docs.isEmpty()) {
            System.out.println("Sample Text: " + docs.get(3).content().substring(0, 100) + "...");
        }
    }
}