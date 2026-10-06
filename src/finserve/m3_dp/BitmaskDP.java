package finserve.m3_dp;

public class BitmaskDP {
    //Resource and Route Optimization
    // DP algorithm with Bitmasking
    // Time complexity: O(n^2 * 2^n)
    // Space complexity: O(n * 2^n)
    
    // Example: Traveling Salesman Problem logic for visiting different bank branches or ATMs
    public static int optimize(int[][] cost) {
        int n = cost.length;
        int VISITED_ALL = (1 << n) - 1;
        int[][] dp = new int[1 << n][n];
        
        for (int i = 0; i < (1 << n); i++) {
            for (int j = 0; j < n; j++) {
                dp[i][j] = Integer.MAX_VALUE / 2;
            }
        }
        dp[1][0] = 0; // Start at node 0 (e.g., Main Branch)
        
        for (int mask = 1; mask < (1 << n); mask++) {
            for (int i = 0; i < n; i++) {
                // If i is in the mask
                if ((mask & (1 << i)) != 0) {
                    for (int j = 0; j < n; j++) {
                        // If j is NOT in the mask, we can visit it next
                        if ((mask & (1 << j)) == 0) {
                            int nextMask = mask | (1 << j);
                            dp[nextMask][j] = Math.min(dp[nextMask][j], dp[mask][i] + cost[i][j]);
                        }
                    }
                }
            }
        }
        
        int minCost = Integer.MAX_VALUE / 2;
        for (int i = 1; i < n; i++) {
            minCost = Math.min(minCost, dp[VISITED_ALL][i] + cost[i][0]); // Return to start
        }
        
        return minCost;
    }

    public static void main(String[] args) {
        System.out.println("--- Bitmask DP Demo (FinServe) ---");
        // Example costs between 4 financial operations or ATM locations
        int[][] operationsCost = {
            {0, 10, 15, 20},
            {10, 0, 35, 25},
            {15, 35, 0, 30},
            {20, 25, 30, 0}
        };
        System.out.println("Optimizing sequence of resource visits/operations.");
        int optimalCost = optimize(operationsCost);
        System.out.println("Minimum cost to visit all operations and return: " + optimalCost);
    }
}
