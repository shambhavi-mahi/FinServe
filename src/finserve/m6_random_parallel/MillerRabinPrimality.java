package finserve.m6_random_parallel;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

/**
 * Module 6: Randomised and Parallel Algorithms
 * 
 * Financial Use Case: RSA Cryptographic Key Generation & SWIFT Interbank Security
 * 
 * Algorithm Paradigm: Monte Carlo Randomized Algorithm
 * - Runs in deterministic polynomial time O(k * log^3 n).
 * - Output: Always correct when it reports "COMPOSITE" (witness found).
 *   Has a small, bounded probability of error when it reports "PROBABLE PRIME".
 * 
 * The Witness Construction:
 * - For an odd candidate n > 2, factor out powers of 2 from n - 1:
 *     n - 1 = 2^s * d,  where d is odd.
 * - Choose a random base a in [2, n - 2].
 * - Compute x = a^d mod n using fast modular exponentiation.
 * - If x == 1 or x == n - 1, the test passes for base a.
 * - Otherwise, repeatedly square x: x = x^2 mod n, for r = 1 to s - 1.
 *   If x == n - 1 at any step, the test passes.
 * - If x never reaches n - 1, 'a' is a guaranteed WITNESS that n is composite!
 * 
 * The Carmichael-Number Challenge:
 * - Fermat's Little Theorem states: if n is prime, a^(n-1) ≡ 1 (mod n).
 * - However, Fermat's test FAILS on Carmichael numbers (e.g., 561, 1105, 1729, 2465).
 *   561 = 3 * 11 * 17 is composite, yet a^560 ≡ 1 (mod 561) for all gcd(a, 561) = 1!
 * - Miller-Rabin overcomes this: by checking non-trivial square roots of 1 mod n,
 *   at least 75% of all bases are witnesses to the compositeness of ANY composite number,
 *   including Carmichael numbers.
 * 
 * Why Probabilistic Primality is Enough in Banking Practice:
 * - Each independent round has error probability <= 1/4.
 * - After k rounds, the probability of falsely declaring a composite as prime is <= (1/4)^k = 2^(-2k).
 * - For k = 40 rounds: Error <= (1/4)^40 ≈ 8.27 * 10^-25.
 * - In financial cryptographic standards (FIPS 186-4, SWIFT, TLS), this probability
 *   is astronomically lower than the chance of a cosmic ray flipping a CPU bit!
 */
public class MillerRabinPrimality {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static class PrimalityReport {
        public final BigInteger candidate;
        public final boolean isProbablyPrime;
        public final int roundsTested;
        public final double maxErrorProbability;
        public final BigInteger compositeWitness; // null if probable prime
        public final boolean fermatFooled;       // true if Fermat test was tricked
        public final long executionTimeNs;

        public PrimalityReport(BigInteger candidate, boolean prime, int rounds, double errorProb, 
                               BigInteger witness, boolean fermatFooled, long timeNs) {
            this.candidate = candidate;
            this.isProbablyPrime = prime;
            this.roundsTested = rounds;
            this.maxErrorProbability = errorProb;
            this.compositeWitness = witness;
            this.fermatFooled = fermatFooled;
            this.executionTimeNs = timeNs;
        }
    }

    /**
     * Executes the Miller-Rabin primality test for k independent rounds.
     */
    public static PrimalityReport testPrimality(BigInteger n, int k) {
        long start = System.nanoTime();

        // Base cases
        if (n.compareTo(BigInteger.TWO) < 0) {
            return new PrimalityReport(n, false, 0, 0.0, null, false, System.nanoTime() - start);
        }
        if (n.equals(BigInteger.TWO) || n.equals(BigInteger.valueOf(3))) {
            return new PrimalityReport(n, true, k, 0.0, null, false, System.nanoTime() - start);
        }
        if (n.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            return new PrimalityReport(n, false, 0, 0.0, BigInteger.TWO, false, System.nanoTime() - start);
        }

        // Decompose n - 1 = 2^s * d with d odd
        BigInteger d = n.subtract(BigInteger.ONE);
        int s = 0;
        while (d.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            d = d.divide(BigInteger.TWO);
            s++;
        }

        BigInteger nMinusOne = n.subtract(BigInteger.ONE);
        BigInteger nMinusThree = n.subtract(BigInteger.valueOf(3));

        for (int round = 0; round < k; round++) {
            // Pick random base a in [2, n - 2]
            BigInteger a;
            do {
                a = new BigInteger(n.bitLength(), RANDOM);
            } while (a.compareTo(BigInteger.TWO) < 0 || a.compareTo(nMinusOne) >= 0);

            // Compute x = a^d mod n
            BigInteger x = a.modPow(d, n);

            if (x.equals(BigInteger.ONE) || x.equals(nMinusOne)) {
                continue; // Passes this round
            }

            boolean composite = true;
            for (int r = 1; r < s; r++) {
                x = x.modPow(BigInteger.TWO, n);
                if (x.equals(nMinusOne)) {
                    composite = false;
                    break;
                }
            }

            if (composite) {
                // 'a' is a witness to compositeness!
                // Check if naive Fermat test would have been fooled: a^(n-1) mod n == 1
                boolean fermatTricked = a.modPow(nMinusOne, n).equals(BigInteger.ONE);
                long time = System.nanoTime() - start;
                return new PrimalityReport(n, false, round + 1, 0.0, a, fermatTricked, time);
            }
        }

        // Passed all k rounds
        double errorProb = Math.pow(0.25, k);
        long time = System.nanoTime() - start;
        return new PrimalityReport(n, true, k, errorProb, null, false, time);
    }

    /**
     * Demonstrates the Carmichael challenge:
     * Carmichael numbers like 561 pass Fermat's test for all coprime bases,
     * but Miller-Rabin immediately detects their compositeness.
     */
    public static String demonstrateCarmichael() {
        BigInteger carmichael561 = BigInteger.valueOf(561); // 3 * 11 * 17
        PrimalityReport report = testPrimality(carmichael561, 20);

        StringBuilder sb = new StringBuilder();
        sb.append("--- Carmichael Number Challenge (n = 561 = 3 * 11 * 17) ---\n");
        sb.append("Fermat's Test Flaw: For all bases 'a' coprime to 561, a^560 ≡ 1 (mod 561)!\n");
        sb.append("Naive Fermat test concludes: '561 IS PRIME' (FALSE POSITIVE!).\n");
        sb.append("Miller-Rabin Result: ").append(report.isProbablyPrime ? "PRIME" : "COMPOSITE (SUCCESS!)\n");
        if (report.compositeWitness != null) {
            sb.append("Found Non-Trivial Square Root / Miller-Rabin Witness base a = ")
              .append(report.compositeWitness).append("\n");
        }
        sb.append("Verdict: Miller-Rabin exposes Carmichael composites where Fermat fails.\n");
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("--- Miller-Rabin Primality Demo (FinServe) ---");
        BigInteger prime64 = new BigInteger("18446744073709551557");
        PrimalityReport r1 = testPrimality(prime64, 40);
        System.out.println("Candidate: " + prime64 + " -> " + (r1.isProbablyPrime ? "PROBABLE PRIME" : "COMPOSITE"));
        System.out.println("Max error probability: " + r1.maxErrorProbability);
        System.out.println(demonstrateCarmichael());
    }
}
