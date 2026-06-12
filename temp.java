import java.util.List;
import java.util.Map;

public class temp {
        public static void main(String[] args) {
                Indexer indexer = new Indexer();
                indexer.invertedIndex();
        
                BM25 bm25 =
                        new BM25(indexer);
        
                bm25.setK1(1.5);
                bm25.setB(0.75);
        
                Map<Integer, Double> bm25Result =
                        bm25.rank(
                                List.of(
                                        "boundary",
                                        "layer",
                                        "flow"
                                )
                        );
                
                        System.out.println(bm25Result.toString());
                
                TP_Ranker tp =
                        new TP_Ranker(indexer);
                
                Map<Integer, Double> tpResult =
                        tp.rank(
                                List.of(
                                        "boundary",
                                        "layer",
                                        "flow"
                                )
                        );
                            // System.out.println(tpResult.toString());
                }
}