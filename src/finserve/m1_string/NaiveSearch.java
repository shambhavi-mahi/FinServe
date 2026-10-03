package finserve.m1_string;

import java.util.ArrayList;
import java.util.List;

/**
 * Naive String Matching Algorithm
 * Banking use case: Basic customer name lookup and matching.
 * Time Complexity: O(N * M)
 * Space Complexity: O(1)
 */
public class NaiveSearch {
    public static List<Integer> search(String text, String pattern) {
        List<Integer> occurrences = new ArrayList<>();
        if (pattern == null || pattern.isEmpty()) return occurrences;
        
        int M = pattern.length();
        int N = text.length();

        for (int i = 0; i <= N - M; i++) {
            int j;
            for (j = 0; j < M; j++) {
                if (text.charAt(i + j) != pattern.charAt(j)) {
                    break;
                }
            }
            if (j == M) {
                occurrences.add(i);
            }
        }
        return occurrences;
    }
}
