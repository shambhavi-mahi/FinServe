package finserve.m6_random_parallel;

import finserve.model.Transaction;
import java.util.*;

/**
 * Module 6: Randomised and Parallel Algorithms
 * 
 * Financial Use Case: Real-Time Streaming Financial Audit & Compliance Inspection
 * 
 * Problem Formulation:
 * A bank processes high-speed transaction streams (potentially millions of transactions per day).
 * Regulators require an unbiased, uniform random sample of size k for compliance audits.
 * The total stream length N is unknown in advance, and storing the entire stream in memory
 * for audit sampling is impractical.
 * 
 * Algorithm R (Alan G. Waterman / Jeffrey Vitter):
 * 1. Store the first k transactions directly in the reservoir.
 * 2. For each incoming transaction i (where i = k + 1, k + 2, ..., N):
 *    - Pick a random integer j uniformly in the range [0, i - 1].
 *    - If j < k, replace reservoir[j] with transaction i.
 *    - Otherwise, discard transaction i.
 * 
 * The Elegant Proof of Uniform Sampling (By Induction):
 * - Base Case (i = k):
 *   The first k items are all placed in the reservoir.
 *   Probability for each item is k / k = 1. Base case holds.
 * 
 * - Inductive Hypothesis:
 *   Assume that after step i - 1, every transaction seen so far is in the reservoir
 *   with probability exactly k / (i - 1).
 * 
 * - Inductive Step (step i):
 *   1. The new transaction i is placed into the reservoir with probability k / i
 *      (since j is chosen uniformly from {0, ..., i-1} and accepted if j < k).
 *   2. For any previous transaction x already in the reservoir:
 *      P(x remains in reservoir after step i)
 *      = P(x in reservoir at step i-1) * P(x is NOT replaced at step i)
 *      = [k / (i - 1)] * [1 - P(transaction i enters AND evicts slot of x)]
 *      = [k / (i - 1)] * [1 - (k / i) * (1 / k)]
 *      = [k / (i - 1)] * [1 - (1 / i)]
 *      = [k / (i - 1)] * [(i - 1) / i]
 *      = k / i.
 * 
 * - Conclusion:
 *   By mathematical induction, at any point N, every single transaction in history
 *   has probability EXACTLY k / N of being in the reservoir.
 *   Space Complexity: O(k) (constant memory relative to stream length N).
 *   Time Complexity: O(N) single-pass streaming.
 */
public class ReservoirSampling {

    private static final Random RANDOM = new Random();

    public static class SampleResult<T> {
        public final List<T> sample;
        public final int sampleSizeK;
        public final int totalStreamSizeN;
        public final double uniformProbabilityPerItem;
        public final long executionTimeNs;

        public SampleResult(List<T> sample, int k, int n, long timeNs) {
            this.sample = sample;
            this.sampleSizeK = k;
            this.totalStreamSizeN = n;
            this.uniformProbabilityPerItem = n > 0 ? (double) k / n : 0.0;
            this.executionTimeNs = timeNs;
        }
    }

    /**
     * Samples exactly k items uniformly from any streaming source or collection of size N.
     * Memory usage is strictly O(k), never storing the full stream.
     */
    public static <T> SampleResult<T> sampleStream(Iterable<T> stream, int k) {
        long start = System.nanoTime();
        List<T> reservoir = new ArrayList<>(k);
        int count = 0;

        for (T item : stream) {
            count++;
            if (count <= k) {
                reservoir.add(item);
            } else {
                // Pick random index in [0, count - 1]
                int j = RANDOM.nextInt(count);
                if (j < k) {
                    reservoir.set(j, item);
                }
            }
        }

        long time = System.nanoTime() - start;
        return new SampleResult<>(reservoir, Math.min(k, count), count, time);
    }

    public static void main(String[] args) {
        System.out.println("--- Reservoir Sampling Demo (FinServe) ---");
        List<String> stream = new ArrayList<>();
        for (int i = 1; i <= 10000; i++) {
            stream.add("TX-" + i + " ($" + (i * 10) + ")");
        }
        int k = 5;
        SampleResult<String> sample = sampleStream(stream, k);
        System.out.printf("Sampled %d items from %d streaming transactions (P = %.4f%%):\n", 
            sample.sampleSizeK, sample.totalStreamSizeN, sample.uniformProbabilityPerItem * 100);
        for (String s : sample.sample) {
            System.out.println("  " + s);
        }
    }
}
