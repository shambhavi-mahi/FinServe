# FinServe – Intelligent Banking Analytics Platform

Welcome to the FinServe project! This project applies advanced string, suffix, and dynamic programming algorithms to banking analytics, fraud detection, and transaction processing.

## Project Structure

The project is structured to allow concurrent development among teammates:
- `finserve.Main`: The main entry point and integration menu.
- `finserve.model`: Shared data models (Customer, Account, Transaction, FinancialDocument).
- `finserve.m1_string`: (My Module) String matching algorithms for text analysis and fraud detection.
- `finserve.m2_suffix`: (Teammate's Module) Suffix structures for document indexing.
- `finserve.m3_dp`: (Teammate's Module) Dynamic programming for error correction and optimizations.
- `finserve.m5_np_approx`: Module 5 NP-Completeness, Reductions, FPT, and Approximation algorithms.
- `finserve.m6_random_parallel`: Module 6 Randomized and Parallel algorithms for banking scale.

## M1 – String Algorithms

This module implements key string searching algorithms with financial use cases:

1. **KMP (Knuth-Morris-Pratt)**
   - **Use Case:** Search transaction descriptions and payment codes.
   - **Complexity:** Time O(N + M), Space O(M).
2. **Z-Function**
   - **Use Case:** Find repeated patterns in transaction histories.
   - **Complexity:** Time O(N + M), Space O(N + M).
3. **Rabin-Karp**
   - **Use Case:** Search financial and routing codes using hashing.
   - **Complexity:** Time O(N + M) average / O(N * M) worst case, Space O(1).
4. **Aho-Corasick**
   - **Use Case:** Detect multiple fraud patterns simultaneously in high-throughput transaction streams.
   - **Complexity:** Time O(N + M + Z), Space O(N * alphabet size).

## M5 – NP-Completeness, Reductions & Approximation

This module tackles computationally intractable financial problems using formal complexity tools, approximation guarantees, and fixed-parameter tractability:

1. **Vertex Cover 2-Approximation & FPT (`VertexCoverApprox`)**
   - **Use Case:** Fraud ring mule network surveillance — intercepting all suspicious transaction channels with minimal monitored accounts.
   - **Theory & Guarantees:** 
     - From Karp's reduction zoo: 3-SAT -> CLIQUE -> INDEPENDENT-SET -> VERTEX-COVER.
     - Maximal Matching 2-Approximation achieves |Cover| <= 2 * |OPT| in O(V + E) linear time.
     - Parameterized Complexity (FPT): Solves k-Vertex Cover in O(2^k * (V + E)) bounded search tree time for regulator budget k.
     - Includes Practical Engineer's NP-Hardness Checklist for financial architectures.
2. **Capital Budgeting & Portfolio Allocation (`KnapsackCapitalBudgeting`)**
   - **Use Case:** Commercial bank loan portfolio allocation and capital reserve constraints.
   - **Theory & Guarantees:** 
     - Exact DP: Pseudo-polynomial O(n * W) time.
     - Greedy 2-Approximation: Yield density sort with highest-item fallback, guaranteeing >= 0.50 * OPT in O(n log n).
     - FPTAS (Fully Polynomial Time Approximation Scheme): Scaled values with precision parameter ε, achieving >= (1 - ε) * OPT in O(n^2 / ε) time.
3. **Ledger Discrepancy Reconciliation (`ReconciliationSubsetSum`)**
   - **Use Case:** Automated Clearing House (ACH) daily cutoff batch discrepancy reconciliation.
   - **Theory & Guarantees:**
     - Decision vs Optimization problems: Finding exact subset summing to T vs minimizing discrepancy.
     - Formal polynomial-time witness verification in O(|witness|) proving Subset-Sum in NP.
4. **Metropolitan ATM Logistics (`MetricTSPApprox`)**
   - **Use Case:** Cash replenishment vehicle route optimization across metropolitan branches (scaling beyond Bitmask DP).
   - **Theory & Guarantees:**
     - HAM-CYCLE -> TSP reduction intuition.
     - Metric TSP 2-Approximation via MST doubling and Eulerian shortcutting, guaranteeing Tour <= 2 * OPT in O(V^2) time.

## M6 – Randomised and Parallel Algorithms

This module leverages randomization and multi-core parallelism for high-speed, secure financial systems:

1. **Miller-Rabin Primality Test (`MillerRabinPrimality`)**
   - **Use Case:** RSA / SWIFT cryptographic key generation and secure interbank messaging.
   - **Theory & Guarantees:**
     - Monte Carlo algorithm with witness construction (n - 1 = 2^s * d).
     - Overcomes the Carmichael-number challenge (e.g., n = 561) that fools Fermat's test.
     - Bounded error probability <= (1/4)^k; after k=40 rounds, error < 10^-24 (safer than hardware cosmic-ray bit flips).
2. **Reservoir Sampling (`ReservoirSampling`)**
   - **Use Case:** Real-time compliance audit and fraud inspection on streaming transaction flows with unknown N.
   - **Theory & Guarantees:**
     - Algorithm R operates in O(k) memory and O(N) streaming time.
     - Inductive proof guarantees every single transaction in stream history has probability EXACTLY k / N of being sampled.
3. **Randomized Universal Hashing (`UniversalHashing`)**
   - **Use Case:** Anti-collision transaction deduplication and Hash-DoS attack protection in banking APIs.
   - **Theory & Guarantees:**
     - Las Vegas randomized Carter-Wegman 2-universal hash family h_{a,b}(x) = ((ax + b) mod p) mod m.
     - Guarantees pairwise collision probability <= 1/m regardless of adversarial input clustering.
4. **Randomized QuickSort (`RandomizedQuickSort`)**
   - **Use Case:** High-volume financial ledger sorting without vulnerability to sorted/reverse-sorted inputs.
   - **Theory & Guarantees:**
     - Las Vegas randomized pivot selection.
     - Rigorous indicator random variable analysis proving E[T(n)] = 2n ln(n) + O(n) for all inputs, avoiding O(n^2) worst-case degradation.
5. **Parallel Ledger Analytics (`ParallelLedgerAnalytics`)**
   - **Use Case:** High-throughput multi-core transaction aggregation and running balance ledger computation.
   - **Theory & Guarantees:**
     - Work (T_1) and Span (T_∞) analysis; Brent's Theorem bound: T_p <= (T_1 / p) + T_∞.
     - ForkJoin Parallel Reduce for volume and statistics with work O(N) and span O(log N).
     - Blelloch Parallel Prefix-Sum (Scan) for running account balances using Up-Sweep and Down-Sweep in O(log N) span.

## Setup
This is a standard Java project that does not rely on external libraries. It is fully compatible with Eclipse and other standard Java IDEs. Simply import the folder into Eclipse as an existing project.

