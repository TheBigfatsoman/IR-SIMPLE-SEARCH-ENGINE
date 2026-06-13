import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TP_Util {
    public static double factorial(int n) {
        double f = 1.0;

        for (int i = 2; i <= n; i++) {
            f *= i;
        }

        return f;
    } 

    public static Map<Integer, Double> sort(Map<Integer, Double> scores) {
        List<Map.Entry<Integer, Double>> entries = new ArrayList<>(scores.entrySet());

        entries.sort((a,b) -> Double.compare(b.getValue(), a.getValue()));

        Map<Integer, Double> result = new LinkedHashMap<>();

        for (Map.Entry<Integer, Double> e : entries) {
            result.put(e.getKey(), e.getValue());
        }

        return result;
    }

    public static double poisson(int k, double lambda) {
        return Math.exp(-lambda) * Math.pow(lambda, k) / factorial(k);
    }
}