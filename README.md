# FinServe – Intelligent Banking Analytics Platform

Welcome to the FinServe project! This project applies advanced string, suffix, and dynamic programming algorithms to banking analytics, fraud detection, and transaction processing.

## Project Structure

The project is structured to allow concurrent development among teammates:
- `finserve.Main`: The main entry point and integration menu.
- `finserve.model`: Shared data models (Customer, Account, Transaction, FinancialDocument).
- `finserve.m1_string`: (My Module) String matching algorithms for text analysis and fraud detection.
- `finserve.m2_suffix`: (Teammate's Module) Suffix structures for document indexing.
- `finserve.m3_dp`: (Teammate's Module) Dynamic programming for error correction and optimizations.

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
   - **Use Case:** Detect multiple fraud patterns simultaneously (e.g., OTP, PHISHING, UNAUTHORIZED) in high-throughput transaction streams.
   - **Complexity:** Time O(N + M + Z), Space O(N * alphabet size).

## Setup
This is a standard Java project that does not rely on external libraries. It is fully compatible with Eclipse and other standard Java IDEs. Simply import the folder into Eclipse as an existing project.
