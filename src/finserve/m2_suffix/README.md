# FinServe Module 2 – Suffix Structures

This module implements key **Suffix-Based Algorithms & Data Structures** for the **FinServe – Intelligent Banking Analytics Platform**. These structures enable high-speed indexing, substring searching, pattern extraction, and substring statistics across banking transaction logs and financial documents.

---

## Algorithms Summary & FinServe Banking Use Cases

| Algorithm | Class | Primary FinServe Banking Use Case | Time Complexity | Space Complexity |
|---|---|---|---|---|
| **Suffix Array** | `SuffixArray.java` | Indexes transaction logs and payment descriptions for fast binary search pattern lookups. | Construction: $O(N \log^2 N)$<br>Search: $O(M \log N)$ | $O(N)$ |
| **SA-IS** | `SAIS.java` | Linear-time suffix array construction for indexing massive financial reports and audit logs. | Construction: $O(N)$<br>Search: $O(M \log N)$ | $O(N)$ |
| **LCP / Kasai** | `LCP.java` | Computes Longest Common Prefix array to discover recurring vendor descriptions and repeated transaction patterns. | Construction: $O(N)$ | $O(N)$ |
| **Suffix Tree** | `SuffixTree.java` | Enables fast sub-linear keyword searching, occurrence counting, and document indexing. | Construction: $O(N^2)$<br>Search: $O(M + K)$ | $O(N)$ |
| **Suffix Automaton** | `SuffixAutomaton.java` | Analyzes total unique transaction substrings and tests sub-phrase existence in $O(M)$ time using DAWG. | Construction: $O(N)$<br>Search: $O(M)$ | $O(N)$ |

---

## Detailed Algorithm Descriptions

### 1. `SuffixArray.java`
- **Description**: Constructs a lexicographically sorted array of all text suffixes using a prefix-doubling sorting algorithm. Binary search is performed on the array to locate pattern occurrences.
- **Sample Input**: `"TX1001:ONLINE PAYMENT AMAZON TX1002:ONLINE PAYMENT FLIPKART..."`, Pattern: `"ONLINE PAYMENT"`
- **Sample Output**: Suffix Array indices `[7, 94, 36]` pointing to matching transaction starting positions.

### 2. `SAIS.java`
- **Description**: Implements Suffix Array Induced Sorting (SA-IS) to construct suffix arrays in guaranteed linear $O(N)$ time via LMS (Leftmost S-type) substring classification and bucket induced sorting.
- **Sample Input**: `"FINANCIAL REPORT 2026: ONLINE PAYMENT AMAZON..."`, Keyword: `"PAYMENT"`
- **Sample Output**: Suffix Array built in $O(N)$ time with binary search matches at character indices `[30, 83, 52]`.

### 3. `LCP.java`
- **Description**: Uses Kasai's algorithm to compute the Longest Common Prefix array from a text and its Suffix Array in $O(N)$ time. Extracts repeated payment phrases and longest duplicate transaction substrings.
- **Sample Input**: Transaction string containing repeated `"ONLINE PAYMENT AMAZON"` entries.
- **Sample Output**: Longest repeated substring `":ONLINE PAYMENT AMAZON TX100"` and list of recurring patterns of length $\ge 10$.

### 4. `SuffixTree.java`
- **Description**: Builds a compressed trie of all text suffixes. Allows searching for any pattern in $O(M + K)$ time where $M$ is pattern length and $K$ is the number of occurrences.
- **Sample Input**: Financial document query `"AMAZON"` or `"WITHDRAWAL"`.
- **Sample Output**: `Exists: true`, `Count: 2`, `Indices: [22, 109]`.

### 5. `SuffixAutomaton.java`
- **Description**: Builds a Minimal Directed Acyclic Word Graph (DAWG) representing all substrings of a text in linear state space (at most $2N-1$ states). Computes exact count of unique transaction substrings and occurrence frequencies.
- **Sample Input**: Transaction stream string.
- **Sample Output**: `Total States: 208`, `Unique Substrings: 9747`, `Pattern "ONLINE PAYMENT" Count: 3`.

---

## Public API Integration Methods

Each class provides modular static methods ready for integration with `finserve.Main`:

```java
// Suffix Array & SA-IS
int[] sa = SuffixArray.buildSuffixArray(text);
int[] saLinear = SAIS.buildSuffixArray(text);
List<Integer> matches = SuffixArray.search(text, sa, pattern);

// LCP / Kasai
int[] lcp = LCP.buildLCP(text, sa);
String longestRepeated = LCP.findLongestRepeatedSubstring(text, sa, lcp);

// Suffix Tree
SuffixTree tree = SuffixTree.buildSuffixTree(text);
List<Integer> treeMatches = tree.search(pattern);

// Suffix Automaton
SuffixAutomaton automaton = SuffixAutomaton.buildAutomaton(text);
boolean exists = automaton.contains(pattern);
int occurrences = automaton.countOccurrences(pattern);
long uniqueSubstrings = automaton.countUniqueSubstrings();
```
