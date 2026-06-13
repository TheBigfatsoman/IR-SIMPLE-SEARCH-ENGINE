import java.util.List;

/**
 * A generic "search" function used by Evaluator to evaluate any ranking model.
 *
 * Implementations should return the top-k ranked documents for a given query
 * as a list of (docId, score) pairs ordered by descending score.
 *
 * Usage with method references / lambdas in Main.java:
 *
 *   ScoredSearch bimSearch  = (query, topK) -> toScoredList(bimEngine.search(query, topK));
 *   ScoredSearch bm25Search = (query, topK) -> toScoredList(bm25.search(query, topK));
 *   ScoredSearch tpSearch   = (query, topK) -> toScoredList(tpRanker.search(query, topK));
 *
 * Each engine's own record type (BIMEngine.ScoredDoc, BM25.ScoredDoc, TP_Ranker.ScoredDoc,
 * BM11Engine.ScoredDoc) can be converted via Evaluator.ScoredDoc(docId, score).
 */
@FunctionalInterface
public interface ScoredSearch {
    List<Evaluator.ScoredDoc> search(String query, int topK);
}