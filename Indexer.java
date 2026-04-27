import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Indexer {

    private final CranfieldParser parser = new CranfieldParser();
    private final Tokenizer tokenizer = new Tokenizer();
    
    public void invertedIndex() {
        // Change this path to match your file's location
        // If it's in the root folder, use "cran.all.1400"
        // If it's in a folder named 'data', use "data/cran.all.1400"
        List<Document> documents = parser.parseFile("data/cran.all.1400"); 
        System.out.println(new File("data/").getAbsolutePath());

        if (documents.isEmpty()) {
            System.out.println("Warning: No documents were parsed. Check the file path and format.");
        }

        while (!documents.isEmpty()) {
            Document doc = documents.remove(0);
            tokenizer.tokenize(doc.content()).forEach(token -> {
                // Note: substring(4) takes "1" from "Doc-1"
                invertedIndex.computeIfAbsent(token, k -> new java.util.ArrayList<>())
                            .add(Integer.parseInt(doc.id().substring(4)));
            });
        }

        return invertedIndex;
    }
}