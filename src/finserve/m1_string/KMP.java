package finserve.m1_string;

import java.util.ArrayList;
import java.util.List;

/**
 * KMP Algorithm
 * Banking use case: Search transaction descriptions/payment codes.
 * Time Complexity: O(N + M) where N is text length and M is pattern length.
 * Space Complexity: O(M) for the LPS array.
 */
public class KMP {
    public static List<Integer> search(String text, String pattern) {
        List<Integer> occurrences = new ArrayList<>();
        if (pattern == null || pattern.length() == 0) return occurrences;
        
        int[] lps = computeLPS(pattern);
        int i = 0; // index for text
        int j = 0; // index for pattern
        
        while (i < text.length()) {
            if (pattern.charAt(j) == text.charAt(i)) {
                j++;
                i++;
            }
            if (j == pattern.length()) {
                occurrences.add(i - j);
                j = lps[j - 1];
            } else if (i < text.length() && pattern.charAt(j) != text.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
        return occurrences;
    }
    
    private static int[] computeLPS(String pattern) {
        int[] lps = new int[pattern.length()];
        int length = 0;
        int i = 1;
        lps[0] = 0;
        
        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(length)) {
                length++;
                lps[i] = length;
                i++;
            } else {
                if (length != 0) {
                    length = lps[length - 1];
                } else {
                    lps[i] = length;
                    i++;
                }
            }
        }
        return lps;
    }

    public static void main(String[] args) {
        String text = "ONLINE PAYMENT AMAZON ONLINE PAYMENT FLIPKART";
        String pattern = "ONLINE PAYMENT";
        System.out.println("KMP Match indices for '" + pattern + "': " + search(text, pattern));
    }
}
