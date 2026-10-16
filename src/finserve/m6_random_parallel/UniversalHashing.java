package finserve.m6_random_parallel;

import java.util.*;

/**
 * Module 6: Randomised and Parallel Algorithms
 * 
 * Financial Use Case: Anti-Collision Transaction Deduplication & Fraud Cache
 * 
 * Algorithm Paradigm: Las Vegas Randomized Algorithm
 * - Output: Always 100% correct (never yields false negatives or corrupted lookups).
 * - Running Time: A random variable with guaranteed expected constant time O(1).
 * 
 * Why Hashing is Fundamentally Probabilistic:
 * - For any fixed, deterministic hash function h: U -> {0..m-1}, an adversary
 *   (or real-world clustered transaction patterns) can choose a sequence of keys
 *   that all hash to the same bucket, degrading hash table lookups to O(n) linked-list traversal.
 *   This is known as a Hash-DoS attack against financial transaction APIs.
 * - By selecting a hash function h AT RANDOM from a 2-Universal Hash Family at startup,
 *   the probability of collision between any two distinct keys is provably <= 1/m,
 *   regardless of what inputs the adversary feeds into the system!
 * 
 * Construction of 2-Universal Hash Family (Carter & Wegman):
 * - Choose a large prime p > universe size (e.g., p = 2,147,483,647 = 2^31 - 1, Mersenne prime).
 * - Randomly choose:
 *     a ∈ {1, 2, ..., p - 1}
 *     b ∈ {0, 1, ..., p - 1}
 * - Function: h_{a,b}(k) = ((a * k + b) mod p) mod m.
 * - Theorem: For any two distinct keys x != y:
 *     P_{h ∈ H}[h(x) == h(y)] <= 1 / m.
 */
public class UniversalHashing {

    // Large Mersenne Prime 2^31 - 1
    private static final long P = 2147483647L;
    private final long a;
    private final long b;
    private final int m; // Bucket capacity

    public UniversalHashing(int bucketCount) {
        this.m = bucketCount;
        Random rand = new Random();
        this.a = 1 + (Math.abs(rand.nextLong()) % (P - 1));
        this.b = Math.abs(rand.nextLong()) % P;
    }

    /**
     * Hashes an integer key (or string hashCode) into bucket index [0, m-1].
     */
    public int hash(long key) {
        long nonNegativeKey = key & 0x7FFFFFFF; // ensure positive
        return (int) (((a * nonNegativeKey + b) % P) % m);
    }

    public int hashString(String key) {
        long h = 0;
        for (int i = 0; i < key.length(); i++) {
            h = (h * 31 + key.charAt(i)) % P;
        }
        return hash(h);
    }

    public static class CollisionMetrics {
        public final int totalKeys;
        public final int buckets;
        public final int maxChainLength;
        public final int collisionsCount;
        public final double expectedCollisionsPerPair;
        public final double actualCollisionRate;

        public CollisionMetrics(int totalKeys, int buckets, int maxChain, int collisions) {
            this.totalKeys = totalKeys;
            this.buckets = buckets;
            this.maxChainLength = maxChain;
            this.collisionsCount = collisions;
            this.expectedCollisionsPerPair = 1.0 / buckets;
            long pairs = ((long) totalKeys * (totalKeys - 1)) / 2;
            this.actualCollisionRate = pairs > 0 ? (double) collisions / pairs : 0.0;
        }
    }

    /**
     * Compares Universal Hashing vs Naive Modulo Hashing on clustered transaction IDs.
     */
    public static Map<String, CollisionMetrics> compareWithNaive(List<Integer> keys, int bucketCount) {
        // Universal Hash Table
        UniversalHashing uh = new UniversalHashing(bucketCount);
        int[] universalBuckets = new int[bucketCount];
        int universalCollisions = 0;

        for (int key : keys) {
            int bucket = uh.hash(key);
            if (universalBuckets[bucket] > 0) {
                universalCollisions += universalBuckets[bucket];
            }
            universalBuckets[bucket]++;
        }
        int maxUniversalChain = 0;
        for (int c : universalBuckets) maxUniversalChain = Math.max(maxUniversalChain, c);

        // Naive Modulo Hash Table (h(k) = k % m)
        int[] naiveBuckets = new int[bucketCount];
        int naiveCollisions = 0;
        for (int key : keys) {
            int bucket = Math.abs(key) % bucketCount;
            if (naiveBuckets[bucket] > 0) {
                naiveCollisions += naiveBuckets[bucket];
            }
            naiveBuckets[bucket]++;
        }
        int maxNaiveChain = 0;
        for (int c : naiveBuckets) maxNaiveChain = Math.max(maxNaiveChain, c);

        Map<String, CollisionMetrics> results = new LinkedHashMap<>();
        results.put("Universal Hashing (Randomized Las Vegas)", 
            new CollisionMetrics(keys.size(), bucketCount, maxUniversalChain, universalCollisions));
        results.put("Naive Modulo Hashing (h(k) = k % m)", 
            new CollisionMetrics(keys.size(), bucketCount, maxNaiveChain, naiveCollisions));

        return results;
    }

    public static void main(String[] args) {
        System.out.println("--- Universal Hashing Demo (FinServe) ---");
        List<Integer> keys = new ArrayList<>();
        for (int i = 1; i <= 5000; i++) {
            keys.add(i * 32); // Clustered patterns
        }
        Map<String, CollisionMetrics> res = compareWithNaive(keys, 128);
        for (Map.Entry<String, CollisionMetrics> e : res.entrySet()) {
            System.out.println(e.getKey() + " -> Collisions: " + e.getValue().collisionsCount + ", Max Chain: " + e.getValue().maxChainLength);
        }
    }
}
