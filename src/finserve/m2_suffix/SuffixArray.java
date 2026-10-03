package finserve.m2_suffix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Suffix Array Implementation for FinServe Banking Analytics.
 * 
 * Banking Use Case:
 * Indexes large transaction logs and payment descriptions to enable fast binary search queries.
 * Allows quick lookup of keyword matches (e.g. "ONLINE PAYMENT", "AMAZON") across high-volume transaction data.
 * 
 * Time Complexity:
 * - Construction: O(N log^2 N) using prefix-doubling sorting (where N = text length).
 * - Search: O(M log N) where M = pattern length and N = text length.
 * Space Complexity:
 * - O(N) for storing the suffix array indices and rank records.
 */
public class SuffixArray {

    /**
     * Helper class representing a suffix and its rank pair during prefix-doubling.
     */
    public static class Suffix implements Comparable<Suffix> {
        public int index;
        public int rank1;
        public int rank2;

        public Suffix(int index, int rank1, int rank2) {
            this.index = index;
            this.rank1 = rank1;
            this.rank2 = rank2;
        }

        @Override
        public int compareTo(Suffix other) {
            if (this.rank1 != other.rank1) {
                return Integer.compare(this.rank1, other.rank1);
            }
            return Integer.compare(this.rank2, other.rank2);
        }
    }

    /**
     * Builds a Suffix Array for the given text using prefix doubling algorithm.
     * 
     * @param text The input text (e.g. transaction descriptions concatenated).
     * @return Array of starting indices of suffixes in lexicographical order.
     */
    public static int[] buildSuffixArray(String text) {
        if (text == null) return new int[0];
        int n = text.length();
        if (n == 0) return new int[0];

        Suffix[] suffixes = new Suffix[n];

        for (int i = 0; i < n; i++) {
            int r1 = text.charAt(i);
            int r2 = (i + 1 < n) ? text.charAt(i + 1) : -1;
            suffixes[i] = new Suffix(i, r1, r2);
        }

        Arrays.sort(suffixes);

        int[] ind = new int[n];
        for (int k = 4; k < 2 * n; k *= 2) {
            int rank = 0;
            int prevRank1 = suffixes[0].rank1;
            suffixes[0].rank1 = rank;
            ind[suffixes[0].index] = 0;

            for (int i = 1; i < n; i++) {
                if (suffixes[i].rank1 == prevRank1 && suffixes[i].rank2 == suffixes[i - 1].rank2) {
                    suffixes[i].rank1 = rank;
                } else {
                    prevRank1 = suffixes[i].rank1;
                    suffixes[i].rank1 = ++rank;
                }
                ind[suffixes[i].index] = i;
            }

            for (int i = 0; i < n; i++) {
                int nextIndex = suffixes[i].index + k / 2;
                suffixes[i].rank2 = (nextIndex < n) ? suffixes[ind[nextIndex]].rank1 : -1;
            }

            Arrays.sort(suffixes);
        }

        int[] sa = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = suffixes[i].index;
        }
        return sa;
    }

    /**
     * Performs binary search on the Suffix Array to locate all starting indices of pattern in text.
     * 
     * @param text    The full text indexed by sa.
     * @param sa      The suffix array of text.
     * @param pattern Pattern string to search for.
     * @return List of starting indices where pattern occurs in text.
     */
    public static List<Integer> search(String text, int[] sa, String pattern) {
        List<Integer> result = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty() || sa == null || sa.length == 0) {
            return result;
        }

        int n = text.length();
        int m = pattern.length();

        // Find first occurrence (lower bound)
        int low = 0, high = n - 1;
        int firstMatch = -1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            int saIdx = sa[mid];
            String suffixSub = text.substring(saIdx, Math.min(saIdx + m, n));
            int cmp = suffixSub.compareTo(pattern);
            if (cmp >= 0) {
                if (suffixSub.startsWith(pattern)) {
                    firstMatch = mid;
                }
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        if (firstMatch == -1) return result;

        // Find last occurrence (upper bound)
        low = firstMatch;
        high = n - 1;
        int lastMatch = firstMatch;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            int saIdx = sa[mid];
            if (text.startsWith(pattern, saIdx)) {
                lastMatch = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        for (int i = firstMatch; i <= lastMatch; i++) {
            result.add(sa[i]);
        }
        return result;
    }

    /**
     * Convenience method to build suffix array and search for pattern in text.
     */
    public static List<Integer> search(String text, String pattern) {
        int[] sa = buildSuffixArray(text);
        return search(text, sa, pattern);
    }

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("FinServe M2 - Suffix Array Analytics");
        System.out.println("=========================================");

        String text = "TX1001:ONLINE PAYMENT AMAZON TX1002:ONLINE PAYMENT FLIPKART TX1003:ATM CASH WITHDRAWAL TX1004:ONLINE PAYMENT AMAZON TX1005:INTERNATIONAL PAYMENT";
        System.out.println("Input Transaction Log:");
        System.out.println(text);
        System.out.println();

        int[] sa = buildSuffixArray(text);
        System.out.println("Constructed Suffix Array of length: " + sa.length);
        System.out.println("Top 5 Suffixes in Lexicographical Order:");
        for (int i = 0; i < Math.min(5, sa.length); i++) {
            System.out.println("  SA[" + i + "] = " + sa[i] + " -> \"" + text.substring(sa[i], Math.min(sa[i] + 35, text.length())) + "...\"");
        }

        String query = "ONLINE PAYMENT";
        System.out.println("\nSearching for Keyword: \"" + query + "\"...");
        List<Integer> matches = search(text, sa, query);
        System.out.println("Pattern found at character indices: " + matches);
        for (int idx : matches) {
            System.out.println("  Found match at index " + idx + ": \"" + text.substring(idx, Math.min(idx + 25, text.length())) + "...\"");
        }
    }
}
