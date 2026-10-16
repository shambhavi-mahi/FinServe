package finserve.m5_np_approx;

import java.util.*;

/**
 * Module 5: NP-Completeness and Approximation Algorithms
 * 
 * Financial Use Case: Interbank Clearing House & Ledger Discrepancy Reconciliation
 * 
 * Problem Formulation:
 * At daily settlement cutoff, an Automated Clearing House (ACH) or central bank reports
 * an aggregate discrepancy or clearing batch settlement target T.
 * The bank's audit team holds n unsettled pending ledger transactions with amounts {a_1, ..., a_n}.
 * The auditor must determine which subset of pending transactions sums exactly to target T.
 * 
 * Core Complexity Concepts Illustrated:
 * 1. Decision Problem vs Optimisation Problem:
 *    - Decision: Does there exist S ⊆ {1..n} such that ∑_{i∈S} a_i = T? (Answer: YES/NO).
 *    - Optimisation: Find subset S that minimizes |∑_{i∈S} a_i - T| (or maximizes settled value <= T).
 * 2. P, NP, co-NP Definitions Through Verification & Witnesses:
 *    - SUBSET-SUM ∈ NP because given a proposed subset witness (certificate),
 *      verification simply sums the numbers in O(n) deterministic polynomial time.
 *    - Finding the witness from scratch is NP-Complete (Karp's 21 problems).
 *    - co-NP: Proving that NO such subset exists requires checking exponential combinations
 *      (unless NP = co-NP).
 */
public class ReconciliationSubsetSum {

    public static class AuditTransaction {
        public final String txId;
        public final String description;
        public final int amount; // in whole dollars/cents

        public AuditTransaction(String txId, String description, int amount) {
            this.txId = txId;
            this.description = description;
            this.amount = amount;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s ($%d)", txId, description, amount);
        }
    }

    public static class VerificationResult {
        public final boolean isValid;
        public final int computedSum;
        public final int target;
        public final long verificationTimeNs;
        public final String proofExplanation;

        public VerificationResult(boolean isValid, int computedSum, int target, long timeNs, String explanation) {
            this.isValid = isValid;
            this.computedSum = computedSum;
            this.target = target;
            this.verificationTimeNs = timeNs;
            this.proofExplanation = explanation;
        }
    }

    public static class ReconciliationResult {
        public final boolean exactMatchFound;
        public final List<AuditTransaction> reconciledSubset;
        public final int totalReconciledAmount;
        public final int discrepancyRemaining;
        public final long executionTimeNs;

        public ReconciliationResult(boolean exact, List<AuditTransaction> subset, int total, int discrepancy, long timeNs) {
            this.exactMatchFound = exact;
            this.reconciledSubset = subset;
            this.totalReconciledAmount = total;
            this.discrepancyRemaining = discrepancy;
            this.executionTimeNs = timeNs;
        }
    }

    /**
     * Canonical NP Witness Verification:
     * Demonstrates that verifying a solution is in P (deterministic O(k) polynomial time).
     */
    public static VerificationResult verifyWitness(List<AuditTransaction> witnessCertificate, int targetSum) {
        long start = System.nanoTime();
        int sum = 0;
        for (AuditTransaction tx : witnessCertificate) {
            sum += tx.amount;
        }
        long time = System.nanoTime() - start;

        boolean valid = (sum == targetSum);
        String explanation = valid ?
            "VALID WITNESS: Verified in " + time + " ns (O(|witness|) deterministic polynomial time). Proves Subset-Sum ∈ NP." :
            "INVALID WITNESS: Sum " + sum + " does NOT match target " + targetSum + ".";

        return new VerificationResult(valid, sum, targetSum, time, explanation);
    }

    /**
     * Solves the Decision & Optimization problem via Dynamic Programming.
     * Finds the exact subset if one exists (Decision = YES), or the closest subset (Optimization).
     */
    public static ReconciliationResult reconcileBatch(List<AuditTransaction> pendingTxs, int targetSum) {
        long start = System.nanoTime();
        int n = pendingTxs.size();

        // dp[j] stores whether a sum of j is achievable
        boolean[] dp = new boolean[targetSum + 1];
        int[] parentTx = new int[targetSum + 1];
        int[] prevSum = new int[targetSum + 1];
        Arrays.fill(parentTx, -1);
        Arrays.fill(prevSum, -1);

        dp[0] = true;

        for (int i = 0; i < n; i++) {
            int amt = pendingTxs.get(i).amount;
            for (int s = targetSum; s >= amt; s--) {
                if (!dp[s] && dp[s - amt]) {
                    dp[s] = true;
                    parentTx[s] = i;
                    prevSum[s] = s - amt;
                }
            }
        }

        // Check if exact target is reachable (Decision Problem)
        int bestSum = 0;
        for (int s = targetSum; s >= 0; s--) {
            if (dp[s]) {
                bestSum = s;
                break;
            }
        }

        // Reconstruct subset
        List<AuditTransaction> subset = new ArrayList<>();
        int curr = bestSum;
        while (curr > 0 && parentTx[curr] != -1) {
            int txIndex = parentTx[curr];
            subset.add(pendingTxs.get(txIndex));
            curr = prevSum[curr];
        }

        long time = System.nanoTime() - start;
        boolean exact = (bestSum == targetSum);
        return new ReconciliationResult(exact, subset, bestSum, Math.abs(targetSum - bestSum), time);
    }

    public static void main(String[] args) {
        System.out.println("--- Reconciliation Subset-Sum Demo (FinServe) ---");
        List<AuditTransaction> pending = Arrays.asList(
            new AuditTransaction("TX-1", "Wire", 450),
            new AuditTransaction("TX-2", "Clearing", 1200),
            new AuditTransaction("TX-3", "Settlement", 310),
            new AuditTransaction("TX-4", "Cheque", 890)
        );
        int target = 1650;
        ReconciliationResult res = reconcileBatch(pending, target);
        System.out.println("Exact match: " + res.exactMatchFound + " (Reconciled: $" + res.totalReconciledAmount + ")");
        System.out.println("Transactions: " + res.reconciledSubset);
        VerificationResult v = verifyWitness(res.reconciledSubset, target);
        System.out.println(v.proofExplanation);
    }
}
