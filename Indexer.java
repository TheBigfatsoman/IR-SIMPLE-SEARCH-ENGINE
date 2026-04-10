import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Indexer {

    private final Map<String, List<Integer>> invertedIndex = new HashMap<>();
    private final CranfieldParser parser = new CranfieldParser();
    private final Tokenizer tokenizer = new Tokenizer();

    public Map<String, List<Integer>> getInvertedIndex() {
        return invertedIndex;
    }
    
    private void invertedIndex() {
        List<Document> documents = parser.parseFile("data/cran.all.1400");

        while (!documents.isEmpty()) {
            Document doc = documents.remove(0);
            tokenizer.tokenize(doc.content()).forEach(token -> {
                invertedIndex.computeIfAbsent(token, k -> new java.util.ArrayList<>()).add(Integer.parseInt(doc.id().substring(4)));
            });
        }
    }

    public static void main(String[] args) {
        Indexer indexer = new Indexer();
        indexer.invertedIndex();
        System.out.println(indexer.getInvertedIndex().toString());
    }
}