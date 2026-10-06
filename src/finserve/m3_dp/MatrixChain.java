package finserve.m3_dp;

public class MatrixChain {
    //Batch Processing Optimization
    // DP algorithm for Matrix Chain Multiplication
    // Time complexity: O(n^3)
    // Space complexity: O(n^2)
    
    public static int matrixChainOrder(int[] p) {
        int n = p.length - 1;
        int[][] m = new int[n + 1][n + 1];
        
        for (int i = 1; i <= n; i++) {
            m[i][i] = 0;
        }
        
        for (int l = 2; l <= n; l++) { // l is chain length
            for (int i = 1; i <= n - l + 1; i++) {
                int j = i + l - 1;
                m[i][j] = Integer.MAX_VALUE;
                for (int k = i; k <= j - 1; k++) {
                    int q = m[i][k] + m[k + 1][j] + p[i - 1] * p[k] * p[j];
                    if (q < m[i][j]) {
                        m[i][j] = q;
                    }
                }
            }
        }
        
        return m[1][n];
    }

    public static void main(String[] args) {
        System.out.println("--- Matrix Chain Multiplication Demo (FinServe) ---");
        // E.g., Dimensions of data matrices representing sequential transaction analyses
        int[] dimensions = {10, 30, 5, 60}; 
        System.out.println("Optimizing the evaluation order of sequential transaction data transformations.");
        int minOperations = matrixChainOrder(dimensions);
        System.out.println("Minimum scalar multiplications needed: " + minOperations);
        System.out.println("This minimizes computational overhead for batch data processing.");
    }
}
