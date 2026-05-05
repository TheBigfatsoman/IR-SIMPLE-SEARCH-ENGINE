import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class BooleanEngine {

    private final Map<String, List<Integer>> invertedIndex;
    private final String query;

    public BooleanEngine(
        Map<String, List<Integer>> invertedIndex,
        String query
    ) {
        this.invertedIndex = invertedIndex;
        this.query = query;
    }

    private List<Integer> execute() {
        Stack<List<Integer>> operandStack = new Stack<>();
        Stack<String> operatorStack = new Stack<>();

        // Split by spaces OR at the boundaries of ( and )
        String[] terms = query.split("(?=[()])|(?<=[()])|\\s+");

        for (String term : terms) {
            if (
                term.equalsIgnoreCase("AND") ||
                term.equalsIgnoreCase("OR") ||
                term.equalsIgnoreCase("NOT")
            ) {
                while (
                    !operatorStack.isEmpty() &&
                    getPriority(operatorStack.peek()) >=
                    getPriority(term.toUpperCase())
                ) {
                    processOperator(operandStack, operatorStack.pop());
                }
                operatorStack.push(term.toUpperCase());
            } else if (term.equals("(")) {
                operatorStack.push("(");
            } else if (term.equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    processOperator(operandStack, operatorStack.pop());
                }
                if (!operatorStack.isEmpty()) operatorStack.pop(); // remove "("
            } else {

                // It's a word
                List<Integer> list = invertedIndex.getOrDefault(
                    term.toLowerCase(),
                    new java.util.ArrayList<>()
                );
                operandStack.push(list);
            }
        }

        while (!operatorStack.isEmpty()) {
            if (operatorStack.peek().equals("(")) {
                operatorStack.pop();
                continue;
            }
            processOperator(operandStack, operatorStack.pop());
        }

        return operandStack.isEmpty()
            ? new java.util.ArrayList<>()
            : operandStack.pop();
    }

    private void processOperator(Stack<List<Integer>> operandStack, String operator) {
        if (operandStack.isEmpty()) return;
        if (operandStack.size() < 2 && !operator.equals("NOT")) return;

        List<Integer> right = operandStack.pop();
        List<Integer> left = (operator.equals("NOT") && operandStack.isEmpty()) ? new java.util.ArrayList<>() : operandStack.pop();


        switch (operator) {
            case "AND":
                operandStack.push(and(left, right));
                break;
            case "OR":
                operandStack.push(or(left, right));
                break;
            case "NOT":
                operandStack.push(not(left, right));
                break;
        }
    }

    private List<Integer> and(List<Integer> l1, List<Integer> l2) {
        List<Integer> result = new java.util.ArrayList<>();
        int i = 0,
            j = 0;
        while (i < l1.size() && j < l2.size()) {
            if (l1.get(i).equals(l2.get(j))) {
                result.add(l1.get(i));
                i++;
                j++;
            } else if (l1.get(i) < l2.get(j)) {
                i++;
            } else {
                j++;
            }
        }
        return result;
    }

    private List<Integer> or(List<Integer> l1, List<Integer> l2) {
        List<Integer> result = new java.util.ArrayList<>();
        int i = 0,
            j = 0;
        while (i < l1.size() && j < l2.size()) {
            if (l1.get(i).equals(l2.get(j))) {
                result.add(l1.get(i));
                i++;
                j++;
            } else if (l1.get(i) < l2.get(j)) {
                result.add(l1.get(i));
                i++;
            } else {
                result.add(l2.get(j));
                j++;
            }
        }
        while (i < l1.size()) result.add(l1.get(i++));
        while (j < l2.size()) result.add(l2.get(j++));
        return result;
    }

    private List<Integer> not(List<Integer> l1, List<Integer> l2) {
        List<Integer> result = new java.util.ArrayList<>();
        int i = 0,
            j = 0;
        while (i < l1.size() && j < l2.size()) {
            if (l1.get(i).equals(l2.get(j))) {
                i++;
                j++;
            } else if (l1.get(i) < l2.get(j)) {
                result.add(l1.get(i));
                i++;
            } else {
                j++;
            }
        }
        while (i < l1.size()) result.add(l1.get(i++));
        return result;
    }

    public List<Integer> getResults() {
        return execute();
    }

    private int getPriority(String op) {
        switch (op) {
            case "NOT":
                return 3;
            case "AND":
                return 2;
            case "OR":
                return 1;
            default:
                return 0;
        }
    }
}
