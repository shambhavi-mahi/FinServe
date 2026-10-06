package finserve.m3_dp;

public class WeightedEditDistance {
    // Severity-Based Error Detection
    // DP algorithm for Weighted Edit Distance
    // Time complexity: O(m * n)
    // Space complexity: O(m * n)

    public static int calculateWeighted(String a, String b) {
        int m = a.length();
        int n = b.length();
        int[][] dp = new int[m + 1][n + 1];

        // Define costs for financial data errors
        int insertCost = 2; // e.g., accidental extra digit/char
        int deleteCost = 2; // e.g., missing digit/char
        int replaceCost = 3; // e.g., wrong digit/char (more penalty as it changes info)

        for (int i = 0; i <= m; i++)
            dp[i][0] = i * deleteCost;
        for (int j = 0; j <= n; j++)
            dp[0][j] = j * insertCost;

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + replaceCost,
                            Math.min(dp[i - 1][j] + deleteCost,
                                    dp[i][j - 1] + insertCost));
                }
            }
        }
        return dp[m][n];
    }

    public static void main(String[] args) {
        System.out.println("--- Weighted Edit Distance Demo (FinServe) ---");
        String input = "1005"; // Mistyped amount or code
        String expected = "100";
        System.out.println("Comparing financial codes/amounts with assigned costs:");
        System.out.println("Input: " + input);
        System.out.println("Expected: " + expected);
        int distance = calculateWeighted(input, expected);
        System.out.println("Weighted Distance: " + distance);
        System.out.println("Higher costs reflect the severity of the error.");
    }
}
