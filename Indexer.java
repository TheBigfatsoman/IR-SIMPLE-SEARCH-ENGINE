import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Indexer {

    private final CranfieldParser parser = new CranfieldParser();
    private final Tokenizer tokenizer = new Tokenizer();
    
    /**
     * Builds an inverted index from the documents in the specified file.
     * @param filePath The path to the file containing the documents to index.
     * @return A map where the keys are tokens and the values are lists of document IDs that contain those tokens.
     */
    public Map<String, List<Integer>> invertedIndex(String filePath) {
        Map<String, List<Integer>> invertedIndex = new HashMap<>();
        List<Document> documents = parser.parseFile(filePath);

        while (!documents.isEmpty()) {
            Document doc = documents.remove(0);
            tokenizer.tokenize(doc.content()).forEach(token -> {
                invertedIndex.computeIfAbsent(token, k -> new java.util.ArrayList<>()).add(Integer.parseInt(doc.id().substring(4)));
            });
        }

        return invertedIndex;
    }
}