package finserve.m6_random_parallel;

import finserve.model.Transaction;
import java.util.*;
import java.util.concurrent.*;

/**
 * Module 6: Randomised and Parallel Algorithms
 * 
 * Financial Use Case: High-Throughput Parallel Ledger Aggregation & Real-Time Balance Scan
 * 
 * Theoretical Parallel Foundations:
 * 1. Work and Span Model:
 *    - Work (T_1): Total operations executed on 1 processor (sequential time).
 *    - Span (T_∞): Critical path length / depth of computation DAG (time with ∞ processors).
 *    - Theoretical Parallelism = T_1 / T_∞ (Maximum possible speedup).
 * 
 * 2. Brent's Theorem:
 *    - On p processors, parallel running time T_p satisfies:
 *        T_p <= (T_1 / p) + T_∞.
 *    - Speedup Ceiling: S_p = T_1 / T_p <= min(p, T_1 / T_∞).
 * 
 * 3. Parallel Primitives Implemented:
 *    - Parallel Reduce (Divide-and-Conquer):
 *      Work T_1 = O(n), Span T_∞ = O(log n).
 *      Aggregates total volume, max value, and count across financial transaction datasets.
 *    - Parallel Prefix-Sum Scan (Blelloch Algorithm):
 *      Work T_1 = O(n), Span T_∞ = O(log n).
 *      Calculates running cumulative account balances over thousands of credits/debits.
 * 
 * 4. Where Parallelism Shines vs Where It Doesn't:
 *    - Shines: Large data-parallel chunks (embarrassingly parallel batch ledgers).
 *    - Fails: Small n where thread synchronization and cache-line bouncing overhead > computation.
 */
public class ParallelLedgerAnalytics {

    private static final ForkJoinPool POOL = ForkJoinPool.commonPool();

    public static class LedgerMetrics {
        public final long count;
        public final double totalVolume;
        public final double maxAmount;
        public final double minAmount;
        public final double averageAmount;
        public final long timeNs;
        public final int coresUsed;

        public LedgerMetrics(long count, double total, double max, double min, long timeNs, int cores) {
            this.count = count;
            this.totalVolume = total;
            this.maxAmount = max;
            this.minAmount = min;
            this.averageAmount = count > 0 ? total / count : 0.0;
            this.timeNs = timeNs;
            this.coresUsed = cores;
        }

        @Override
        public String toString() {
            return String.format(
                "Transactions Processed: %,d\n" +
                "Total Volume:           $%,.2f\n" +
                "Max Transaction:        $%,.2f\n" +
                "Min Transaction:        $%,.2f\n" +
                "Average Amount:         $%,.2f\n" +
                "Execution Time:         %.3f ms (using %d CPU cores)",
                count, totalVolume, maxAmount, minAmount, averageAmount, timeNs / 1_000_000.0, coresUsed
            );
        }
    }

    /**
     * Parallel Reduce ForkJoin Task:
     * Divides array into subproblems until threshold, achieving O(log n) Span.
     */
    private static class ParallelReduceTask extends RecursiveTask<double[]> {
        private final double[] data;
        private final int start;
        private final int end;
        private static final int THRESHOLD = 50_000; // Granularity threshold

        public ParallelReduceTask(double[] data, int start, int end) {
            this.data = data;
            this.start = start;
            this.end = end;
        }

        @Override
        protected double[] compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                // Sequential base case
                double sum = 0.0;
                double max = length > 0 ? data[start] : 0.0;
                double min = length > 0 ? data[start] : 0.0;
                for (int i = start; i < end; i++) {
                    double val = data[i];
                    sum += val;
                    if (val > max) max = val;
                    if (val < min) min = val;
                }
                return new double[]{sum, max, min, length};
            }

            int mid = start + length / 2;
            ParallelReduceTask left = new ParallelReduceTask(data, start, mid);
            ParallelReduceTask right = new ParallelReduceTask(data, mid, end);

            left.fork();
            double[] rResult = right.compute();
            double[] lResult = left.join();

            return new double[]{
                lResult[0] + rResult[0],
                Math.max(lResult[1], rResult[1]),
                Math.min(lResult[2], rResult[2]),
                lResult[3] + rResult[3]
            };
        }
    }

    /**
     * Parallel Ledger Reduction:
     * Computes aggregate metrics in parallel with work O(N) and span O(log N).
     */
    public static LedgerMetrics computeParallelMetrics(double[] amounts) {
        long start = System.nanoTime();
        double[] res = POOL.invoke(new ParallelReduceTask(amounts, 0, amounts.length));
        long time = System.nanoTime() - start;
        int cores = Runtime.getRuntime().availableProcessors();
        return new LedgerMetrics((long) res[3], res[0], res[1], res[2], time, cores);
    }

    /**
     * Sequential Ledger Reduction (for speedup & Brent's theorem benchmarking).
     */
    public static LedgerMetrics computeSequentialMetrics(double[] amounts) {
        long start = System.nanoTime();
        double sum = 0.0;
        double max = amounts.length > 0 ? amounts[0] : 0.0;
        double min = amounts.length > 0 ? amounts[0] : 0.0;
        for (double val : amounts) {
            sum += val;
            if (val > max) max = val;
            if (val < min) min = val;
        }
        long time = System.nanoTime() - start;
        return new LedgerMetrics(amounts.length, sum, max, min, time, 1);
    }

    /**
     * Blelloch Parallel Prefix-Sum (Scan) Algorithm:
     * Calculates running account balance over transaction history in O(N) Work and O(log N) Span.
     * Uses Up-Sweep (reduce) and Down-Sweep phases.
     */
    public static double[] blellochParallelScan(double[] input) {
        int n = input.length;
        if (n == 0) return new double[0];

        // Pad to next power of 2 for clean binary tree representation
        int powerOfTwo = 1;
        while (powerOfTwo < n) powerOfTwo <<= 1;
        double[] tree = new double[2 * powerOfTwo];

        // Copy input to leaves
        System.arraycopy(input, 0, tree, powerOfTwo, n);

        // Phase 1: Up-Sweep (Reduce tree from leaves to root)
        // Span: O(log N), Work: O(N)
        for (int step = powerOfTwo / 2; step > 0; step /= 2) {
            for (int i = step; i < 2 * step; i++) {
                tree[i] = tree[2 * i] + tree[2 * i + 1];
            }
        }

        // Set root to 0 for exclusive scan
        tree[1] = 0;

        // Phase 2: Down-Sweep (Propagate prefixes down to leaves)
        // Span: O(log N), Work: O(N)
        for (int step = 1; step < powerOfTwo; step *= 2) {
            for (int i = step; i < 2 * step; i++) {
                double leftVal = tree[2 * i];
                tree[2 * i] = tree[i]; // Left child gets parent value
                tree[2 * i + 1] = tree[i] + leftVal; // Right child gets parent + old left
            }
        }

        // Extract inclusive running balance
        double[] runningBalances = new double[n];
        for (int i = 0; i < n; i++) {
            runningBalances[i] = tree[powerOfTwo + i] + input[i];
        }

        return runningBalances;
    }

    /**
     * Benchmark & Brent's Theorem Analysis:
     */
    public static String analyzeWorkSpanAndBrentsTheorem(double[] testData) {
        int cores = Runtime.getRuntime().availableProcessors();
        LedgerMetrics seq = computeSequentialMetrics(testData);
        LedgerMetrics par = computeParallelMetrics(testData);

        double t1 = seq.timeNs / 1_000_000.0;
        double tp = par.timeNs / 1_000_000.0;
        double speedup = tp > 0 ? t1 / tp : 1.0;
        double efficiency = (speedup / cores) * 100.0;

        // Theoretical Span T_∞ estimation: ~ log2(N)
        double spanRatio = Math.log(testData.length) / Math.log(2);

        return "============================================================\n" +
               "PARALLEL WORK-SPAN & BRENT'S THEOREM BENCHMARK\n" +
               "============================================================\n" +
               String.format("Dataset Size (N):       %,d transactions\n", testData.length) +
               String.format("Available CPU Cores (p):%d cores\n", cores) +
               String.format("Sequential Work (T_1):  %.3f ms\n", t1) +
               String.format("Parallel Time (T_p):    %.3f ms\n", tp) +
               String.format("Measured Speedup (S_p): %.2fx (Ideal theoretical: %dx)\n", speedup, cores) +
               String.format("Parallel Efficiency:    %.1f%%\n", efficiency) +
               "Brent's Theorem Bound:  T_p <= (T_1 / p) + T_∞\n" +
               String.format("Estimated Span (T_∞):   ~%.1f steps [O(log N)]\n", spanRatio) +
               "Parallelism (T_1 / T_∞):~" + String.format("%.0f", (testData.length / Math.max(1, spanRatio))) + " (Embarrassingly Parallel)\n" +
               "Where Parallelism Shines: Large data-parallel batches without locks.\n" +
               "Where Parallelism Fails:  Small N (< 10,000) where thread setup costs dominate.\n" +
               "============================================================";
    }

    public static void main(String[] args) {
        System.out.println("--- Parallel Ledger Analytics Demo (FinServe) ---");
        double[] amounts = new double[200_000];
        for (int i = 0; i < amounts.length; i++) amounts[i] = 50.0 + (i % 500);

        System.out.println(analyzeWorkSpanAndBrentsTheorem(amounts));

        double[] sampleLedger = {100.0, -25.0, 50.0, 200.0, -80.0};
        double[] balances = blellochParallelScan(sampleLedger);
        System.out.println("Input Amounts:    " + Arrays.toString(sampleLedger));
        System.out.println("Running Balances: " + Arrays.toString(balances));
    }
}
