package finserve.m5_np_approx;

import java.util.*;

/**
 * Module 5: NP-Completeness and Approximation Algorithms
 * 
 * Financial Use Case: Investment Portfolio & Loan Capital Budgeting
 * 
 * Problem Formulation:
 * A commercial bank has an available liquidity/capital lending budget W ($ millions).
 * It receives n corporate loan / project funding proposals.
 * Each proposal i requires capital w_i (weight) and yields expected interest profit v_i (value).
 * The bank must select a subset of loans to maximize total return without exceeding budget W.
 * This is the canonical 0/1 Knapsack problem (NP-Hard).
 * 
 * Approximation Hierarchy Demonstrated:
 * 1. Exact DP: Pseudo-polynomial time O(n * W). Infeasible when W is large (e.g., $500,000,000).
 * 2. Greedy 2-Approximation (APX class):
 *    - Sort proposals by profitability density (v_i / w_i).
 *    - Greedily pick proposals until budget would be exceeded.
 *    - Compare greedy pack against the single highest-value proposal and pick the max.
 *    - Guarantee: Achieves at least 50% of optimal (Ratio factor >= 1/2) in O(n log n).
 * 3. FPTAS (Fully Polynomial Time Approximation Scheme):
 *    - Given tolerance ε (e.g. ε = 0.05 for 95% guaranteed optimality).
 *    - Scales down profits by K = (ε * v_max) / n.
 *    - Computes exact DP over scaled values in O(n^2 / ε) time.
 *    - Proves how (1 - ε) approximation exists for Knapsack (FPTAS), unlike general TSP which is not in APX!
 */
public class KnapsackCapitalBudgeting {

    public static class LoanProposal {
        public final String id;
        public final String borrowerName;
        public final int capitalRequired; // Weight in $K
        public final int expectedReturn;   // Value in $K

        public LoanProposal(String id, String borrowerName, int capitalRequired, int expectedReturn) {
            this.id = id;
            this.borrowerName = borrowerName;
            this.capitalRequired = capitalRequired;
            this.expectedReturn = expectedReturn;
        }

        public double getDensity() {
            return (double) expectedReturn / capitalRequired;
        }

        @Override
        public String toString() {
            return String.format("%s (%s): Req=$%dK, Ret=$%dK [Yield=%.2f%%]", 
                id, borrowerName, capitalRequired, expectedReturn, getDensity() * 100);
        }
    }

    public static class AllocationResult {
        public final String methodName;
        public final List<LoanProposal> selectedLoans;
        public final int totalCapitalUsed;
        public final int totalExpectedReturn;
        public final long executionTimeNs;
        public final String theoreticalGuarantee;

        public AllocationResult(String methodName, List<LoanProposal> selected, int capital, int profit, long timeNs, String guarantee) {
            this.methodName = methodName;
            this.selectedLoans = selected;
            this.totalCapitalUsed = capital;
            this.totalExpectedReturn = profit;
            this.executionTimeNs = timeNs;
            this.theoreticalGuarantee = guarantee;
        }
    }

    /**
     * Exact 0/1 Knapsack Dynamic Programming:
     * Time Complexity: O(n * W)
     */
    public static AllocationResult solveExactDP(List<LoanProposal> proposals, int budget) {
        long start = System.nanoTime();
        int n = proposals.size();
        int[][] dp = new int[n + 1][budget + 1];

        for (int i = 1; i <= n; i++) {
            LoanProposal p = proposals.get(i - 1);
            for (int w = 0; w <= budget; w++) {
                if (p.capitalRequired <= w) {
                    dp[i][w] = Math.max(dp[i - 1][w], dp[i - 1][w - p.capitalRequired] + p.expectedReturn);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        // Backtrack to find chosen items
        List<LoanProposal> selected = new ArrayList<>();
        int currW = budget;
        for (int i = n; i > 0; i--) {
            if (dp[i][currW] != dp[i - 1][currW]) {
                LoanProposal p = proposals.get(i - 1);
                selected.add(p);
                currW -= p.capitalRequired;
            }
        }

        int totalCap = 0;
        int totalVal = 0;
        for (LoanProposal p : selected) {
            totalCap += p.capitalRequired;
            totalVal += p.expectedReturn;
        }

        long time = System.nanoTime() - start;
        return new AllocationResult("Exact 0/1 Knapsack DP", selected, totalCap, totalVal, time, "1.00 * OPT (Optimal)");
    }

    /**
     * Greedy 2-Approximation Algorithm:
     * Time Complexity: O(n log n)
     * Guarantee: Returns at least 0.50 * OPT.
     */
    public static AllocationResult solveGreedy2Approx(List<LoanProposal> proposals, int budget) {
        long start = System.nanoTime();
        List<LoanProposal> sorted = new ArrayList<>(proposals);
        sorted.sort((a, b) -> Double.compare(b.getDensity(), a.getDensity()));

        List<LoanProposal> greedyList = new ArrayList<>();
        int greedyCap = 0;
        int greedyVal = 0;

        for (LoanProposal p : sorted) {
            if (greedyCap + p.capitalRequired <= budget) {
                greedyList.add(p);
                greedyCap += p.capitalRequired;
                greedyVal += p.expectedReturn;
            }
        }

        // Find single highest-profit item that fits budget
        LoanProposal maxSingle = null;
        for (LoanProposal p : proposals) {
            if (p.capitalRequired <= budget) {
                if (maxSingle == null || p.expectedReturn > maxSingle.expectedReturn) {
                    maxSingle = p;
                }
            }
        }

        // Compare greedy set with highest single item
        List<LoanProposal> finalSelected;
        int finalCap, finalVal;
        if (maxSingle != null && maxSingle.expectedReturn > greedyVal) {
            finalSelected = Collections.singletonList(maxSingle);
            finalCap = maxSingle.capitalRequired;
            finalVal = maxSingle.expectedReturn;
        } else {
            finalSelected = greedyList;
            finalCap = greedyCap;
            finalVal = greedyVal;
        }

        long time = System.nanoTime() - start;
        return new AllocationResult("Greedy 2-Approximation", finalSelected, finalCap, finalVal, time, ">= 0.50 * OPT");
    }

    /**
     * FPTAS (Fully Polynomial Time Approximation Scheme):
     * Time Complexity: O(n^2 / ε)
     * Guarantee: Returns at least (1 - ε) * OPT.
     */
    public static AllocationResult solveFPTAS(List<LoanProposal> proposals, int budget, double epsilon) {
        long start = System.nanoTime();
        int n = proposals.size();
        if (n == 0 || budget <= 0) {
            return new AllocationResult("FPTAS (ε=" + epsilon + ")", Collections.emptyList(), 0, 0, 0, ">= " + (1 - epsilon) + " * OPT");
        }

        int vMax = 0;
        for (LoanProposal p : proposals) {
            vMax = Math.max(vMax, p.expectedReturn);
        }

        double K = (epsilon * vMax) / (double) n;
        if (K < 1.0) K = 1.0; // Avoid division issues

        int[] scaledVal = new int[n];
        int sumScaledVal = 0;
        for (int i = 0; i < n; i++) {
            scaledVal[i] = (int) Math.floor(proposals.get(i).expectedReturn / K);
            sumScaledVal += scaledVal[i];
        }

        // dp[v] = min weight required to achieve scaled profit v
        int INF = Integer.MAX_VALUE / 2;
        int[][] minWeightDP = new int[n + 1][sumScaledVal + 1];
        for (int[] row : minWeightDP) Arrays.fill(row, INF);
        minWeightDP[0][0] = 0;

        for (int i = 1; i <= n; i++) {
            int w_i = proposals.get(i - 1).capitalRequired;
            int v_i = scaledVal[i - 1];
            for (int v = 0; v <= sumScaledVal; v++) {
                minWeightDP[i][v] = minWeightDP[i - 1][v];
                if (v >= v_i && minWeightDP[i - 1][v - v_i] != INF) {
                    minWeightDP[i][v] = Math.min(minWeightDP[i][v], minWeightDP[i - 1][v - v_i] + w_i);
                }
            }
        }

        // Find max scaled profit that fits in budget
        int bestScaledV = 0;
        for (int v = sumScaledVal; v >= 0; v--) {
            if (minWeightDP[n][v] <= budget) {
                bestScaledV = v;
                break;
            }
        }

        // Backtrack
        List<LoanProposal> selected = new ArrayList<>();
        int currV = bestScaledV;
        for (int i = n; i > 0 && currV > 0; i--) {
            int v_i = scaledVal[i - 1];
            int w_i = proposals.get(i - 1).capitalRequired;
            if (currV >= v_i && minWeightDP[i - 1][currV - v_i] != INF && 
                minWeightDP[i][currV] == minWeightDP[i - 1][currV - v_i] + w_i) {
                selected.add(proposals.get(i - 1));
                currV -= v_i;
            }
        }

        int actualCap = 0;
        int actualProfit = 0;
        for (LoanProposal p : selected) {
            actualCap += p.capitalRequired;
            actualProfit += p.expectedReturn;
        }

        long time = System.nanoTime() - start;
        return new AllocationResult("FPTAS (ε=" + epsilon + ")", selected, actualCap, actualProfit, time, 
            String.format(">= %.2f * OPT (Guaranteed %.1f%% of Optimal)", (1 - epsilon), (1 - epsilon) * 100));
    }

    public static void main(String[] args) {
        System.out.println("--- Knapsack Capital Budgeting Demo (FinServe) ---");
        List<LoanProposal> proposals = Arrays.asList(
            new LoanProposal("LP-1", "Green Energy Wind Farm", 250, 400),
            new LoanProposal("LP-2", "Fintech Payment Rails", 150, 260),
            new LoanProposal("LP-3", "Commercial Real Estate", 400, 500),
            new LoanProposal("LP-4", "Healthcare Biotech R&D", 200, 310)
        );
        int budget = 500;
        AllocationResult exact = solveExactDP(proposals, budget);
        AllocationResult greedy = solveGreedy2Approx(proposals, budget);
        AllocationResult fptas = solveFPTAS(proposals, budget, 0.10);

        System.out.println("Exact DP Return:    $" + exact.totalExpectedReturn + "K (Capital: $" + exact.totalCapitalUsed + "K)");
        System.out.println("Greedy 2-Approx:    $" + greedy.totalExpectedReturn + "K (" + greedy.theoreticalGuarantee + ")");
        System.out.println("FPTAS (ε=0.10):     $" + fptas.totalExpectedReturn + "K (" + fptas.theoreticalGuarantee + ")");
    }
}
