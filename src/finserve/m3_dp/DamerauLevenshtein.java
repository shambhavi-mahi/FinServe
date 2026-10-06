package finserve.m3_dp;

public class DamerauLevenshtein {
    //Fast Data-Entry Error Handling
    // DP algorithm for Damerau-Levenshtein distance (includes transpositions)
    // Time complexity: O(m * n)
    // Space complexity: O(m * n)
    
    public static int calculate(String a, String b) {
        int m = a.length();
        int n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                
                dp[i][j] = Math.min(dp[i - 1][j - 1] + cost, // Replace
                             Math.min(dp[i - 1][j] + 1,      // Delete
                                      dp[i][j - 1] + 1));    // Insert
                                      
                if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)) {
                    dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + cost); // Transposition
                }
            }
        }
        return dp[m][n];
    }

    public static void main(String[] args) {
        System.out.println("--- Damerau-Levenshtein Distance Demo (FinServe) ---");
        String input = "AMZAON PAYMENT";
        String expected = "AMAZON PAYMENT";
        System.out.println("Correcting typing error (character swap):");
        System.out.println("Input: " + input);
        System.out.println("Expected: " + expected);
        int distance = calculate(input, expected);
        System.out.println("Damerau-Levenshtein Distance: " + distance);
        System.out.println("Effectively handles swaps in fast data entry scenarios.");
    }
}
