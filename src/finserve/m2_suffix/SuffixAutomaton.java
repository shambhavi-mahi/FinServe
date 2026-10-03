package finserve.m2_suffix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Suffix Automaton (DAWG - Directed Acyclic Word Graph) Implementation for FinServe.
 * 
 * Banking Use Case:
 * Efficiently analyzes all unique and repeated substrings across high-velocity transaction data.
 * Ideal for calculating substring statistics, verifying token presence in O(M) time, and 
 * detecting recurring fraudulent payment sub-phrases.
 * 
 * Time Complexity:
 * - Construction: O(N) linear time (where N = text length).
 * - Pattern Search / Substring Existence: O(M) where M = pattern length.
 * - Total Unique Substrings Query: O(1) after construction.
 * Space Complexity:
 * - O(N) states (at most 2N - 1 states and 3N - 4 transitions).
 */
public class SuffixAutomaton {

    public static class State {
        public int len;
        public int link;
        public Map<Character, Integer> next = new HashMap<>();
        public int cnt = 0; // Number of occurrences of substrings ending at this state
        public int firstPos = -1;

        public State(int len, int link) {
            this.len = len;
            this.link = link;
        }
    }

    private final String text;
    private final List<State> states = new ArrayList<>();
    private int last;

    public SuffixAutomaton(String text) {
        this.text = text;
        // Init root state 0
        State root = new State(0, -1);
        states.add(root);
        last = 0;

        if (text != null && !text.isEmpty()) {
            buildAutomaton();
            calculateOccurrences();
        }
    }

    private void buildAutomaton() {
        for (int i = 0; i < text.length(); i++) {
            extend(text.charAt(i), i);
        }
    }

    private void extend(char c, int pos) {
        int cur = states.size();
        State curState = new State(states.get(last).len + 1, -1);
        curState.cnt = 1; // Base occurrence for non-cloned state
        curState.firstPos = pos;
        states.add(curState);

        int p = last;
        while (p != -1 && !states.get(p).next.containsKey(c)) {
            states.get(p).next.put(c, cur);
            p = states.get(p).link;
        }

        if (p == -1) {
            states.get(cur).link = 0;
        } else {
            int q = states.get(p).next.get(c);
            if (states.get(p).len + 1 == states.get(q).len) {
                states.get(cur).link = q;
            } else {
                int clone = states.size();
                State cloneState = new State(states.get(p).len + 1, states.get(q).link);
                cloneState.next = new HashMap<>(states.get(q).next);
                cloneState.cnt = 0; // Cloned state initially has 0 direct occurrences
                cloneState.firstPos = states.get(q).firstPos;
                states.add(cloneState);

                while (p != -1 && states.get(p).next.get(c) == q) {
                    states.get(p).next.put(c, clone);
                    p = states.get(p).link;
                }
                states.get(q).link = clone;
                states.get(cur).link = clone;
            }
        }
        last = cur;
    }

    private void calculateOccurrences() {
        // Sort states by length descending
        Integer[] perm = new Integer[states.size()];
        for (int i = 0; i < states.size(); i++) perm[i] = i;
        Arrays.sort(perm, (a, b) -> Integer.compare(states.get(b).len, states.get(a).len));

        for (int v : perm) {
            if (states.get(v).link != -1) {
                states.get(states.get(v).link).cnt += states.get(v).cnt;
            }
        }
    }

    /**
     * Checks if a pattern exists as a substring in O(M) time.
     */
    public boolean contains(String pattern) {
        return getMatchedState(pattern) != -1;
    }

    /**
     * Returns total occurrences of pattern in text in O(M) time.
     */
    public int countOccurrences(String pattern) {
        int state = getMatchedState(pattern);
        return (state == -1) ? 0 : states.get(state).cnt;
    }

    /**
     * Returns total number of distinct substrings in text in O(1) time.
     */
    public long countUniqueSubstrings() {
        long count = 0;
        for (int i = 1; i < states.size(); i++) {
            State st = states.get(i);
            count += st.len - states.get(st.link).len;
        }
        return count;
    }

    private int getMatchedState(String pattern) {
        if (pattern == null || pattern.isEmpty() || states.isEmpty()) return -1;
        int curr = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (!states.get(curr).next.containsKey(c)) {
                return -1;
            }
            curr = states.get(curr).next.get(c);
        }
        return curr;
    }

    public static SuffixAutomaton buildAutomaton(String text) {
        return new SuffixAutomaton(text);
    }

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("FinServe M2 - Suffix Automaton (DAWG)");
        System.out.println("=========================================");

        String transactionData = "TX1001:ONLINE PAYMENT AMAZON TX1002:ONLINE PAYMENT FLIPKART TX1003:ATM CASH WITHDRAWAL TX1004:ONLINE PAYMENT AMAZON TX1005:INTERNATIONAL PAYMENT";
        System.out.println("Input Transaction Stream:");
        System.out.println(transactionData);
        System.out.println();

        System.out.println("Constructing Suffix Automaton...");
        SuffixAutomaton sa = SuffixAutomaton.buildAutomaton(transactionData);

        System.out.println("Automaton Stats:");
        System.out.println("  Total States: " + sa.states.size());
        System.out.println("  Total Unique Transaction Substrings: " + sa.countUniqueSubstrings());

        String[] testPatterns = {"ONLINE PAYMENT", "AMAZON", "ATM CASH", "BITCOIN"};
        System.out.println("\nPattern Occurrence Queries in O(M) time:");
        for (String pattern : testPatterns) {
            boolean exists = sa.contains(pattern);
            int count = sa.countOccurrences(pattern);
            System.out.println("  Pattern: \"" + pattern + "\" -> Exists: " + exists + ", Count: " + count);
        }
    }
}
