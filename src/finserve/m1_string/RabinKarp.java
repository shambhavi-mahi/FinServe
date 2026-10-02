package finserve.m1_string;

import java.util.ArrayList;
import java.util.List;

/**
 * Rabin-Karp Algorithm
 * Banking use case: Search financial/payment codes.
 * Time Complexity: O(N + M) average, O(N * M) worst case.
 * Space Complexity: O(1)
 */
public class RabinKarp {
    public static final int d = 256;
    public static final int q = 101; // prime number

    public static List<Integer> search(String text, String pattern) {
        List<Integer> occurrences = new ArrayList<>();
        int M = pattern.length();
        int N = text.length();
        if (M == 0 || N < M) return occurrences;
        
        int i, j;
        int p = 0; // hash for pattern
        int t = 0; // hash for text
        int h = 1;
        
        for (i = 0; i < M - 1; i++) {
            h = (h * d) % q;
        }
        
        for (i = 0; i < M; i++) {
            p = (d * p + pattern.charAt(i)) % q;
            t = (d * t + text.charAt(i)) % q;
        }
        
        for (i = 0; i <= N - M; i++) {
            if (p == t) {
                for (j = 0; j < M; j++) {
                    if (text.charAt(i + j) != pattern.charAt(j)) {
                        break;
                    }
                }
                if (j == M) {
                    occurrences.add(i);
                }
            }
            if (i < N - M) {
                t = (d * (t - text.charAt(i) * h) + text.charAt(i + M)) % q;
                if (t < 0) {
                    t = (t + q);
                }
            }
        }
        return occurrences;
    }

    public static void main(String[] args) {
        String text = "ONLINE PAYMENT AMAZON ONLINE PAYMENT FLIPKART";
        String pattern = "AMAZON";
        System.out.println("Rabin-Karp Match indices for '" + pattern + "': " + search(text, pattern));
    }
}
