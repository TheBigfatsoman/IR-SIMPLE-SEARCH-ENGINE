import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TP_Util {
    public static Map<Integer, Double> sort(Map<Integer, Double> scores) {
        List<Map.Entry<Integer, Double>> entries = new ArrayList<>(scores.entrySet());

        entries.sort((a,b) -> Double.compare(b.getValue(), a.getValue()));

        Map<Integer, Double> result = new LinkedHashMap<>();

        for (Map.Entry<Integer, Double> e : entries) {
            result.put(e.getKey(), e.getValue());
        }

        return result;
    }
}