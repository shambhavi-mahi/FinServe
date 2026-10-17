# FinServe – Intelligent Banking Analytics Platform

Welcome to **FinServe**! FinServe is an enterprise-grade banking analytics, financial risk surveillance, and high-throughput transaction processing platform developed for **Sem-4 Data Structures & Algorithms III (DSA-3)**.

FinServe integrates classical string matching, suffix indexing, dynamic programming, network flow optimization, NP-hardness reductions & approximation algorithms, and randomized/parallel algorithms into a single interactive Java platform operating on real-world Kaggle banking datasets.

---

## Interactive Menu Architecture

FinServe features a modular **7-Core Option Main Menu** with dedicated interactive sub-menus for each algorithmic module:

```
================================================================================
              FINSERVE - INTELLIGENT BANKING ANALYTICS PLATFORM
================================================================================
 1. Customer Account Details & Transactions (O(1) Search) [with limit]
 2. Module 1: String Algorithms & Fraud Pattern Detection
 3. Module 2: Suffix Structures & Financial Document Indexing
 4. Module 3: Advanced Dynamic Programming & Financial Optimizations
 5. Module 4: Network Flow & Interbank Liquidity Clearing
 6. Module 5: NP-Completeness, Reductions & Approximation
 7. Module 6: Randomised and Parallel Algorithms
 8. Exit
================================================================================
```

Each module sub-menu runs in its own execution loop with **display restrictions** (`[with limit]`) to ensure clean, overflow-protected terminal output during grading and demonstrations.

---

## Project Structure

```
FinServe/
├── data_kaggle/
│   ├── bank_accounts.csv
│   └── account_transactions.csv
├── src/
│   └── finserve/
│       ├── Main.java                        # Main entry point & interactive menu routing
│       ├── model/                           # Shared models (Account, Transaction, Customer)
│       ├── m1_string/                       # Module 1: String Searching & Fraud Detection
│       ├── m2_suffix/                       # Module 2: Suffix Arrays, LCP, Suffix Trees
│       ├── m3_dp/                           # Module 3: Advanced Dynamic Programming
│       ├── m4_network_flow/                 # Module 4: Network Flow & Liquidity Optimization
│       ├── m5_np_approx/                    # Module 5: NP-Completeness, Reductions, FPT & Approx
│       └── m6_random_parallel/              # Module 6: Randomized & Parallel Multi-Core Algorithms
├── bin/                                     # Eclipse / CLI compiled bytecode
├── out/                                     # IntelliJ / standard build output
└── README.md                                # Platform documentation
```

---

## Module Breakdown & Theoretical Guarantees

### M1 – String Algorithms & Fraud Detection
1. **Knuth-Morris-Pratt (`KMP`)**
   - **Use Case:** Search transaction descriptions and payment reference codes.
   - **Complexity:** Time $O(N + M)$, Space $O(M)$ via $\pi$-failure function.
2. **Z-Function (`ZFunction`)**
   - **Use Case:** Detect repeated periodic transactions and velocity fraud patterns.
   - **Complexity:** Time $O(N + M)$, Space $O(N + M)$ with linear box maintenance.
3. **Rabin-Karp (`RabinKarp`)**
   - **Use Case:** Search financial routing codes and SWIFT tags using rolling polynomial hashing.
   - **Complexity:** Time $O(N + M)$ average / $O(N \cdot M)$ worst case, Space $O(1)$.
4. **Aho-Corasick (`AhoCorasick`)**
   - **Use Case:** Multi-pattern dictionary automaton simultaneously scanning transaction streams for 40+ known financial crime and money-laundering keywords.
   - **Complexity:** Time $O(N + M + Z)$, Space $O(\text{Trie Nodes} \times \Sigma)$ using failure & dictionary output links.

---

### M2 – Suffix Structures & Document Indexing
1. **Suffix Array & SA-IS (`SuffixArray`, `SAIS`)**
   - **Use Case:** Full-text indexing of regulatory disclosures, loan contracts, and customer agreements.
   - **Complexity:** Construction in $O(N)$ via SA-IS; Substring queries in $O(M \log N)$ via binary search.
2. **Longest Common Prefix (`LCP`)**
   - **Use Case:** Identify duplicate payment instructions and recurring fraud signatures across customer logs.
   - **Complexity:** Time $O(N)$ using Kasai's algorithm; $O(1)$ range queries via RMQ.
3. **Suffix Tree & Automaton (`SuffixTree`, `SuffixAutomaton`)**
   - **Use Case:** Linear-time exact string analytics and sub-string matching for AML regulatory filings.
   - **Complexity:** Construction and query in $O(N)$ time.

---

### M3 – Advanced Dynamic Programming & Financial Optimizations
1. **Levenshtein Distance (`Levenshtein`)**
   - **Use Case:** KYC & AML sanctions screening against OFAC watchlists with fuzzy name matching.
   - **Complexity:** Time $O(N \cdot M)$, Space $O(\min(N, M))$.
2. **Damerau-Levenshtein (`DamerauLevenshtein`)**
   - **Use Case:** Catch adjacent digit/character transpositions in IBAN and credit card account entries.
   - **Complexity:** Time $O(N \cdot M)$ covering insertions, deletions, substitutions, and transpositions.
3. **Weighted Edit Distance (`WeightedEditDistance`)**
   - **Use Case:** Merchant entity resolution with customized penalty matrices for OCR and phonetic errors.
   - **Complexity:** Time $O(N \cdot M)$.
4. **Bitmask DP (`BitmaskDP`)**
   - **Use Case:** Exact cash replenishment routing for armored vehicles across small regional clusters ($N \le 20$).
   - **Complexity:** Time $O(N^2 \cdot 2^N)$, Space $O(N \cdot 2^N)$ via Held-Karp bitmasking.
5. **Matrix Chain Multiplication (`MatrixChain`)**
   - **Use Case:** Optimal multi-currency conversion order to minimize interbank spread fees in multi-leg FX trades.
   - **Complexity:** Time $O(N^3)$, Space $O(N^2)$.

---

### M4 – Network Flow & Interbank Liquidity Clearing
1. **Interbank Liquidity Clearing & Bottleneck Min-Cut (`InterbankMaxFlow`)**
   - **Use Case:** Settlement of interbank credit lines (Fed Discount Window, Repo Lines, Bilateral RTGS).
   - **Theory & Guarantees:** 
     - Edmonds-Karp algorithm using BFS augmenting paths in $O(V \cdot E^2)$ time.
     - **Integrality Theorem**: Integer credit limits guarantee integer flow settlements.
     - **Max-Flow Min-Cut Theorem**: Cut capacity equals maximum throughput; saturated cut edges identify systemic risk credit choke points.
2. **RTGS Payment Clearing Engine (`DinicMaxFlow`)**
   - **Use Case:** High-throughput Real-Time Gross Settlement engine for dense correspondent networks.
   - **Theory & Guarantees:** Dinic's Algorithm runs in $O(V^2 \cdot E)$ on general networks using BFS Level Graphs and DFS Blocking Flows with `ptr[]` current-edge elimination.
3. **Loan Underwriter & Compliance Case Assignment (`BipartiteMatchingAssignment`)**
   - **Use Case:** Optimal assignment of high-risk commercial loans to certified underwriters.
   - **Theory & Guarantees:** Maximum Bipartite Matching reduced to unit Max-Flow. **König's Theorem** ($\text{Max Matching} = \text{Min Vertex Cover}$) identifies the staffing choke point capping loan origination capacity.
4. **Cross-Border Multi-Currency Payment Routing (`MinCostPaymentRouting`)**
   - **Use Case:** Minimizing international wire fees and SWIFT margins while dispatching target liquidity.
   - **Theory & Guarantees:** Min-Cost Max-Flow (MCMF) via Successive Shortest Path (SSP) with SPFA. Cycle-cancelling intuition guarantees no negative-fee cycles remain.
5. **Strategic Fintech Product Portfolio Selection (`FintechProjectSelection`)**
   - **Use Case:** Digital banking product portfolio selection with shared prerequisite IT infrastructure dependencies.
   - **Theory & Guarantees:** Project Selection closure problem reduced to Minimum $s-t$ Cut. $\infty$-capacity dependency edges guarantee prerequisites are never severed, maximizing $\text{Net Profit} = \text{Potential Revenue} - \text{Capacity}(\text{Min-Cut})$.

---

### M5 – NP-Completeness, Reductions & Approximation
1. **Fraud Ring Surveillance (`VertexCoverApprox`)**
   - **Use Case:** Intercepting all suspicious transaction channels in mule networks with minimal monitored accounts.
   - **Theory & Guarantees:** 
     - Karp's reduction chain: $\text{3-SAT} \to \text{CLIQUE} \to \text{INDEPENDENT-SET} \to \text{VERTEX-COVER}$.
     - Maximal Matching 2-Approximation achieves $|\text{Cover}| \le 2 \cdot |\text{OPT}|$ in $O(V + E)$ linear time.
     - Fixed-Parameter Tractability (FPT): Solves $k$-Vertex Cover in $O(2^k \cdot (V + E))$ bounded search tree time for regulator budget $k$.
     - Includes Practical Engineer's NP-Hardness Checklist.
2. **Capital Budgeting & Portfolio Allocation (`KnapsackCapitalBudgeting`)**
   - **Use Case:** Loan capital allocation under statutory capital reserve limits.
   - **Theory & Guarantees:** Exact pseudo-polynomial DP $O(n \cdot W)$; Greedy 2-Approximation $\ge 0.50 \cdot \text{OPT}$ in $O(n \log n)$; FPTAS achieving $\ge (1 - \varepsilon) \cdot \text{OPT}$ in $O(n^2 / \varepsilon)$ time.
3. **Ledger Discrepancy Reconciliation (`ReconciliationSubsetSum`)**
   - **Use Case:** Automated Clearing House (ACH) daily cutoff batch discrepancy reconciliation.
   - **Theory & Guarantees:** Decision vs. Optimization formulations; formal polynomial-time witness verification in $O(|\text{witness}|)$ proving Subset-Sum $\in \text{NP}$.
4. **Metropolitan ATM Cash Logistics (`MetricTSPApprox`)**
   - **Use Case:** Cash replenishment vehicle route optimization scaling beyond Bitmask DP for city-wide ATM networks.
   - **Theory & Guarantees:** Metric TSP 2-Approximation via MST doubling and Eulerian shortcutting, guaranteeing $\text{Tour} \le 2 \cdot \text{OPT}$ in $O(V^2)$ time.

---

### M6 – Randomised and Parallel Algorithms
1. **Miller-Rabin Primality Test (`MillerRabinPrimality`)**
   - **Use Case:** RSA / SWIFT cryptographic key generation and secure interbank messaging.
   - **Theory & Guarantees:** Monte Carlo test using non-trivial square roots of unity ($n - 1 = 2^s \cdot d$). Defeats Carmichael numbers; error probability $\le (1/4)^k < 10^{-24}$ for $k=40$.
2. **Reservoir Sampling (`ReservoirSampling`)**
   - **Use Case:** Real-time compliance audit and fraud inspection on streaming transaction flows with unknown length $N$.
   - **Theory & Guarantees:** Algorithm R operates in $O(k)$ memory; inductive proof guarantees every transaction in stream history has probability exactly $k / N$ of selection.
3. **Randomized Universal Hashing (`UniversalHashing`)**
   - **Use Case:** Anti-collision transaction deduplication and Hash-DoS attack protection in banking APIs.
   - **Theory & Guarantees:** Las Vegas randomized Carter-Wegman 2-universal hash family $h_{a,b}(x) = ((ax + b) \bmod p) \bmod m$. Guarantees collision probability $\le 1/m$ regardless of adversarial input clustering.
4. **Randomized QuickSort (`RandomizedQuickSort`)**
   - **Use Case:** High-volume financial ledger sorting immune to sorted/reverse-sorted adversarial degradation.
   - **Theory & Guarantees:** Las Vegas random pivot selection; indicator random variable proof bounds expected runtime to $E[T(n)] = 2n \ln n + O(n)$.
5. **Parallel Ledger Analytics (`ParallelLedgerAnalytics`)**
   - **Use Case:** High-throughput multi-core transaction aggregation and running balance ledger computation.
   - **Theory & Guarantees:** 
     - Work ($T_1$) and Span ($T_\infty$) analysis; Brent's Theorem bound $T_p \le (T_1 / p) + T_\infty$.
     - ForkJoin Parallel Reduce for volume metrics with work $O(N)$ and span $O(\log N)$.
     - Blelloch Parallel Prefix-Sum (Scan) for running account balances using Up-Sweep and Down-Sweep in $O(\log N)$ span.

---

## Setup & Execution

### Prerequisites
- Java Development Kit (JDK 11 or higher recommended).
- Standard Java IDE (Eclipse, IntelliJ IDEA, VS Code) or terminal execution.

### Compile & Run from Terminal
```bash
# Compile all modules into bin/
javac -d bin src/finserve/model/*.java src/finserve/m1_string/*.java src/finserve/m2_suffix/*.java src/finserve/m3_dp/*.java src/finserve/m4_network_flow/*.java src/finserve/m5_np_approx/*.java src/finserve/m6_random_parallel/*.java src/finserve/Main.java

# Run FinServe Analytics
java -cp bin finserve.Main
```

### Eclipse IDE Setup
1. Open Eclipse $\to$ **File** $\to$ **Import...**
2. Choose **General** $\to$ **Existing Projects into Workspace**.
3. Select the `FinServe` directory as the root directory and click **Finish**.
4. Right click `Main.java` $\to$ **Run As** $\to$ **Java Application**.
