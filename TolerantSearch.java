import java.util.*;

public class TolerantSearch {

    // ==============================
    // Levenshtein Distance
    // ==============================
    public static int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                        dp[i - 1][j - 1], // replace
                        Math.min(
                            dp[i - 1][j],   // delete
                            dp[i][j - 1]    // insert
                        )
                    );
                }
            }
        }

        return dp[a.length()][b.length()];
    }

    // ==============================
    // Suggest closest word
    // ==============================
    public static String correctWord(String input, Set<String> dictionary) {

        // 1. Exact match
        if (dictionary.contains(input)) return input;

        // 2. Prefix match (NEW)
        for (String word : dictionary) {
            if (word.startsWith(input)) {
                return word;
            }
        }

        // 3. Levenshtein fallback
        String bestWord = input;
        int minDist = Integer.MAX_VALUE;

        for (String word : dictionary) {
            int dist = levenshtein(input, word);

            if (dist < minDist) {
                minDist = dist;
                bestWord = word;
            }
        }

        if (minDist <= 2) return bestWord;

        return input;
    }

    // ==============================
    // Process full query
    // ==============================
    public static String processQuery(String query, Set<String> dictionary) {
        StringBuilder result = new StringBuilder();

        String[] parts = query.split(" ");

        for (String part : parts) {

            // Keep boolean operators unchanged
            if (part.equalsIgnoreCase("AND") ||
                part.equalsIgnoreCase("OR") ||
                part.equalsIgnoreCase("NOT")) {

                result.append(part.toUpperCase()).append(" ");
            }

            // Handle wildcard
            else if (part.contains("*")) {
                List<String> matches = wildcardSearch(part, dictionary);

                if (!matches.isEmpty()) {
                    // Convert to OR query: (word1 OR word2 ...)
                    result.append("(");
                    for (int i = 0; i < matches.size(); i++) {
                        result.append(matches.get(i));
                        if (i < matches.size() - 1) {
                            result.append(" OR ");
                        }
                    }
                    result.append(") ");
                } else {
                    result.append(part).append(" ");
                }
            }

            // Normal word → correct it
            else {
                String lower = part.toLowerCase();

                // 1. Exact match
                if (dictionary.contains(lower)) {
                    result.append(lower).append(" ");
                }

                // 2. Prefix matches (NEW)
                else {
                    List<String> matches = getPrefixMatches(lower, dictionary);

                    if (matches.size() > 1) {
                        // multiple → (a OR b OR c)
                        result.append("(");
                        for (int i = 0; i < matches.size(); i++) {
                            result.append(matches.get(i));
                            if (i < matches.size() - 1) {
                                result.append(" OR ");
                            }
                        }
                        result.append(") ");
                    }

                    else if (matches.size() == 1) {
                        // single → just use it
                        result.append(matches.get(0)).append(" ");
                    }

                    // 3. Fallback → Levenshtein
                    else {
                        String fixed = correctWord(lower, dictionary);
                        result.append(fixed).append(" ");
                    }
                }
            }
        }

        return result.toString().trim();
    }

    // ==============================
    // Wildcard search (prefix-based)
    // ==============================
    public static List<String> wildcardSearch(String pattern, Set<String> dictionary) {
        String prefix = pattern.replace("*", "").toLowerCase();
        List<String> results = new ArrayList<>();

        for (String word : dictionary) {
            if (word.startsWith(prefix)) {
                results.add(word);
            }
        }

        return results;
    }

    public static List<String> getPrefixMatches(String input, Set<String> dictionary) {
        List<String> matches = new ArrayList<>();

        for (String word : dictionary) {
            if (word.startsWith(input)) {
                matches.add(word);
            }
        }

        return matches;
    }
}