import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Tokenizer {
    private static final Set<String> STOP_WORDS = Set.of(
        "a", "an", "the", "and", "are", "as", "at", "be", "but", "by", 
        "for", "if", "in", "into", "is", "it", "no", "not", "of", "on", 
        "or", "such", "that", "their", "then", "there", "these", 
        "they", "this", "to", "was", "will", "with", ".", ",", "!", "?", ";", ":", 
        "-", "_", "(", ")", "[", "]", "{", "}", "\"", "'"
    );

    /**
     * Tokenizes the input text into a list of tokens, removing stop words and punctuation.
     * @param text sentences to be tokenized
     * @return a list of strings of tokens
     */
    public List<String> tokenize(String text) {
        if (text == null) {
            return Collections.emptyList();
        }

        String[] tokens = text.toLowerCase().split("\\s+");
        return Arrays.stream(tokens)
                     .filter(token -> !token.isEmpty())
                     .filter(token -> !STOP_WORDS.contains(token))
                     .collect(Collectors.toList());
    }
}
