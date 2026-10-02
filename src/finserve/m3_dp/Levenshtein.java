package finserve.m3_dp;

public class Levenshtein {
    // DP algorithm for Levenshtein distance
    // Time complexity: O(m * n)
    // Space complexity: O(m * n) (can be optimized to O(min(m,n)))
    
    public static int calculate(String a, String b) {
        int m = a.length();
        int n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], // Replace
                                    Math.min(dp[i - 1][j],    // Delete
                                             dp[i][j - 1]));  // Insert
                }
            }
        }
        return dp[m][n];
    }

    public static void main(String[] args) {
        System.out.println("--- Levenshtein Distance Demo (FinServe) ---");
        String input = "AMAZN PAYMENT";
        String expected = "AMAZON PAYMENT";
        System.out.println("Correcting transaction description:");
        System.out.println("Input: " + input);
        System.out.println("Expected: " + expected);
        int distance = calculate(input, expected);
        System.out.println("Levenshtein Distance: " + distance);
        System.out.println("A lower distance indicates a likely match for autocorrection.");
    }
}
