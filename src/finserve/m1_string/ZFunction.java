package finserve.m1_string;

import java.util.ArrayList;
import java.util.List;

/**
 * Z-Function Algorithm
 * Banking use case: Find repeated patterns in transaction descriptions.
 * Time Complexity: O(N + M)
 * Space Complexity: O(N + M)
 */
public class ZFunction {
    public static List<Integer> search(String text, String pattern) {
        List<Integer> occurrences = new ArrayList<>();
        if (pattern == null || pattern.length() == 0) return occurrences;
        
        String concat = pattern + "$" + text;
        int l = concat.length();
        int[] z = new int[l];
        
        int left = 0, right = 0;
        for (int i = 1; i < l; ++i) {
            if (i <= right) {
                z[i] = Math.min(right - i + 1, z[i - left]);
            }
            while (i + z[i] < l && concat.charAt(z[i]) == concat.charAt(i + z[i])) {
                z[i]++;
            }
            if (i + z[i] - 1 > right) {
                left = i;
                right = i + z[i] - 1;
            }
        }
        
        for (int i = 0; i < l; ++i) {
            if (z[i] == pattern.length()) {
                occurrences.add(i - pattern.length() - 1);
            }
        }
        return occurrences;
    }

    public static void main(String[] args) {
        String text = "ONLINE PAYMENT AMAZON ONLINE PAYMENT FLIPKART";
        String pattern = "ONLINE";
        System.out.println("Z-Function Match indices for '" + pattern + "': " + search(text, pattern));
    }
}
