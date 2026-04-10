import java.io.*;
import java.util.*;

public class DictionaryLoader {

    public static Set<String> loadDictionary(String filePath) {
        Set<String> dictionary = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {

                // lowercase + remove symbols
                line = line.toLowerCase().replaceAll("[^a-z ]", " ");

                String[] words = line.split("\\s+");

                for (String word : words) {
                    if (!word.isEmpty()) {
                        dictionary.add(word);
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        return dictionary;
    }
}