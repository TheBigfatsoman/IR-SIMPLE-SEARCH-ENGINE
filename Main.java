import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing system and building Inverted Index...");
        
        // 1. Initialize Peran 1's Indexer
        Indexer indexer = new Indexer();
        indexer.invertedIndex(); // Make sure Peran 1 changes this method to public
        System.out.println("Documents loaded: " + indexer.getInvertedIndex().size());
        
        // 2. Extract the dictionary (all unique terms) from the inverted index
        Set<String> dictionary = indexer.getInvertedIndex().keySet();
        System.out.println("Index built successfully! Vocabulary size: " + dictionary.size() + " terms.");

        Scanner sc = new Scanner(System.in);
        
        while (true) {
            System.out.print("""
                    \nList Command:
                    1. Lihat Isi Inverted Index
                    2. BOOLEAN QUERY (dengan Tolerant Search)
                    3. Exit
                    Input Command: 
                    """);
            
            int command = sc.nextInt();
            sc.nextLine(); // consume the newline character

            switch (command) {
                case 1:
                    System.out.println("Menampilkan Inverted Index (Sample):");
                    // Print just a few to avoid flooding the console
                    indexer.getInvertedIndex().entrySet().stream()
                           .limit(10)
                           .forEach(entry -> System.out.println(entry.getKey() + " -> " + entry.getValue()));
                    break;
                case 2:
                    System.out.print("Masukkan Query: ");
                    String rawQuery = sc.nextLine();
                    
                    // 3. Pass the raw query through Peran 3's Tolerant Search
                    String correctedQuery = TolerantSearch.processQuery(rawQuery, dictionary);
                    
                    System.out.println("Original Query  : " + rawQuery);
                    System.out.println("Corrected Query : " + correctedQuery);
                    
                    // TODO: Pass the correctedQuery to Peran 2's Boolean Engine here
                    break;
                case 3:
                    System.out.println("Exiting...");
                    sc.close();
                    return;
                default:
                    System.out.println("Command tidak ditemukan");
                    break;
            }
        }
    }
}