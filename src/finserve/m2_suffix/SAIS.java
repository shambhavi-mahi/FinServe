package finserve.m2_suffix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SA-IS (Suffix Array Induced Sorting) Algorithm for FinServe Banking Analytics.
 * 
 * Banking Use Case:
 * Efficiently constructs suffix arrays in linear O(N) time. Essential for processing
 * large-scale financial documents, compliance audit logs, and transaction streams
 * where sub-quadratic indexing speed is required.
 * 
 * Time Complexity:
 * - Construction: O(N) linear time (where N = text length).
 * - Search: O(M log N) where M = pattern length.
 * Space Complexity:
 * - O(N) space for bucket counts, LMS types, and temporary arrays.
 */
public class SAIS {

    /**
     * Builds Suffix Array in linear O(N) time using SA-IS algorithm.
     * 
     * @param text Input financial text.
     * @return Suffix Array (indices of sorted suffixes).
     */
    public static int[] buildSuffixArray(String text) {
        if (text == null || text.length() == 0) return new int[0];
        
        int n = text.length();
        int[] s = new int[n + 1];
        int maxChar = 0;
        for (int i = 0; i < n; i++) {
            s[i] = text.charAt(i) + 1; // +1 shift to reserve 0 for sentinel
            if (s[i] > maxChar) maxChar = s[i];
        }
        s[n] = 0; // sentinel

        int[] saWithSentinel = sais(s, maxChar + 1);

        int[] sa = new int[n];
        System.arraycopy(saWithSentinel, 1, sa, 0, n);
        return sa;
    }

    private static int[] sais(int[] s, int K) {
        int n = s.length;
        int[] sa = new int[n];
        if (n == 1) {
            sa[0] = 0;
            return sa;
        }

        boolean[] isS = new boolean[n];
        isS[n - 1] = true;
        for (int i = n - 2; i >= 0; i--) {
            if (s[i] < s[i + 1]) {
                isS[i] = true;
            } else if (s[i] > s[i + 1]) {
                isS[i] = false;
            } else {
                isS[i] = isS[i + 1];
            }
        }

        int[] lmsMap = new int[n];
        Arrays.fill(lmsMap, -1);
        int lmsCount = 0;
        for (int i = 1; i < n; i++) {
            if (isS[i] && !isS[i - 1]) {
                lmsMap[i] = lmsCount++;
            }
        }

        int[] lms = new int[lmsCount];
        int idx = 0;
        for (int i = 1; i < n; i++) {
            if (isS[i] && !isS[i - 1]) {
                lms[idx++] = i;
            }
        }

        int[] bucket = new int[K];
        for (int val : s) bucket[val]++;

        induceSort(s, sa, isS, lms, bucket, K);

        int[] sortedLMS = new int[lmsCount];
        int count = 0;
        for (int val : sa) {
            if (lmsMap[val] != -1) {
                sortedLMS[count++] = val;
            }
        }

        int[] lmsNames = new int[lmsCount];
        int name = 0;
        lmsNames[lmsMap[sortedLMS[0]]] = name;

        for (int i = 1; i < lmsCount; i++) {
            int prev = sortedLMS[i - 1];
            int curr = sortedLMS[i];
            if (!isLmsSubstringsEqual(s, isS, prev, curr)) {
                name++;
            }
            lmsNames[lmsMap[curr]] = name;
        }

        int[] lmsSA;
        if (name + 1 < lmsCount) {
            lmsSA = sais(lmsNames, name + 1);
        } else {
            lmsSA = new int[lmsCount];
            for (int i = 0; i < lmsCount; i++) {
                lmsSA[lmsNames[i]] = i;
            }
        }

        int[] orderedLMS = new int[lmsCount];
        for (int i = 0; i < lmsCount; i++) {
            orderedLMS[i] = lms[lmsSA[i]];
        }

        induceSort(s, sa, isS, orderedLMS, bucket, K);

        return sa;
    }

    private static void induceSort(int[] s, int[] sa, boolean[] isS, int[] lms, int[] bucket, int K) {
        int n = s.length;
        Arrays.fill(sa, -1);

        int[] head = getBucketHeads(bucket, K);
        int[] tail = getBucketTails(bucket, K);

        for (int i = lms.length - 1; i >= 0; i--) {
            int pos = lms[i];
            sa[tail[s[pos]]--] = pos;
        }

        head = getBucketHeads(bucket, K);
        for (int i = 0; i < n; i++) {
            if (sa[i] > 0) {
                int j = sa[i] - 1;
                if (!isS[j]) {
                    sa[head[s[j]]++] = j;
                }
            }
        }

        tail = getBucketTails(bucket, K);
        for (int i = n - 1; i >= 0; i--) {
            if (sa[i] > 0) {
                int j = sa[i] - 1;
                if (isS[j]) {
                    sa[tail[s[j]]--] = j;
                }
            }
        }
    }

    private static boolean isLmsSubstringsEqual(int[] s, boolean[] isS, int i, int j) {
        int n = s.length;
        for (int k = 0; ; k++) {
            boolean iLms = (k > 0) && isS[i + k] && !isS[i + k - 1];
            boolean jLms = (k > 0) && isS[j + k] && !isS[j + k - 1];

            if (iLms && jLms) return true;
            if (iLms || jLms) return false;

            if (i + k >= n || j + k >= n || s[i + k] != s[j + k] || isS[i + k] != isS[j + k]) {
                return false;
            }
        }
    }

    private static int[] getBucketHeads(int[] bucket, int K) {
        int[] head = new int[K];
        int sum = 0;
        for (int i = 0; i < K; i++) {
            head[i] = sum;
            sum += bucket[i];
        }
        return head;
    }

    private static int[] getBucketTails(int[] bucket, int K) {
        int[] tail = new int[K];
        int sum = 0;
        for (int i = 0; i < K; i++) {
            sum += bucket[i];
            tail[i] = sum - 1;
        }
        return tail;
    }

    /**
     * Performs binary search on SA-IS constructed Suffix Array.
     */
    public static List<Integer> search(String text, int[] sa, String pattern) {
        return SuffixArray.search(text, sa, pattern);
    }

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("FinServe M2 - SA-IS (Linear Suffix Array)");
        System.out.println("=========================================");

        String financialDoc = "FINANCIAL REPORT 2026: ONLINE PAYMENT AMAZON ONLINE PAYMENT FLIPKART INTERNATIONAL PAYMENT ATM CASH WITHDRAWAL";
        System.out.println("Input Financial Document:");
        System.out.println(financialDoc);
        System.out.println();

        int[] sa = buildSuffixArray(financialDoc);
        System.out.println("SA-IS Suffix Array constructed in O(N) time! Length: " + sa.length);
        System.out.println("First 5 Suffixes in Lexicographical Order:");
        for (int i = 0; i < Math.min(5, sa.length); i++) {
            System.out.println("  SA[" + i + "] = " + sa[i] + " -> \"" + financialDoc.substring(sa[i], Math.min(sa[i] + 30, financialDoc.length())) + "...\"");
        }

        String query = "PAYMENT";
        System.out.println("\nSearching for Document Keyword: \"" + query + "\"...");
        List<Integer> occurrences = search(financialDoc, sa, query);
        System.out.println("Occurrences found at indices: " + occurrences);
        for (int idx : occurrences) {
            System.out.println("  Match at index " + idx + ": \"" + financialDoc.substring(idx, Math.min(idx + 25, financialDoc.length())) + "...\"");
        }
    }
}
