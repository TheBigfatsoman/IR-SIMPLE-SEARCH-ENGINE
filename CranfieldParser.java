import java.io.*;
import java.util.*;

record Document(String id, String content) {}

public class CranfieldParser {

    /**
     * Parses a Cranfield-style file and returns a list of documents.
     *
     * @param filePath The path to the file to parse.
     * @return A list of Document objects.
     */
    public List<Document> parseFile(String filePath) {
        List<Document> documents = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentId = "";
            StringBuilder contentBuilder = new StringBuilder();
            String currentTag = "";

            while ((line = reader.readLine()) != null) {
                // check new tag
                if (line.startsWith(".")) {
                    String tag = line.substring(0, 2);
                    
                    if (tag.equals(".I")) {
                        // save document
                        if (!currentId.isEmpty()) {
                            documents.add(new Document("Doc-" + currentId, contentBuilder.toString()));
                        }
                        // start a new document
                        currentId = line.substring(3).trim();
                        contentBuilder = new StringBuilder();
                    }
                    currentTag = tag;
                } else {
                    // Title (.T), Words (.W)
                    if (currentTag.equals(".T") || currentTag.equals(".W")) {
                        contentBuilder.append(line).append(" ");
                    }
                }
            }
            // Add the last document in the file
            if (!currentId.isEmpty()) {
                documents.add(new Document("Doc-" + currentId, contentBuilder.toString()));
            }

        } catch (IOException e) {
            System.err.println("Error reading the 1400 file: " + e.getMessage());
        }

        return documents;
    }
}