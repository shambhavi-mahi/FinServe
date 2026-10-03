package finserve.m2_suffix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * LCP (Longest Common Prefix) / Kasai's Algorithm for FinServe Banking Analytics.
 * 
 * Banking Use Case:
 * Identifies repeated patterns, recurring vendor descriptions, and potential duplicate/fraudulent
 * transactions across transaction logs by analyzing longest common prefixes.
 * 
 * Time Complexity:
 * - Construction (Kasai): O(N) linear time (where N = text length).
 * - Pattern Analysis: O(N).
 * Space Complexity:
 * - O(N) for rank array and LCP array storage.
 */
public class LCP {

    /**
     * Builds the LCP (Longest Common Prefix) array using Kasai's Algorithm in O(N) time.
     * 
     * @param text        The input transaction text.
     * @param suffixArray The corresponding suffix array for the text.
     * @return Array lcp where lcp[i] is the length of LCP between suffixArray[i] and suffixArray[i-1].
     */
    public static int[] buildLCP(String text, int[] suffixArray) {
        if (text == null || suffixArray == null || text.length() == 0) {
            return new int[0];
        }

        int n = text.length();
        int[] lcp = new int[n];
        int[] rank = new int[n];

        for (int i = 0; i < n; i++) {
            rank[suffixArray[i]] = i;
        }

        int h = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int j = suffixArray[rank[i] - 1];
                while (i + h < n && j + h < n && text.charAt(i + h) == text.charAt(j + h)) {
                    h++;
                }
                lcp[rank[i]] = h;
                if (h > 0) {
                    h--;
                }
            } else {
                lcp[0] = 0;
            }
        }
        return lcp;
    }

    /**
     * Finds the longest repeated pattern in the transaction text using SA and LCP array.
     * 
     * @param text The input transaction text.
     * @param sa   Suffix array.
     * @param lcp  LCP array.
     * @return The longest substring that appears at least twice.
     */
    public static String findLongestRepeatedSubstring(String text, int[] sa, int[] lcp) {
        if (text == null || sa == null || lcp == null || lcp.length == 0) return "";

        int maxLen = 0;
        int maxIdx = 0;

        for (int i = 1; i < lcp.length; i++) {
            if (lcp[i] > maxLen) {
                maxLen = lcp[i];
                maxIdx = sa[i];
            }
        }

        return (maxLen > 0) ? text.substring(maxIdx, maxIdx + maxLen) : "";
    }

    /**
     * Finds all unique repeated patterns of at least a minimum length in transaction descriptions.
     */
    public static List<String> findRepeatedPatterns(String text, int[] sa, int[] lcp, int minLength) {
        Set<String> patterns = new HashSet<>();
        if (text == null || sa == null || lcp == null) return new ArrayList<>();

        for (int i = 1; i < lcp.length; i++) {
            if (lcp[i] >= minLength) {
                String match = text.substring(sa[i], sa[i] + lcp[i]).trim();
                if (!match.isEmpty()) {
                    patterns.add(match);
                }
            }
        }
        return new ArrayList<>(patterns);
    }

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("FinServe M2 - LCP / Kasai's Algorithm");
        System.out.println("=========================================");

        String transactionData = "TX1001:ONLINE PAYMENT AMAZON TX1002:ONLINE PAYMENT FLIPKART TX1003:ATM CASH WITHDRAWAL TX1004:ONLINE PAYMENT AMAZON TX1005:INTERNATIONAL PAYMENT";
        System.out.println("Input Transaction Stream:");
        System.out.println(transactionData);
        System.out.println();

        int[] sa = SuffixArray.buildSuffixArray(transactionData);
        int[] lcp = buildLCP(transactionData, sa);

        System.out.println("Computed LCP Array (sample entries with LCP > 0):");
        for (int i = 0; i < lcp.length; i++) {
            if (lcp[i] > 5) {
                System.out.println("  LCP[" + i + "] = " + lcp[i] + " between SA[" + (i-1) + "] and SA[" + i + "]: \"" 
                        + transactionData.substring(sa[i], sa[i] + lcp[i]) + "\"");
            }
        }

        String longestRepeated = findLongestRepeatedSubstring(transactionData, sa, lcp);
        System.out.println("\nLongest Repeated Substring in Transactions:");
        System.out.println("  \"" + longestRepeated.trim() + "\"");

        System.out.println("\nRepeated Transaction Patterns (minLength >= 10):");
        List<String> repeated = findRepeatedPatterns(transactionData, sa, lcp, 10);
        for (String pattern : repeated) {
            System.out.println("  - \"" + pattern + "\"");
        }
    }
}
