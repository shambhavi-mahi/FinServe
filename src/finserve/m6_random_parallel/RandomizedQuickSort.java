package finserve.m6_random_parallel;

import java.util.*;

/**
 * Module 6: Randomised and Parallel Algorithms
 * 
 * Financial Use Case: Sorting High-Volume Financial Ledgers & Transaction Timelines
 * 
 * Algorithm Paradigm: Las Vegas Randomized Algorithm
 * - Correctness: Always 100% correct (array is always properly sorted).
 * - Running Time: A random variable whose expected value is rigorously O(n log n).
 * 
 * Rigorous Expected-Time Analysis (DSA-1 Revisited):
 * - Why Deterministic QuickSort Fails:
 *   If the pivot is chosen deterministically (e.g. first or last element), an already-sorted
 *   or reverse-sorted batch of banking transactions produces worst-case O(n^2) time
 *   and causes StackOverflowError from O(n) recursion depth.
 * 
 * - The Indicator Random Variable Proof:
 *   Let z_1 < z_2 < ... < z_n be the sorted elements.
 *   Let X_{ij} be an indicator random variable = 1 if z_i and z_j are compared, and 0 otherwise.
 *   Total comparisons C = ∑_{i=1}^{n-1} ∑_{j=i+1}^{n} X_{ij}.
 *   Taking expectation: E[C] = ∑_{i=1}^{n-1} ∑_{j=i+1}^{n} P(z_i is compared to z_j).
 *   z_i and z_j are compared IF AND ONLY IF either z_i or z_j is chosen as pivot BEFORE
 *   any other element in the candidate range {z_i, z_{i+1}, ..., z_j}.
 *   The candidate set size is (j - i + 1).
 *   Since the pivot is chosen uniformly at random, every element in this set is equally likely
 *   to be picked first:
 *     P(z_i is compared to z_j) = 2 / (j - i + 1).
 *   Substituting:
 *     E[C] = ∑_{i=1}^{n-1} ∑_{k=1}^{n-i} 2 / (k + 1)
 *          <= 2 * n * ∑_{k=1}^{n} (1 / k)
 *          = 2 * n * ln(n) + O(n).
 *   Therefore: Expected time E[T(n)] = O(n log n) FOR EVERY POSSIBLE INPUT ARRAY!
 */
public class RandomizedQuickSort {

    private static final Random RANDOM = new Random();

    public static class SortResult {
        public final int elementsSorted;
        public final long comparisonCount;
        public final long executionTimeNs;
        public final String complexityGuarantee;

        public SortResult(int n, long comparisons, long timeNs) {
            this.elementsSorted = n;
            this.comparisonCount = comparisons;
            this.executionTimeNs = timeNs;
            this.complexityGuarantee = "E[T] = O(n log n) (Las Vegas)";
        }
    }

    private static class Counter {
        long comparisons = 0;
    }

    /**
     * Sorts an array of transactions/amounts in-place using Randomized QuickSort.
     */
    public static SortResult sort(double[] arr) {
        long start = System.nanoTime();
        Counter counter = new Counter();
        if (arr != null && arr.length > 1) {
            quickSort(arr, 0, arr.length - 1, counter);
        }
        long time = System.nanoTime() - start;
        return new SortResult(arr == null ? 0 : arr.length, counter.comparisons, time);
    }

    private static void quickSort(double[] arr, int low, int high, Counter counter) {
        while (low < high) {
            // Pick random pivot index in [low, high]
            int randomIndex = low + RANDOM.nextInt(high - low + 1);
            swap(arr, low, randomIndex);

            int pIndex = partition(arr, low, high, counter);

            // Tail call optimization: recurse on smaller half, loop on larger half
            if (pIndex - low < high - pIndex) {
                quickSort(arr, low, pIndex - 1, counter);
                low = pIndex + 1;
            } else {
                quickSort(arr, pIndex + 1, high, counter);
                high = pIndex - 1;
            }
        }
    }

    private static int partition(double[] arr, int low, int high, Counter counter) {
        double pivot = arr[low];
        int i = low + 1;
        int j = high;

        while (true) {
            while (i <= high) {
                counter.comparisons++;
                if (arr[i] > pivot) break;
                i++;
            }
            while (j > low) {
                counter.comparisons++;
                if (arr[j] <= pivot) break;
                j--;
            }
            if (i >= j) break;
            swap(arr, i, j);
        }
        swap(arr, low, j);
        return j;
    }

    private static void swap(double[] arr, int i, int j) {
        double temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    public static void main(String[] args) {
        System.out.println("--- Randomized QuickSort Demo (FinServe) ---");
        double[] amounts = {1500.50, 240.0, 8900.25, 45.10, 3120.0, 750.80, 120.0};
        System.out.println("Unsorted: " + Arrays.toString(amounts));
        SortResult res = sort(amounts);
        System.out.println("Sorted:   " + Arrays.toString(amounts));
        System.out.println("Comparisons: " + res.comparisonCount + " (" + res.complexityGuarantee + ")");
    }
}
