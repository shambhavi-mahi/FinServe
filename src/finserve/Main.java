package finserve;

import finserve.m1_string.AhoCorasick;
import finserve.m1_string.KMP;
import finserve.m1_string.NaiveSearch;
import finserve.m1_string.RabinKarp;
import finserve.m1_string.ZFunction;
import finserve.m2_suffix.LCP;
import finserve.m2_suffix.SuffixArray;
import finserve.m3_dp.BitmaskDP;
import finserve.m3_dp.DamerauLevenshtein;
import finserve.m3_dp.Levenshtein;
import finserve.m3_dp.MatrixChain;
import finserve.m3_dp.WeightedEditDistance;
import finserve.m4_network_flow.BipartiteMatchingAssignment;
import finserve.m4_network_flow.DinicMaxFlow;
import finserve.m4_network_flow.FintechProjectSelection;
import finserve.m4_network_flow.InterbankMaxFlow;
import finserve.m4_network_flow.MinCostPaymentRouting;
import finserve.m5_np_approx.KnapsackCapitalBudgeting;
import finserve.m5_np_approx.MetricTSPApprox;
import finserve.m5_np_approx.ReconciliationSubsetSum;
import finserve.m5_np_approx.VertexCoverApprox;
import finserve.m6_random_parallel.MillerRabinPrimality;
import finserve.m6_random_parallel.ParallelLedgerAnalytics;
import finserve.m6_random_parallel.RandomizedQuickSort;
import finserve.m6_random_parallel.ReservoirSampling;
import finserve.m6_random_parallel.UniversalHashing;
import finserve.model.Account;
import finserve.model.Transaction;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class Main {
    private static List<Transaction> transactions = new ArrayList<>();
    private static Map<String, Account> accounts = new HashMap<>();
    private static Map<String, String> accountTransactions = new HashMap<>();

    public static void main(String[] args) {
        // Map to convert UUIDs to simple sequential IDs
        Map<String, String> uuidToSequentialAccId = new HashMap<>();
        Map<String, String> uuidToSimpleCustomerId = new HashMap<>();
        int accCounter = 1;
        int custCounter = 1;

        // Load accounts dataset (bank_accounts.csv)
        try {
            System.out.println("Loading bank_accounts.csv...");
            java.io.BufferedReader accReader = new java.io.BufferedReader(new java.io.FileReader("data_kaggle/bank_accounts.csv"));
            String line;
            if ((line = accReader.readLine()) != null) {} // Skip header
            while ((line = accReader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 6) {
                    String originalAccId = parts[0].trim();
                    String accId = String.valueOf(accCounter++); // 1, 2, 3...
                    uuidToSequentialAccId.put(originalAccId, accId);

                    String originalCustId = parts[1].trim();
                    if (!uuidToSimpleCustomerId.containsKey(originalCustId)) {
                        uuidToSimpleCustomerId.put(originalCustId, "Customer-" + (custCounter++));
                    }
                    String customerId = uuidToSimpleCustomerId.get(originalCustId);
                    
                    double bal = parts[5].trim().isEmpty() ? 0.0 : Double.parseDouble(parts[5].trim());
                    
                    accounts.put(accId, new Account(accId, customerId, bal));
                    accountTransactions.put(accId, ""); // Initialize empty history
                }
            }
            accReader.close();
        } catch (java.io.FileNotFoundException e) {
            System.out.println("Warning: Dataset not found at 'data_kaggle/bank_accounts.csv'.");
        } catch (Exception ex) {
            System.out.println("Warning: Data format error in bank_accounts.csv: " + ex.getMessage());
        }

        int txCounter = 1;
        // Load transactions dataset (account_transactions.csv)
        try {
            System.out.println("Loading account_transactions.csv (this might take a few seconds)...");
            java.io.BufferedReader fileReader = new java.io.BufferedReader(new java.io.FileReader("data_kaggle/account_transactions.csv"));
            String line;
            if ((line = fileReader.readLine()) != null) {} // Skip header
            while ((line = fileReader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 8) {
                    String txId = String.valueOf(txCounter++); // 1, 2, 3...
                    String originalAccId = parts[1].trim();
                    // Map to the simple sequential account ID
                    String accId = uuidToSequentialAccId.getOrDefault(originalAccId, originalAccId);
                    
                    String txType = parts[4].trim();
                    String txCode = parts[5].trim();
                    String amount = parts[6].trim();
                    String channel = parts[7].trim();
                    
                    String description = txType + " " + txCode + " " + amount + " " + channel;
                    transactions.add(new Transaction(txId, description));
                    
                    if (accountTransactions.containsKey(accId)) {
                        String currentHistory = accountTransactions.get(accId);
                        accountTransactions.put(accId, currentHistory.isEmpty() ? description : currentHistory + " | " + description);
                    }
                }
            }
            fileReader.close();
            System.out.println("Data loading complete.");
        } catch (java.io.FileNotFoundException e) {
            System.out.println("Warning: Dataset not found at 'data_kaggle/account_transactions.csv'.");
        } catch (Exception ex) {
            System.out.println("Warning: Error reading account_transactions.csv: " + ex.getMessage());
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n================================================================================");
            System.out.println("              FINSERVE - INTELLIGENT BANKING ANALYTICS PLATFORM");
            System.out.println("================================================================================");
            System.out.println(" 1. Customer Account Details & Transactions (O(1) Search) [with limit]");
            System.out.println(" 2. Module 1: String Algorithms & Fraud Pattern Detection");
            System.out.println(" 3. Module 2: Suffix Structures & Financial Document Indexing");
            System.out.println(" 4. Module 3: Advanced Dynamic Programming & Financial Optimizations");
            System.out.println(" 5. Module 4: Network Flow & Interbank Liquidity Clearing");
            System.out.println(" 6. Module 5: NP-Completeness, Reductions & Approximation");
            System.out.println(" 7. Module 6: Randomised and Parallel Algorithms");
            System.out.println(" 8. Exit");
            System.out.println("================================================================================");
            System.out.print("Select an option (1-8): ");

            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            
            int choice = -1;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number (1-8).");
                continue;
            }

            switch (choice) {
                case 1:
                    accountManagementMenu(scanner);
                    break;
                case 2:
                    module1SubMenu(scanner);
                    break;
                case 3:
                    module2SubMenu(scanner);
                    break;
                case 4:
                    module3SubMenu(scanner);
                    break;
                case 5:
                    module4SubMenu(scanner);
                    break;
                case 6:
                    module5SubMenu(scanner);
                    break;
                case 7:
                    module6SubMenu(scanner);
                    break;
                case 8:
                    System.out.println("Exiting FinServe Analytics. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option. Please enter a number from 1 to 8.");
            }
        }
    }

    private static void module1SubMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println("       MODULE 1: STRING ALGORITHMS & FRAUD PATTERN DETECTION");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println(" 1. Customer Name Search (Naive) [with limit]");
            System.out.println(" 2. Transaction Search (KMP) [with limit]");
            System.out.println(" 3. Fraud Pattern Detection (Aho-Corasick) [with limit]");
            System.out.println(" 4. Repeated Transaction Pattern (Z-Function) [with limit]");
            System.out.println(" 5. Search Financial Codes (Rabin-Karp) [with limit]");
            System.out.println(" 6. Back to Main Menu");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.print("Select an option (1-6): ");

            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            int choice = -1;
            try { choice = Integer.parseInt(input); } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number (1-6).");
                continue;
            }

            switch (choice) {
                case 1:
                    naiveNameSearchMenu(scanner);
                    break;
                case 2:
                    transactionSearchMenu(scanner);
                    break;
                case 3:
                    fraudPatternDetectionMenu(scanner);
                    break;
                case 4:
                    zFunctionMenu(scanner);
                    break;
                case 5:
                    rabinKarpMenu(scanner);
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid option. Please select 1-6.");
            }
        }
    }

    private static void module2SubMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println("       MODULE 2: SUFFIX STRUCTURES & DOCUMENT INDEXING");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println(" 1. Financial Document Search & Indexing (Suffix Array / SA-IS) [with limit]");
            System.out.println(" 2. Transaction Pattern Analysis (LCP) [Analysis Scope Limit]");
            System.out.println(" 3. Back to Main Menu");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.print("Select an option (1-3): ");

            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            int choice = -1;
            try { choice = Integer.parseInt(input); } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number (1-3).");
                continue;
            }

            switch (choice) {
                case 1:
                    suffixArrayMenu(scanner);
                    break;
                case 2:
                    lcpMenu(scanner);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid option. Please select 1-3.");
            }
        }
    }

    private static void module3SubMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println("   MODULE 3: ADVANCED DYNAMIC PROGRAMMING & FINANCIAL OPTIMIZATIONS");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println(" 1. KYC & AML Sanctions Screening (Levenshtein)");
            System.out.println(" 2. IBAN & Account Number Transposition Detection (Damerau-Levenshtein)");
            System.out.println(" 3. Merchant Entity Resolution (Weighted Edit Distance)");
            System.out.println(" 4. ATM Cash Replenishment Routing (Bitmask DP - Small N)");
            System.out.println(" 5. Optimal Multi-Currency Conversion Cost (Matrix Chain Multiplication)");
            System.out.println(" 6. Back to Main Menu");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.print("Select an option (1-6): ");

            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            int choice = -1;
            try { choice = Integer.parseInt(input); } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number (1-6).");
                continue;
            }

            switch (choice) {
                case 1:
                    levenshteinMenu(scanner);
                    break;
                case 2:
                    damerauLevenshteinMenu(scanner);
                    break;
                case 3:
                    weightedEditDistanceMenu(scanner);
                    break;
                case 4:
                    bitmaskDPMenu();
                    break;
                case 5:
                    matrixChainMenu();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid option. Please select 1-6.");
            }
        }
    }

    private static void module4SubMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println("        MODULE 4: NETWORK FLOW & INTERBANK LIQUIDITY CLEARING");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println(" 1. Interbank Liquidity Clearing & Bottleneck Min-Cut (Edmonds-Karp) [with limit]");
            System.out.println(" 2. RTGS Payment Clearing Engine (Dinic's Blocking Flow) [with limit]");
            System.out.println(" 3. Loan Underwriter & Compliance Case Assignment (Bipartite Matching & Konig) [with limit]");
            System.out.println(" 4. Cross-Border Multi-Currency Payment Routing (Min-Cost Max-Flow / SSP) [with limit]");
            System.out.println(" 5. Strategic Fintech Product Portfolio Selection (Min-Cut Reduction) [with limit]");
            System.out.println(" 6. Back to Main Menu");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.print("Select an option (1-6): ");

            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            int choice = -1;
            try { choice = Integer.parseInt(input); } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number (1-6).");
                continue;
            }

            switch (choice) {
                case 1:
                    interbankMaxFlowMenu(scanner);
                    break;
                case 2:
                    dinicMaxFlowMenu(scanner);
                    break;
                case 3:
                    bipartiteMatchingMenu(scanner);
                    break;
                case 4:
                    minCostPaymentRoutingMenu(scanner);
                    break;
                case 5:
                    fintechProjectSelectionMenu(scanner);
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid option. Please select 1-6.");
            }
        }
    }

    private static void module5SubMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println("   MODULE 5: NP-COMPLETENESS, REDUCTIONS & APPROXIMATION ALGORITHMS");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println(" 1. Fraud Ring Surveillance (Vertex Cover 2-Approx & FPT) [with Checklist]");
            System.out.println(" 2. Loan Capital Budgeting (Knapsack: Exact DP vs Greedy 2-Approx vs FPTAS)");
            System.out.println(" 3. Interbank Clearing Discrepancy Reconciliation (Subset-Sum & Witness Verification)");
            System.out.println(" 4. Metropolitan ATM Cash Logistics (Metric-TSP 2-Approximation via MST)");
            System.out.println(" 5. Back to Main Menu");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.print("Select an option (1-5): ");

            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            int choice = -1;
            try { choice = Integer.parseInt(input); } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number (1-5).");
                continue;
            }

            switch (choice) {
                case 1:
                    fraudRingSurveillanceMenu(scanner);
                    break;
                case 2:
                    capitalBudgetingMenu(scanner);
                    break;
                case 3:
                    reconciliationSubsetSumMenu(scanner);
                    break;
                case 4:
                    metricTSPMenu(scanner);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid option. Please select 1-5.");
            }
        }
    }

    private static void module6SubMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n--------------------------------------------------------------------------------");
            System.out.println("          MODULE 6: RANDOMISED AND PARALLEL ALGORITHMS");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println(" 1. RSA / SWIFT Cryptographic Key Primality (Miller-Rabin Monte Carlo Test)");
            System.out.println(" 2. Streaming Transaction Compliance Audit (Reservoir Sampling Algorithm R) [with limit]");
            System.out.println(" 3. Anti-Collision Transaction Deduplication (Universal Hashing Las Vegas vs Modulo)");
            System.out.println(" 4. High-Volume Transaction Log Sorting (Randomized QuickSort) [Analysis Scope Limit]");
            System.out.println(" 5. Parallel Multi-Core Ledger Analytics & Running Balance Scan (ForkJoin & Blelloch Scan) [Analysis Scope Limit]");
            System.out.println(" 6. Back to Main Menu");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.print("Select an option (1-6): ");

            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            int choice = -1;
            try { choice = Integer.parseInt(input); } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number (1-6).");
                continue;
            }

            switch (choice) {
                case 1:
                    millerRabinMenu(scanner);
                    break;
                case 2:
                    reservoirSamplingMenu(scanner);
                    break;
                case 3:
                    universalHashingMenu(scanner);
                    break;
                case 4:
                    randomizedQuickSortMenu(scanner);
                    break;
                case 5:
                    parallelLedgerAnalyticsMenu(scanner);
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid option. Please select 1-6.");
            }
        }
    }

    private static void accountManagementMenu(Scanner scanner) {
        System.out.println("\n--- Account Management (O(1) Data Retrieval) ---");
        System.out.println("Total Accounts in DB: " + accounts.size() + " (IDs: 1 to " + accounts.size() + ")");
        System.out.print("Enter Account Number (e.g., 1 or 42): ");
        String accNo = scanner.nextLine().trim();
        
        if (accounts.containsKey(accNo)) {
            Account acc = accounts.get(accNo);
            System.out.println("\n[Account Details Found]");
            System.out.println("Account ID: " + acc.getAccountId());
            System.out.println("Customer Name: " + acc.getCustomerId());
            System.out.println("Current Balance: $" + String.format("%.2f", acc.getBalance()));
            
            String history = accountTransactions.get(accNo);
            if (history == null || history.isEmpty()) {
                System.out.println("Transaction History: No transactions found.");
            } else {
                System.out.print("How many recent transactions to view? (e.g., 10, 50, all): ");
                String limitInput = scanner.nextLine().trim().toLowerCase();
                String[] txs = history.split(" \\| ");
                
                int limit = txs.length; // default 'all'
                if (!limitInput.equals("all")) {
                    try {
                        limit = Integer.parseInt(limitInput);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input, showing all transactions.");
                    }
                }
                
                int start = Math.max(0, txs.length - limit);
                System.out.println("Showing " + (txs.length - start) + " recent transactions:");
                for (int i = txs.length - 1; i >= start; i--) {
                    System.out.println("  -> " + txs[i]);
                }
            }
        } else {
            System.out.println("Error: Account " + accNo + " not found in the database.");
        }
    }

    private static void naiveNameSearchMenu(Scanner scanner) {
        System.out.print("Enter partial or full Customer Name to search (e.g., 'Customer-10'): ");
        String pattern = scanner.nextLine();
        
        System.out.print("How many results to view? (e.g., 10, 50, all): ");
        String limitInput = scanner.nextLine().trim().toLowerCase();
        int limit = Integer.MAX_VALUE;
        if (!limitInput.equals("all")) {
            try {
                limit = Integer.parseInt(limitInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, showing all matches.");
            }
        }

        System.out.println("Searching across database using Naive algorithm...");
        int count = 0;
        for (Account acc : accounts.values()) {
            String name = acc.getCustomerId(); // Customer Name is stored here
            if (!NaiveSearch.search(name.toUpperCase(), pattern.toUpperCase()).isEmpty()) {
                System.out.println("Match found -> Acc: " + acc.getAccountId() + " | Name: " + name);
                count++;
                if (count >= limit) break;
            }
        }
        if (count == 0) {
            System.out.println("No customer found with that name pattern.");
        } else {
            System.out.println("Displayed " + count + " match(es).");
        }
    }

    private static void transactionSearchMenu(Scanner scanner) {
        System.out.print("Enter pattern to search (e.g., 'InterbankPortal' or 'IBK_IN'): ");
        String pattern = scanner.nextLine();
        
        System.out.print("How many results to view? (e.g., 10, 50, all): ");
        String limitInput = scanner.nextLine().trim().toLowerCase();
        int limit = Integer.MAX_VALUE;
        if (!limitInput.equals("all")) {
            try {
                limit = Integer.parseInt(limitInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, showing all matches.");
            }
        }
        
        System.out.println("Searching for: '" + pattern + "'...");
        int count = 0;
        for (Transaction tx : transactions) {
            if (!KMP.search(tx.getDescription().toUpperCase(), pattern.toUpperCase()).isEmpty()) {
                System.out.println(tx);
                count++;
                if (count >= limit) break;
            }
        }
        if (count == 0) {
            System.out.println("No matches found.");
        } else {
            System.out.println("Displayed " + count + " match(es).");
        }
    }

    private static void fraudPatternDetectionMenu(Scanner scanner) {
        System.out.print("How many results to view? (e.g., 10, 50, all): ");
        String limitInput = scanner.nextLine().trim().toLowerCase();
        int limit = Integer.MAX_VALUE;
        if (!limitInput.equals("all")) {
            try {
                limit = Integer.parseInt(limitInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, showing all matches.");
            }
        }

        System.out.println("Running fraud pattern detection on all transactions...");
        String[] fraudPatterns = {"SCAM", "HighRiskChannel", "SUSPICIOUS", "UNAUTHORIZED"};
        AhoCorasick ac = new AhoCorasick();
        ac.build(fraudPatterns);
        int count = 0;
        for (Transaction tx : transactions) {
            // Using toUpperCase to ensure case-insensitive matching
            List<String> matches = ac.search(tx.getDescription().toUpperCase());
            if (!matches.isEmpty()) {
                System.out.println(tx.getId() + " -> " + String.join(", ", matches) + " detected");
                count++;
                if (count >= limit) break;
            }
        }
        if (count == 0) {
            System.out.println("No fraud patterns detected.");
        } else {
            System.out.println("Displayed " + count + " match(es).");
        }
    }

    private static void zFunctionMenu(Scanner scanner) {
        System.out.print("Enter transaction pattern to find repetition (e.g., 'Credit'): ");
        String pattern = scanner.nextLine();

        System.out.print("How many results to view? (e.g., 10, 50, all): ");
        String limitInput = scanner.nextLine().trim().toLowerCase();
        int limit = Integer.MAX_VALUE;
        if (!limitInput.equals("all")) {
            try {
                limit = Integer.parseInt(limitInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, showing all matches.");
            }
        }

        int count = 0;
        for (Transaction tx : transactions) {
            List<Integer> matches = ZFunction.search(tx.getDescription().toUpperCase(), pattern.toUpperCase());
            if (!matches.isEmpty()) {
                System.out.println(tx.getId() + " -> Match at index: " + matches);
                count++;
                if (count >= limit) break;
            }
        }
        if (count == 0) {
            System.out.println("No matches found.");
        } else {
            System.out.println("Displayed " + count + " match(es).");
        }
    }

    private static void rabinKarpMenu(Scanner scanner) {
        System.out.print("Enter financial code/keyword to search (e.g., 'CSH_WDL'): ");
        String pattern = scanner.nextLine();
        
        System.out.print("How many results to view? (e.g., 10, 50, all): ");
        String limitInput = scanner.nextLine().trim().toLowerCase();
        int limit = Integer.MAX_VALUE;
        if (!limitInput.equals("all")) {
            try {
                limit = Integer.parseInt(limitInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, showing all matches.");
            }
        }

        int count = 0;
        for (Transaction tx : transactions) {
            List<Integer> matches = RabinKarp.search(tx.getDescription().toUpperCase(), pattern.toUpperCase());
            if (!matches.isEmpty()) {
                System.out.println(tx.getId() + " -> Code found at: " + matches);
                count++;
                if (count >= limit) break;
            }
        }
        if (count == 0) {
            System.out.println("No matches found.");
        } else {
            System.out.println("Displayed " + count + " match(es).");
        }
    }

    private static void suffixArrayMenu(Scanner scanner) {
        System.out.print("Enter keyword for advanced index search (e.g., 'Branch'): ");
        String pattern = scanner.nextLine();
        
        System.out.print("How many results to view? (e.g., 10, 50, all): ");
        String limitInput = scanner.nextLine().trim().toLowerCase();
        int limit = Integer.MAX_VALUE;
        if (!limitInput.equals("all")) {
            try {
                limit = Integer.parseInt(limitInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, showing all matches.");
            }
        }

        int count = 0;
        for (Transaction tx : transactions) {
            List<Integer> matches = SuffixArray.search(tx.getDescription().toUpperCase(), pattern.toUpperCase());
            if (!matches.isEmpty()) {
                System.out.println(tx.getId() + " -> " + tx.getDescription() + " (Found via Suffix Array)");
                count++;
                if (count >= limit) break;
            }
        }
        if (count == 0) {
            System.out.println("No matching transactions found.");
        } else {
            System.out.println("Displayed " + count + " match(es).");
        }
    }

    private static void lcpMenu(Scanner scanner) {
        System.out.print("Enter number of recent transactions to analyze (e.g., 10000) (Large datasets may take time): ");
        String limitInput = scanner.nextLine().trim();
        int limit = 10000;
        try {
            limit = Integer.parseInt(limitInput);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Using default limit of 10000.");
        }

        System.out.println("Finding the Longest Repeated Pattern across the most recent " + limit + " transactions...");
        StringBuilder combined = new StringBuilder();
        int count = 0;
        // Start from end to get recent transactions
        for (int i = transactions.size() - 1; i >= 0 && count < limit; i--) {
            combined.append(transactions.get(i).getDescription().toUpperCase()).append("#");
            count++;
        }
        
        String text = combined.toString();
        if (text.isEmpty()) {
            System.out.println("No transactions to analyze.");
            return;
        }

        System.out.println("Building Suffix Array & LCP Array for " + text.length() + " characters...");
        int[] sa = SuffixArray.buildSuffixArray(text);
        int[] lcp = LCP.buildLCP(text, sa);
        String lrs = LCP.findLongestRepeatedSubstring(text, sa, lcp);
        
        if (lrs != null && !lrs.isEmpty()) {
            System.out.println("\n[SUCCESS] Longest Repeated Pattern found: '" + lrs + "'");
        } else {
            System.out.println("\nNo significant repeated patterns found.");
        }
    }

    private static void levenshteinMenu(Scanner scanner) {
        System.out.print("Enter a new customer name for KYC screening (e.g., USAMA BIN LADEN): ");
        String input = scanner.nextLine();
        System.out.print("Enter a known sanctioned entity name (e.g., OSAMA BIN LADEN): ");
        String expected = scanner.nextLine();
        int dist = Levenshtein.calculate(input.toUpperCase(), expected.toUpperCase());
        System.out.println("Levenshtein Distance: " + dist);
        System.out.println(dist < 3 ? "WARNING: Potential Sanctions Match! MANUAL REVIEW REQUIRED." : "Status: CLEAR.");
    }

    private static void damerauLevenshteinMenu(Scanner scanner) {
        System.out.print("Enter entered IBAN/Account Number (e.g., US456789): ");
        String input = scanner.nextLine();
        System.out.print("Enter expected valid IBAN (e.g., US465789): ");
        String expected = scanner.nextLine();
        int dist = DamerauLevenshtein.calculate(input.toUpperCase(), expected.toUpperCase());
        System.out.println("Damerau-Levenshtein Distance (handles adjacent transpositions): " + dist);
        if (dist == 1) {
            System.out.println("Did you mean " + expected + "? A transposition error was detected.");
        }
    }

    private static void weightedEditDistanceMenu(Scanner scanner) {
        System.out.print("Enter raw transaction merchant string (e.g., AMZN Mktp US): ");
        String input = scanner.nextLine();
        System.out.print("Enter canonical merchant name (e.g., Amazon): ");
        String expected = scanner.nextLine();
        int dist = WeightedEditDistance.calculateWeighted(input.toUpperCase(), expected.toUpperCase());
        System.out.println("Weighted Edit Distance (Penalty Score): " + dist);
    }

    private static void bitmaskDPMenu() {
        System.out.println("Calculating optimal cash replenishment route for 4 branches/ATMs...");
        int[][] travelCosts = {
            {0, 12, 18, 25},
            {12, 0, 35, 10},
            {18, 35, 0, 15},
            {25, 10, 15, 0}
        };
        int cost = BitmaskDP.optimize(travelCosts);
        System.out.println("Minimum secure travel distance/cost: " + cost);
    }

    private static void matrixChainMenu() {
        System.out.println("Optimizing Multi-Currency Conversion (FX Routing)...");
        // Representing currency conversion pair sizes (e.g., USD->EUR->GBP->JPY)
        int[] conversions = {100, 300, 50, 600}; 
        int cost = MatrixChain.matrixChainOrder(conversions);
        System.out.println("Minimum liquidity matching operations required: " + cost);
    }

    private static void interbankMaxFlowMenu(Scanner scanner) {
        System.out.println("\n--- Module 4: Interbank Liquidity Clearing & Bottleneck Min-Cut (Edmonds-Karp) ---");
        System.out.println("Theoretical Principles:");
        System.out.println(" • Max-Flow Problem: Source (Central Bank), Sink (Clearing House), Capacities (Bilateral Credit Limits).");
        System.out.println(" • Flow Conservation: Inflow = Outflow for all intermediary banks; Integrality Theorem guarantees integer settlement.");
        System.out.println(" • Edmonds-Karp O(V * E^2): Shortest augmenting paths in residual graph via BFS.");
        System.out.println(" • Max-Flow Min-Cut Dual Theorem: Maximum throughput equals the capacity of the critical bottleneck cut.");

        InterbankMaxFlow net = new InterbankMaxFlow();
        net.addCreditChannel("CentralReserve", "Bank_A (JPMorgan)", 100, "FedDiscountWindow");
        net.addCreditChannel("CentralReserve", "Bank_B (BankOfAmerica)", 80, "RepoCreditLine");
        net.addCreditChannel("CentralReserve", "Bank_E (Citigroup)", 90, "PrimaryDealerFacility");
        net.addCreditChannel("Bank_A (JPMorgan)", "Bank_C (WellsFargo)", 50, "BilateralRTGS");
        net.addCreditChannel("Bank_A (JPMorgan)", "Bank_D (GoldmanSachs)", 60, "CorrespondentLine");
        net.addCreditChannel("Bank_B (BankOfAmerica)", "Bank_D (GoldmanSachs)", 70, "LiquiditySwap");
        net.addCreditChannel("Bank_B (BankOfAmerica)", "Bank_F (MorganStanley)", 40, "OvernightFedFunds");
        net.addCreditChannel("Bank_E (Citigroup)", "Bank_F (MorganStanley)", 60, "SyndicatedCredit");
        net.addCreditChannel("Bank_C (WellsFargo)", "ClearingHouse (DTCC)", 60, "ACHSettlementLine");
        net.addCreditChannel("Bank_D (GoldmanSachs)", "ClearingHouse (DTCC)", 90, "CHIPSSettlementLine");
        net.addCreditChannel("Bank_F (MorganStanley)", "ClearingHouse (DTCC)", 80, "FedwireClearingLine");

        InterbankMaxFlow.SettlementResult res = net.computeMaxSettlementFlow("CentralReserve", "ClearingHouse (DTCC)");

        System.out.println("\n[MAXIMUM LIQUIDITY SETTLEMENT CAPACITY]:");
        System.out.printf("Maximum Hourly Settled Volume: $%,d Million / hour\n", res.maxLiquidityFlow);
        System.out.printf("Edmonds-Karp Execution Time:   %.3f ms [O(V * E^2)]\n", res.executionTimeNs / 1_000_000.0);

        System.out.print("\nHow many interbank credit channels to view? (e.g., 5, 10, all, default 5) [with limit]: ");
        String limitInput = scanner.nextLine().trim().toLowerCase();
        int limit = 5;
        if (limitInput.equals("all")) {
            limit = res.allChannels.size();
        } else if (!limitInput.isEmpty()) {
            try { limit = Integer.parseInt(limitInput); } catch (NumberFormatException e) { limit = 5; }
        }
        int show = Math.min(limit, res.allChannels.size());
        System.out.println("\n[UTILIZED INTERBANK CHANNELS (Showing " + show + " of " + res.allChannels.size() + ")]: ");
        for (int i = 0; i < show; i++) {
            System.out.println("  " + (i + 1) + ". " + res.allChannels.get(i));
        }

        System.out.println("\n[MIN-CUT DUAL ANALYSIS - SYSTEMIC RISK BOTTLENECK CHANNELS]:");
        System.out.println("According to the Max-Flow Min-Cut Theorem, these 100% saturated credit channels form");
        System.out.println("the systemic choke-point (Cut Capacity = Max Flow = $" + res.maxLiquidityFlow + "M):");
        for (InterbankMaxFlow.FlowEdge cutEdge : res.minCutBottleneckChannels) {
            System.out.println("  CRITICAL BOTTLENECK -> " + cutEdge);
        }
        System.out.println("Partition S (Reachable from Central Reserve): " + res.reachableBanksS);
        System.out.println("Partition T (Clearing House Segment):         " + res.unreachableBanksT);
        System.out.println("Actionable Policy Recommendation: Expanding credit capacity on these bottleneck channels directly increases system throughput.");
    }

    private static void dinicMaxFlowMenu(Scanner scanner) {
        System.out.println("\n--- Module 4: High-Throughput RTGS Payment Settlement Engine (Dinic's Algorithm) ---");
        System.out.println("Theoretical Principles:");
        System.out.println(" • Dinic's Algorithm operates in O(V^2 * E) on general networks and O(E * sqrt(V)) on unit networks.");
        System.out.println(" • Two Alternating Phases:");
        System.out.println("   1. BFS constructs the Level Graph (level[v] = shortest distance from source).");
        System.out.println("   2. DFS pushes Blocking Flows along admissible edges (level[to] == level[from] + 1).");
        System.out.println(" • 'ptr[]' current-edge array prunes exhausted edges to prevent backtracking overhead.");

        DinicMaxFlow dinic = new DinicMaxFlow(16);
        dinic.addCorridor("CentralVault", "HubEast", 150, "Corridor-East");
        dinic.addCorridor("CentralVault", "HubWest", 120, "Corridor-West");
        dinic.addCorridor("CentralVault", "HubSouth", 100, "Corridor-South");
        dinic.addCorridor("HubEast", "RegionalNorth", 90, "Corridor-EN");
        dinic.addCorridor("HubEast", "RegionalCentral", 80, "Corridor-EC");
        dinic.addCorridor("HubWest", "RegionalCentral", 70, "Corridor-WC");
        dinic.addCorridor("HubWest", "RegionalSouth", 60, "Corridor-WS");
        dinic.addCorridor("HubSouth", "RegionalSouth", 80, "Corridor-SS");
        dinic.addCorridor("RegionalNorth", "ClearingTerminal", 110, "Corridor-NC");
        dinic.addCorridor("RegionalCentral", "ClearingTerminal", 130, "Corridor-CC");
        dinic.addCorridor("RegionalSouth", "ClearingTerminal", 90, "Corridor-SC");

        DinicMaxFlow.DinicResult res = dinic.computeDinicMaxFlow("CentralVault", "ClearingTerminal");

        System.out.println("\n[HIGH-THROUGHPUT RTGS CLEARING RESULTS]:");
        System.out.printf("Total Real-Time Gross Settlement Flow: $%,d Million / sec\n", res.maxFlow);
        System.out.printf("BFS Level Graph Construction Phases:   %d phases (<= V-1)\n", res.numPhases);
        System.out.printf("Execution Time:                        %.4f ms (%s)\n", res.executionTimeNs / 1_000_000.0, res.complexity);

        System.out.print("\nHow many RTGS corridors to inspect? (e.g., 5, 10, all, default 5) [with limit]: ");
        String limitInput = scanner.nextLine().trim().toLowerCase();
        int limit = 5;
        if (limitInput.equals("all")) {
            limit = 11;
        } else if (!limitInput.isEmpty()) {
            try { limit = Integer.parseInt(limitInput); } catch (NumberFormatException e) { limit = 5; }
        }
        System.out.println("\nInspecting first " + Math.min(limit, 11) + " configured clearing corridors:");
        String[] sampleCorridors = {
            "CentralVault -> HubEast [Corridor-East]: Cap = $150M",
            "CentralVault -> HubWest [Corridor-West]: Cap = $120M",
            "CentralVault -> HubSouth [Corridor-South]: Cap = $100M",
            "HubEast -> RegionalNorth [Corridor-EN]: Cap = $90M",
            "HubEast -> RegionalCentral [Corridor-EC]: Cap = $80M",
            "HubWest -> RegionalCentral [Corridor-WC]: Cap = $70M",
            "HubWest -> RegionalSouth [Corridor-WS]: Cap = $60M",
            "HubSouth -> RegionalSouth [Corridor-SS]: Cap = $80M",
            "RegionalNorth -> ClearingTerminal [Corridor-NC]: Cap = $110M",
            "RegionalCentral -> ClearingTerminal [Corridor-CC]: Cap = $130M",
            "RegionalSouth -> ClearingTerminal [Corridor-SC]: Cap = $90M"
        };
        for (int i = 0; i < Math.min(limit, sampleCorridors.length); i++) {
            System.out.println("  " + (i + 1) + ". " + sampleCorridors[i]);
        }
        System.out.println("\nComparative Benchmark Insight:");
        System.out.println("Edmonds-Karp performs BFS per augmenting path (up to O(V*E) augmentations = O(V*E^2)).");
        System.out.println("Dinic's algorithm pushes entire blocking flows in each phase (at most V-1 phases = O(V^2*E)),");
        System.out.println("making it the industry standard for high-volume banking transaction clearing.");
    }

    private static void bipartiteMatchingMenu(Scanner scanner) {
        System.out.println("\n--- Module 4: Loan Underwriter & Compliance Case Assignment (Bipartite Matching & Konig) ---");
        System.out.println("Theoretical Principles:");
        System.out.println(" - Maximum Bipartite Matching reduced to Max-Flow with unit capacities.");
        System.out.println(" - Integrality Theorem: Guarantees 0/1 integer flow solutions without fractional split cases.");
        System.out.println(" - Konig's Theorem: In bipartite graphs, Max Matching Size = Min Vertex Cover Size.");
        System.out.println("   The Minimum Vertex Cover identifies the staffing bottlenecks capping underwriting volume.");

        List<BipartiteMatchingAssignment.Applicant> applicants = Arrays.asList(
            new BipartiteMatchingAssignment.Applicant("APP-101", "Apex Solar Infrastructure", "GreenEnergyBond"),
            new BipartiteMatchingAssignment.Applicant("APP-102", "BioGen Oncology Therapeutics", "CommercialCredit"),
            new BipartiteMatchingAssignment.Applicant("APP-103", "Global Freight Logistics", "ForexHedging"),
            new BipartiteMatchingAssignment.Applicant("APP-104", "Metropolis Commercial Towers", "RealEstateMortgage"),
            new BipartiteMatchingAssignment.Applicant("APP-105", "Quantum AI Compute Labs", "TechVentureDebt"),
            new BipartiteMatchingAssignment.Applicant("APP-106", "Nordic Maritime Shipping", "ForexHedging"),
            new BipartiteMatchingAssignment.Applicant("APP-107", "Highland Wind Energy", "GreenEnergyBond"),
            new BipartiteMatchingAssignment.Applicant("APP-108", "Apex Retail Supermarkets", "CommercialCredit")
        );

        List<BipartiteMatchingAssignment.Officer> officers = Arrays.asList(
            new BipartiteMatchingAssignment.Officer("OFF-01", "Sarah Chen, VP Underwriting", "CommercialCredit", "ForexHedging"),
            new BipartiteMatchingAssignment.Officer("OFF-02", "David Miller, Senior Analyst", "GreenEnergyBond"),
            new BipartiteMatchingAssignment.Officer("OFF-03", "Elena Rossi, Credit Director", "CommercialCredit", "TechVentureDebt"),
            new BipartiteMatchingAssignment.Officer("OFF-04", "Marcus Vance, Mortgage Lead", "RealEstateMortgage"),
            new BipartiteMatchingAssignment.Officer("OFF-05", "Amina Patel, Energy Specialist", "GreenEnergyBond"),
            new BipartiteMatchingAssignment.Officer("OFF-06", "Lucas Wright, FX Risk Officer", "ForexHedging")
        );

        BipartiteMatchingAssignment.MatchingResult res = BipartiteMatchingAssignment.solveUnderwritingAssignment(applicants, officers);

        System.out.println("\n[UNDERWRITING ASSIGNMENT RESULTS]:");
        System.out.printf("Total Loan Applicants:         %d cases\n", res.totalApplicants);
        System.out.printf("Available Certified Officers:   %d underwriters\n", res.totalOfficers);
        System.out.printf("Optimal Matches Made:          %d cases successfully paired\n", res.totalAssigned);
        System.out.printf("Matching Efficiency:           %.1f%%\n", (res.totalAssigned * 100.0) / res.totalApplicants);

        System.out.print("\nHow many underwriting assignments to view? (e.g., 5, 10, all, default all) [with limit]: ");
        String limitInput = scanner.nextLine().trim().toLowerCase();
        int limit = res.assignments.size();
        if (!limitInput.equals("all") && !limitInput.isEmpty()) {
            try { limit = Integer.parseInt(limitInput); } catch (NumberFormatException e) { limit = res.assignments.size(); }
        }
        int show = Math.min(limit, res.assignments.size());
        System.out.println("\n[UNDERWRITER <-> APPLICANT ASSIGNMENTS (Showing " + show + " of " + res.assignments.size() + ")]: ");
        for (int i = 0; i < show; i++) {
            System.out.println("  " + (i + 1) + ". " + res.assignments.get(i));
        }

        if (!res.unassignedApplicants.isEmpty()) {
            System.out.println("\n[UNASSIGNED APPLICANTS (Backlogged due to Staffing Constraints)]:");
            for (String un : res.unassignedApplicants) {
                System.out.println("  * " + un);
            }
        }

        System.out.println("\n[KONIG'S THEOREM BOTTLENECK ANALYSIS]:");
        System.out.println("Minimum Vertex Cover Size: " + res.minVertexCoverBottlenecks.size() + " (Exactly matches max assignment size = " + res.totalAssigned + ")");
        System.out.println("The following personnel/cases form the dual bottleneck:");
        for (String b : res.minVertexCoverBottlenecks) {
            System.out.println("  -> " + b);
        }
        System.out.println("Management Takeaway: Hiring underwriters matching the bottleneck specializations directly increases loan origination revenue.");
    }

    private static void minCostPaymentRoutingMenu(Scanner scanner) {
        System.out.println("\n--- Module 4: Cross-Border Multi-Currency Payment Routing (Min-Cost Max-Flow / SSP) ---");
        System.out.println("Theoretical Principles:");
        System.out.println(" • Min-Cost Max-Flow (MCMF): Finds flow of desired volume F that minimizes total transaction cost ∑ c(e)*f(e).");
        System.out.println(" • Successive Shortest Path (SSP): Augments flow along minimum-cost paths in residual network via SPFA.");
        System.out.println(" • Cycle-Cancelling Intuition: A flow is optimal iff residual graph contains NO negative-cost cycles.");

        MinCostPaymentRouting router = new MinCostPaymentRouting(12);
        // Interbank corridors: from, to, capacity ($M), fee per $1M ($K), corridor description
        router.addCorridor("NewYork_Fed", "London_Hub", 60, 2.50, "USD_GBP_Primary");
        router.addCorridor("NewYork_Fed", "Frankfurt_Hub", 40, 1.80, "USD_EUR_Direct");
        router.addCorridor("NewYork_Fed", "Zurich_Hub", 30, 2.10, "USD_CHF_Corridor");
        router.addCorridor("London_Hub", "Tokyo_BOJ", 50, 4.00, "GBP_JPY_DeepLiquidity");
        router.addCorridor("Frankfurt_Hub", "Tokyo_BOJ", 30, 3.20, "EUR_JPY_DirectChannel");
        router.addCorridor("Zurich_Hub", "Singapore_MAS", 35, 2.80, "CHF_SGD_Settlement");
        router.addCorridor("Frankfurt_Hub", "London_Hub", 25, 0.50, "Inter-European_Relay");
        router.addCorridor("Singapore_MAS", "Tokyo_BOJ", 40, 1.50, "SGD_JPY_AsiaPacificRail");

        System.out.print("Enter target liquidity to transfer from NewYork to Tokyo in $M (e.g. 70, 90, default 70): ");
        String amtInput = scanner.nextLine().trim();
        int targetTransfer = 70;
        if (!amtInput.isEmpty()) {
            try { targetTransfer = Integer.parseInt(amtInput); } catch (Exception ignored) {}
        }

        MinCostPaymentRouting.RoutingPlan plan = router.routeOptimalPayment("NewYork_Fed", "Tokyo_BOJ", targetTransfer);

        System.out.println("\n[OPTIMAL ROUTING PLAN (SSP ALGORITHM RESULT)]:");
        System.out.printf("Liquidity Successfully Routed: $%,d Million (Target: $%,d Million)\n", plan.totalLiquidityRouted, targetTransfer);
        System.out.printf("Total Cumulative Wire Fees:     $%,.2f Thousand USD\n", plan.totalFeesPaid);
        if (plan.totalLiquidityRouted > 0) {
            System.out.printf("Average Blended Routing Cost:   $%,.3f Thousand per $1 Million transferred\n", plan.totalFeesPaid / plan.totalLiquidityRouted);
        }
        System.out.printf("MCMF Optimization Run Time:     %.3f ms\n", plan.executionTimeNs / 1_000_000.0);

        System.out.print("\nHow many active payment channels to view? (e.g., 5, 10, all, default all) [with limit]: ");
        String limitInput = scanner.nextLine().trim().toLowerCase();
        int limit = plan.activeCorridors.size();
        if (!limitInput.equals("all") && !limitInput.isEmpty()) {
            try { limit = Integer.parseInt(limitInput); } catch (NumberFormatException e) { limit = plan.activeCorridors.size(); }
        }
        int show = Math.min(limit, plan.activeCorridors.size());
        System.out.println("\n[DISPATCHED PAYMENT CORRIDORS (Showing " + show + " of " + plan.activeCorridors.size() + ")]: ");
        for (int i = 0; i < show; i++) {
            System.out.println("  " + (i + 1) + ". " + plan.activeCorridors.get(i));
        }
        System.out.println("\nCost Minimization Insight:");
        System.out.println("By routing cheaper sub-paths first (e.g., via Frankfurt & Singapore relays),");
        System.out.println("the bank avoids congested expensive direct corridors, saving substantial treasury capital.");
    }

    private static void fintechProjectSelectionMenu(Scanner scanner) {
        System.out.println("\n--- Module 4: Strategic Fintech Product Portfolio Selection (Min-Cut Reduction) ---");
        System.out.println("Theoretical Principles:");
        System.out.println(" • Project Selection as Min-Cut: Selecting a set of projects with dependencies is a closure problem.");
        System.out.println(" - Construction: Source -> Products with capacity = revenue; Dependencies have capacity = INF;");
        System.out.println("   Infrastructure -> Sink with capacity = cost.");
        System.out.println(" • Dual Optimality: Max Net Profit = (Total Potential Revenue) - Capacity(Min-Cut).");
        System.out.println("   Infinite capacity ensures prerequisite infrastructure dependencies can NEVER be severed.");

        List<FintechProjectSelection.InfrastructureModule> infra = Arrays.asList(
            new FintechProjectSelection.InfrastructureModule("INF-1", "Cloud Core Banking Modernization", 120),
            new FintechProjectSelection.InfrastructureModule("INF-2", "Real-Time AML & Sanctions Streaming Pipeline", 80),
            new FintechProjectSelection.InfrastructureModule("INF-3", "HSM Dedicated Hardware Security Crypto Vault", 160),
            new FintechProjectSelection.InfrastructureModule("INF-4", "ISO 20022 High-Speed Messaging Bus", 60),
            new FintechProjectSelection.InfrastructureModule("INF-5", "Distributed Open Banking API Gateway", 90)
        );

        List<FintechProjectSelection.RevenueProduct> prods = Arrays.asList(
            new FintechProjectSelection.RevenueProduct("PRD-1", "Instant Cross-Border Payments App", 200, "INF-1", "INF-2", "INF-4"),
            new FintechProjectSelection.RevenueProduct("PRD-2", "AI Robo-Advisor Wealth Engine", 150, "INF-1", "INF-5"),
            new FintechProjectSelection.RevenueProduct("PRD-3", "Institutional Crypto Custody", 90, "INF-3"), // Requires 160K infra, earns 90K -> Negative ROI!
            new FintechProjectSelection.RevenueProduct("PRD-4", "High-Frequency FX Algorithmic Hedging", 130, "INF-2", "INF-4"),
            new FintechProjectSelection.RevenueProduct("PRD-5", "SME Embedded Finance Micro-Lending", 170, "INF-1", "INF-5"),
            new FintechProjectSelection.RevenueProduct("PRD-6", "Biometric Mobile Banking Suite", 110, "INF-5")
        );

        FintechProjectSelection.PortfolioDecision decision = FintechProjectSelection.optimizeFintechPortfolio(prods, infra);

        System.out.println("\n[FINTECH PORTFOLIO OPTIMIZATION VIA S-T MIN-CUT]:");
        System.out.printf("Total Potential Gross Revenue:   $%,d Thousand\n", decision.totalPotentialRevenue);
        System.out.printf("Minimum Cut Penalty (Cost/Loss): $%,d Thousand\n", decision.minCutCost);
        System.out.printf("MAXIMUM GUARANTEED NET PROFIT:   $%,d Thousand\n", decision.maxNetProfit);
        System.out.printf("Min-Cut Computation Time:        %.3f ms\n", decision.executionTimeNs / 1_000_000.0);

        System.out.print("\nHow many approved products to view? (e.g., 3, all, default all) [with limit]: ");
        String limitInput = scanner.nextLine().trim().toLowerCase();
        int limit = decision.selectedProducts.size();
        if (!limitInput.equals("all") && !limitInput.isEmpty()) {
            try { limit = Integer.parseInt(limitInput); } catch (NumberFormatException e) { limit = decision.selectedProducts.size(); }
        }
        int show = Math.min(limit, decision.selectedProducts.size());
        System.out.println("\n[APPROVED REVENUE PRODUCTS (Showing " + show + " of " + decision.selectedProducts.size() + ")]: ");
        for (int i = 0; i < show; i++) {
            FintechProjectSelection.RevenueProduct p = decision.selectedProducts.get(i);
            System.out.println("  " + (i + 1) + ". " + p.name + " [ID: " + p.id + "] -> Projected Revenue: +$" + p.expectedRevenue + "K");
        }

        System.out.println("\n[SHARED PREREQUISITE INFRASTRUCTURE FUNDED]:");
        for (FintechProjectSelection.InfrastructureModule m : decision.fundedInfrastructure) {
            System.out.println("  * " + m.name + " [ID: " + m.id + "] -> Capital Cost: -$" + m.capitalCost + "K");
        }

        System.out.println("\n[REJECTED PRODUCTS (Prerequisite Cost Exceeds Expected Gain)]:");
        for (FintechProjectSelection.RevenueProduct p : decision.rejectedProducts) {
            System.out.println("  * " + p.name + " [ID: " + p.id + "] -> Requires $" + p.expectedRevenue + "K vs dependencies. Avoided negative ROI!");
        }
        System.out.println("\nReduction Insight: Shared infrastructure costs are amortized across all selected products.");
        System.out.println("Min-Cut guarantees 100% mathematically optimal capital allocation in polynomial time.");
    }

    private static void fraudRingSurveillanceMenu(Scanner scanner) {
        System.out.println("\n--- Module 5: Fraud Ring Surveillance (Vertex Cover 2-Approximation & FPT) ---");
        System.out.println("1. Run 2-Approximation Surveillance on Suspicious Transfer Network");
        System.out.println("2. Run Parameterized Complexity (FPT) Budget Verification");
        System.out.println("3. View Practical Engineer's NP-Hardness Checklist");
        System.out.print("Select sub-option (1-3): ");
        String sub = scanner.nextLine().trim();

        if (sub.equals("3")) {
            System.out.println(VertexCoverApprox.getEngineerChecklist());
            return;
        }

        // Realistic mule account network with flagged suspicious transfers
        List<VertexCoverApprox.Edge> suspiciousNetwork = new ArrayList<>();
        suspiciousNetwork.add(new VertexCoverApprox.Edge("Acc-101 (Mule)", "Acc-204 (Shell Corp)", "Rapid Layering ($45K)"));
        suspiciousNetwork.add(new VertexCoverApprox.Edge("Acc-204 (Shell Corp)", "Acc-309 (Offshore)", "Wire Transfer ($90K)"));
        suspiciousNetwork.add(new VertexCoverApprox.Edge("Acc-101 (Mule)", "Acc-412 (Smurf)", "Structured Cash Deposit"));
        suspiciousNetwork.add(new VertexCoverApprox.Edge("Acc-412 (Smurf)", "Acc-505 (Crypto Gateway)", "P2P Crypto Purchase"));
        suspiciousNetwork.add(new VertexCoverApprox.Edge("Acc-505 (Crypto Gateway)", "Acc-601 (Aggregator)", "Liquidity Injection"));
        suspiciousNetwork.add(new VertexCoverApprox.Edge("Acc-204 (Shell Corp)", "Acc-601 (Aggregator)", "Fake Invoicing"));
        suspiciousNetwork.add(new VertexCoverApprox.Edge("Acc-309 (Offshore)", "Acc-708 (Tax Haven)", "High Risk Jurisdiction"));

        Set<String> allAccounts = new HashSet<>();
        for (VertexCoverApprox.Edge e : suspiciousNetwork) {
            allAccounts.add(e.u);
            allAccounts.add(e.v);
        }

        if (sub.equals("2")) {
            System.out.print("Enter regulator budget k (max accounts allowed to monitor/freeze, e.g. 3 or 4): ");
            String kInput = scanner.nextLine().trim();
            int k = 4;
            try { k = Integer.parseInt(kInput); } catch (Exception ignored) {}
            
            Set<String> cover = new HashSet<>();
            boolean possible = VertexCoverApprox.hasKVertexCoverFPT(suspiciousNetwork, k, cover);
            System.out.println("\n[FPT Search-Tree Branching Result O(2^k * (V+E))]:");
            if (possible) {
                System.out.println("SUCCESS: A vertex cover of size <= " + k + " EXISTS!");
                System.out.println("Monitored Accounts found: " + cover);
            } else {
                System.out.println("INSUFFICIENT BUDGET: No vertex cover of size <= " + k + " can cover all flagged transfers.");
            }
            return;
        }

        // Option 1: 2-Approximation
        System.out.println("Analyzing " + suspiciousNetwork.size() + " suspicious transfer channels across " + allAccounts.size() + " accounts...");
        VertexCoverApprox.SurveillancePlan plan = VertexCoverApprox.compute2Approximation(allAccounts, suspiciousNetwork);

        System.out.println("\n[MAXIMAL MATCHING FOUND]:");
        for (VertexCoverApprox.Edge e : plan.maximalMatchingEdges) {
            System.out.println("  * Independent Transfer: " + e);
        }
        System.out.println("\n[RECOMMENDED ACCOUNTS FOR SURVEILLANCE (VERTEX COVER)]:");
        for (String acc : plan.monitoredAccounts) {
            System.out.println("  -> Placed on Freeze Watchlist: " + acc);
        }
        System.out.println("\nTheoretical Guarantee: |Cover| <= 2 * |OPT| (At most 2x theoretical minimum accounts).");
        System.out.println("Total Accounts Monitored: " + plan.monitoredAccounts.size() + " accounts covering 100% of suspicious edges.");
    }

    private static void capitalBudgetingMenu(Scanner scanner) {
        System.out.println("\n--- Module 5: Investment Portfolio & Loan Capital Budgeting (Knapsack) ---");
        List<KnapsackCapitalBudgeting.LoanProposal> proposals = new ArrayList<>();
        proposals.add(new KnapsackCapitalBudgeting.LoanProposal("LP-1", "Green Energy Wind Farm", 250, 400));
        proposals.add(new KnapsackCapitalBudgeting.LoanProposal("LP-2", "Fintech Payment Rails", 150, 260));
        proposals.add(new KnapsackCapitalBudgeting.LoanProposal("LP-3", "Commercial Real Estate", 400, 500));
        proposals.add(new KnapsackCapitalBudgeting.LoanProposal("LP-4", "Healthcare Biotech R&D", 200, 310));
        proposals.add(new KnapsackCapitalBudgeting.LoanProposal("LP-5", "SME Supply Chain Logistics", 100, 180));
        proposals.add(new KnapsackCapitalBudgeting.LoanProposal("LP-6", "E-Commerce Fulfillment Hub", 300, 420));
        proposals.add(new KnapsackCapitalBudgeting.LoanProposal("LP-7", "AI Cloud Infrastructure", 350, 480));
        proposals.add(new KnapsackCapitalBudgeting.LoanProposal("LP-8", "Microfinance Aggregator", 80, 130));

        System.out.println("Available Loan Proposals (Capital vs Return in $K):");
        for (KnapsackCapitalBudgeting.LoanProposal p : proposals) {
            System.out.println("  " + p);
        }

        System.out.print("\nEnter Total Bank Lending Budget W in $K (default 750): ");
        String bInput = scanner.nextLine().trim();
        int budget = 750;
        try { if (!bInput.isEmpty()) budget = Integer.parseInt(bInput); } catch (Exception ignored) {}

        System.out.print("Enter FPTAS tolerance epsilon ε in range (0.01 to 0.50, default 0.10 for 90% OPT): ");
        String epsInput = scanner.nextLine().trim();
        double eps = 0.10;
        try { if (!epsInput.isEmpty()) eps = Double.parseDouble(epsInput); } catch (Exception ignored) {}

        KnapsackCapitalBudgeting.AllocationResult exact = KnapsackCapitalBudgeting.solveExactDP(proposals, budget);
        KnapsackCapitalBudgeting.AllocationResult greedy = KnapsackCapitalBudgeting.solveGreedy2Approx(proposals, budget);
        KnapsackCapitalBudgeting.AllocationResult fptas = KnapsackCapitalBudgeting.solveFPTAS(proposals, budget, eps);

        System.out.println("\n==========================================================================");
        System.out.printf("%-26s | %-12s | %-13s | %-10s | %s\n", "Method", "Capital Used", "Return ($K)", "Time", "Approximation Guarantee");
        System.out.println("--------------------------------------------------------------------------");
        System.out.printf("%-26s | $%-11d | $%-12d | %-7d ns | %s\n", exact.methodName, exact.totalCapitalUsed, exact.totalExpectedReturn, exact.executionTimeNs, exact.theoreticalGuarantee);
        System.out.printf("%-26s | $%-11d | $%-12d | %-7d ns | %s\n", greedy.methodName, greedy.totalCapitalUsed, greedy.totalExpectedReturn, greedy.executionTimeNs, greedy.theoreticalGuarantee);
        System.out.printf("%-26s | $%-11d | $%-12d | %-7d ns | %s\n", fptas.methodName, fptas.totalCapitalUsed, fptas.totalExpectedReturn, fptas.executionTimeNs, fptas.theoreticalGuarantee);
        System.out.println("==========================================================================");
        System.out.println("Theoretical Insight: Knapsack admits FPTAS (Fully Polynomial Time Approximation Scheme) in O(n^2 / ε),");
        System.out.println("meaning we can achieve (1 - ε) accuracy in polynomial time for any desired ε > 0.");
    }

    private static void reconciliationSubsetSumMenu(Scanner scanner) {
        System.out.println("\n--- Module 5: Interbank Clearing Discrepancy Reconciliation (Subset-Sum) ---");
        List<ReconciliationSubsetSum.AuditTransaction> pending = new ArrayList<>();
        pending.add(new ReconciliationSubsetSum.AuditTransaction("TX-101", "Foreign Exchange Wire", 450));
        pending.add(new ReconciliationSubsetSum.AuditTransaction("TX-102", "Interbank Clearing", 1200));
        pending.add(new ReconciliationSubsetSum.AuditTransaction("TX-103", "Merchant Settlement", 310));
        pending.add(new ReconciliationSubsetSum.AuditTransaction("TX-104", "Cheque Batch #49", 890));
        pending.add(new ReconciliationSubsetSum.AuditTransaction("TX-105", "ATM Network Dispense", 2450));
        pending.add(new ReconciliationSubsetSum.AuditTransaction("TX-106", "Treasury Bond Yield", 750));
        pending.add(new ReconciliationSubsetSum.AuditTransaction("TX-107", "Corporate Payroll Debit", 1600));
        pending.add(new ReconciliationSubsetSum.AuditTransaction("TX-108", "SWIFT MT103 Transfer", 500));

        System.out.println("Unsettled Pending Transactions:");
        for (ReconciliationSubsetSum.AuditTransaction tx : pending) {
            System.out.println("  " + tx);
        }

        System.out.print("\nEnter Target Clearing Discrepancy Amount $T (default 2840): ");
        String tInput = scanner.nextLine().trim();
        int target = 2840;
        try { if (!tInput.isEmpty()) target = Integer.parseInt(tInput); } catch (Exception ignored) {}

        // Solve reconciliation
        ReconciliationSubsetSum.ReconciliationResult res = ReconciliationSubsetSum.reconcileBatch(pending, target);
        System.out.println("\n[RECONCILIATION RESULT]:");
        System.out.println("Exact Match Found (Decision = YES): " + res.exactMatchFound);
        System.out.println("Total Amount Reconciled: $" + res.totalReconciledAmount);
        System.out.println("Remaining Discrepancy:   $" + res.discrepancyRemaining);
        System.out.println("Reconciled Transactions:");
        for (ReconciliationSubsetSum.AuditTransaction tx : res.reconciledSubset) {
            System.out.println("  -> " + tx);
        }

        // Canonical NP Witness Verification
        System.out.println("\n--- Canonical NP Witness Verification Demo ---");
        System.out.println("In complexity theory, SUBSET-SUM ∈ NP because verifying a proposed solution (witness)");
        System.out.println("takes deterministic polynomial time O(|witness|), even though finding it is NP-Complete.");
        ReconciliationSubsetSum.VerificationResult vRes = ReconciliationSubsetSum.verifyWitness(res.reconciledSubset, target);
        System.out.println(vRes.proofExplanation);
    }

    private static void metricTSPMenu(Scanner scanner) {
        System.out.println("\n--- Module 5: Metropolitan ATM Cash Logistics (Metric-TSP 2-Approximation) ---");
        System.out.println("Context: While Option 12 uses Bitmask DP (limited to N <= 16, O(2^N * N^2)),");
        System.out.println("real city ATM cash delivery requires scaling to dozens of branches.");
        System.out.println("Metric-TSP 2-Approximation runs in O(V^2) with a guaranteed 2.0x bound!");

        List<MetricTSPApprox.ATMNode> atms = new ArrayList<>();
        atms.add(new MetricTSPApprox.ATMNode(0, "Central Vault (Depot)", 0.0, 0.0));
        atms.add(new MetricTSPApprox.ATMNode(1, "North Financial Branch", 5.0, 12.0));
        atms.add(new MetricTSPApprox.ATMNode(2, "International Airport Terminal", 18.0, 8.0));
        atms.add(new MetricTSPApprox.ATMNode(3, "Downtown Commercial Kiosk", 8.0, 3.0));
        atms.add(new MetricTSPApprox.ATMNode(4, "Tech Park ATM Hub", 14.0, 15.0));
        atms.add(new MetricTSPApprox.ATMNode(5, "Suburban Shopping Mall", 3.0, 16.0));
        atms.add(new MetricTSPApprox.ATMNode(6, "Harbor District Branch", 10.0, -4.0));
        atms.add(new MetricTSPApprox.ATMNode(7, "South Metro Station", 4.0, -9.0));

        MetricTSPApprox.TSPTourResult res = MetricTSPApprox.solveMetricTSP(atms);
        System.out.println("\n[COMPUTED METRIC-TSP 2-APPROXIMATION TOUR]:");
        for (int i = 0; i < res.tour.size(); i++) {
            System.out.println("  Stop " + (i + 1) + ": " + res.tour.get(i));
        }
        System.out.printf("\nTotal Tour Route Distance: %.2f km\n", res.totalDistanceKm);
        System.out.printf("Minimum Spanning Tree (MST): %.2f km (Theoretical Lower Bound for OPT)\n", res.mstWeightKm);
        System.out.printf("Theoretical Upper Bound:    %.2f km (2 * MST <= 2 * OPT)\n", res.theoreticalUpperBoundKm);
        System.out.printf("Approximation Guarantee:    Tour Distance (%.2f km) <= 2 * OPT (PASS!)\n", res.totalDistanceKm);
    }

    private static void millerRabinMenu(Scanner scanner) {
        System.out.println("\n--- Module 6: RSA & SWIFT Cryptographic Key Primality (Miller-Rabin) ---");
        System.out.println("1. Test a Custom Number or Standard Cryptographic Candidate");
        System.out.println("2. Run the Carmichael Number Challenge (n = 561)");
        System.out.print("Select sub-option (1 or 2): ");
        String sub = scanner.nextLine().trim();

        if (sub.equals("2")) {
            System.out.println(MillerRabinPrimality.demonstrateCarmichael());
            return;
        }

        System.out.print("Enter candidate number to test (press Enter for a 64-bit prime candidate): ");
        String input = scanner.nextLine().trim();
        BigInteger n;
        if (input.isEmpty()) {
            n = new BigInteger("18446744073709551557"); // 64-bit prime
            System.out.println("Testing default 64-bit candidate: " + n);
        } else {
            try {
                n = new BigInteger(input);
            } catch (Exception e) {
                System.out.println("Invalid number format. Using default 64-bit candidate.");
                n = new BigInteger("18446744073709551557");
            }
        }

        System.out.print("Enter number of independent rounds k (default 40): ");
        String kInput = scanner.nextLine().trim();
        int k = 40;
        try { if (!kInput.isEmpty()) k = Integer.parseInt(kInput); } catch (Exception ignored) {}

        MillerRabinPrimality.PrimalityReport report = MillerRabinPrimality.testPrimality(n, k);
        System.out.println("\n[MILLER-RABIN PRIMALITY REPORT (Monte Carlo)]: ");
        System.out.println("Candidate Tested:       " + report.candidate);
        System.out.println("Classification:         " + (report.isProbablyPrime ? "PROBABLE PRIME" : "COMPOSITE"));
        System.out.println("Rounds Tested:          " + report.roundsTested);
        System.out.printf("Execution Time:         %.3f ms\n", report.executionTimeNs / 1_000_000.0);
        if (report.isProbablyPrime) {
            System.out.printf("Max Error Probability:  (1/4)^%d = %e\n", report.roundsTested, report.maxErrorProbability);
            System.out.println("Industry Context: In banking standards (FIPS 186-4, SWIFT RSA), k=40 error rate is < 10^-24,");
            System.out.println("which is far lower than cosmic-ray memory corruption rates. This is why Monte Carlo primality is enough!");
        } else {
            System.out.println("Guaranteed Witness Base: a = " + report.compositeWitness);
        }
    }

    private static void reservoirSamplingMenu(Scanner scanner) {
        System.out.println("\n--- Module 6: Streaming Transaction Compliance Audit (Reservoir Sampling) ---");
        System.out.println("Total transactions in live dataset: " + transactions.size());
        System.out.print("Enter audit sample size k to inspect (e.g. 10, 50, 100): ");
        String kInput = scanner.nextLine().trim();
        int k = 10;
        try { if (!kInput.isEmpty()) k = Integer.parseInt(kInput); } catch (Exception ignored) {}

        System.out.println("Streaming all " + transactions.size() + " transactions through Algorithm R in O(k) memory...");
        ReservoirSampling.SampleResult<Transaction> sampleRes = ReservoirSampling.sampleStream(transactions, k);

        System.out.print("How many sampled transactions to view? (e.g., 10, 50, all): ");
        String limitInput = scanner.nextLine().trim().toLowerCase();
        int displayLimit = sampleRes.sample.size();
        if (!limitInput.equals("all") && !limitInput.isEmpty()) {
            try {
                displayLimit = Integer.parseInt(limitInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, showing default sampled transactions.");
                displayLimit = 10;
            }
        }
        int showCount = Math.min(displayLimit, sampleRes.sample.size());
        System.out.println("\n[UNBIASED UNIFORM SAMPLE OF " + sampleRes.sampleSizeK + " AUDIT TRANSACTIONS (Showing " + showCount + ")]: ");
        for (int i = 0; i < showCount; i++) {
            System.out.println("  Sample #" + (i + 1) + " -> " + sampleRes.sample.get(i));
        }

        System.out.println("\n[MATHEMATICAL UNIFORMITY GUARANTEE]:");
        System.out.printf("Stream Size N:           %,d transactions\n", sampleRes.totalStreamSizeN);
        System.out.printf("Sample Size k:           %d\n", sampleRes.sampleSizeK);
        System.out.printf("Probability per item:    k/N = %.8f%% (Exactly equal for every transaction in history!)\n", sampleRes.uniformProbabilityPerItem * 100);
        System.out.printf("Execution Time:          %.3f ms in single streaming pass with O(k) memory.\n", sampleRes.executionTimeNs / 1_000_000.0);
    }

    private static void universalHashingMenu(Scanner scanner) {
        System.out.println("\n--- Module 6: Anti-Collision Transaction Deduplication (Universal Hashing) ---");
        System.out.println("Simulating 10,000 incoming transaction IDs with clustered timestamp patterns...");
        List<Integer> clusteredKeys = new ArrayList<>();
        // Generate clustered keys sharing common multiples (worst-case for modulo hashing)
        for (int i = 1; i <= 10000; i++) {
            clusteredKeys.add(i * 32); 
        }

        int bucketCount = 256;
        Map<String, UniversalHashing.CollisionMetrics> results = UniversalHashing.compareWithNaive(clusteredKeys, bucketCount);

        System.out.println("\n==========================================================================================");
        System.out.printf("%-42s | %-16s | %-10s | %s\n", "Hashing Scheme", "Total Collisions", "Max Chain", "Actual vs Expected Pairwise Rate");
        System.out.println("------------------------------------------------------------------------------------------");
        for (Map.Entry<String, UniversalHashing.CollisionMetrics> entry : results.entrySet()) {
            UniversalHashing.CollisionMetrics m = entry.getValue();
            System.out.printf("%-42s | %-16d | %-10d | %.6f (Exp: %.6f)\n", 
                entry.getKey(), m.collisionsCount, m.maxChainLength, m.actualCollisionRate, m.expectedCollisionsPerPair);
        }
        System.out.println("==========================================================================================");
        System.out.println("Las Vegas Insight: Universal Hashing randomly draws h_{a,b}(x) = ((ax+b) mod p) mod m at startup.");
        System.out.println("It ALWAYS yields the exact transaction (100% correct), but guarantees expected O(1) time");
        System.out.println("preventing Hash-DoS attacks even when inputs share common divisors!");
    }

    private static void randomizedQuickSortMenu(Scanner scanner) {
        System.out.println("\n--- Module 6: High-Volume Transaction Log Sorting (Randomized QuickSort) ---");
        System.out.print("Enter number of transactions to sort (e.g. 50000): ");
        String nInput = scanner.nextLine().trim();
        int n = 50000;
        try { if (!nInput.isEmpty()) n = Integer.parseInt(nInput); } catch (Exception ignored) {}

        // Test 1: Adversarial already-sorted array
        double[] sortedArray = new double[n];
        for (int i = 0; i < n; i++) sortedArray[i] = i * 1.5;

        // Test 2: Random transaction amounts
        double[] randomArray = new double[n];
        Random rand = new Random();
        for (int i = 0; i < n; i++) randomArray[i] = rand.nextDouble() * 10000.0;

        System.out.println("\nRunning Randomized QuickSort with Las Vegas random pivot selection...");
        RandomizedQuickSort.SortResult rSorted = RandomizedQuickSort.sort(sortedArray);
        RandomizedQuickSort.SortResult rRandom = RandomizedQuickSort.sort(randomArray);

        System.out.println("\n[BENCHMARK RESULTS]:");
        System.out.printf("1. Adversarial Already-Sorted Input (%,d elements):\n", n);
        System.out.printf("   Comparisons Made: %,d (Avoided O(N^2) disaster of %,d comparisons!)\n", rSorted.comparisonCount, ((long) n * n) / 2);
        System.out.printf("   Execution Time:   %.3f ms\n", rSorted.executionTimeNs / 1_000_000.0);

        System.out.printf("2. Random Transaction Amounts (%,d elements):\n", n);
        System.out.printf("   Comparisons Made: %,d\n", rRandom.comparisonCount);
        System.out.printf("   Execution Time:   %.3f ms\n", rRandom.executionTimeNs / 1_000_000.0);

        System.out.println("\nTheoretical Guarantee: E[Comparisons] <= 2 * n * ln(n) = " + 
            String.format("%,.0f", 2.0 * n * Math.log(n)) + ". Las Vegas property ensures 100% sort accuracy.");

        System.out.print("\nHow many sorted transaction amounts to view? (e.g., 5, 20, all, none, default 5): ");
        String viewInput = scanner.nextLine().trim().toLowerCase();
        int viewLimit = 5;
        if (!viewInput.equals("none")) {
            if (viewInput.equals("all")) {
                viewLimit = n;
            } else if (!viewInput.isEmpty()) {
                try {
                    viewLimit = Integer.parseInt(viewInput);
                } catch (NumberFormatException e) {
                    viewLimit = 5;
                }
            }
            int show = Math.min(viewLimit, n);
            System.out.println("First " + show + " sorted transaction amounts:");
            for (int i = 0; i < show; i++) {
                System.out.printf("  Item #%d: $%,.2f\n", (i + 1), randomArray[i]);
            }
        }
    }

    private static void parallelLedgerAnalyticsMenu(Scanner scanner) {
        System.out.println("\n--- Module 6: Parallel Multi-Core Ledger Analytics & Running Balance Scan ---");
        System.out.print("Enter number of transactions to analyze (e.g. 100000 or all) [Analysis Scope Limit]: ");
        String limitInput = scanner.nextLine().trim().toLowerCase();
        int limit = 100000;
        if (limitInput.equals("all")) {
            limit = transactions.size();
        } else {
            try { if (!limitInput.isEmpty()) limit = Integer.parseInt(limitInput); } catch (Exception ignored) {}
        }
        limit = Math.min(limit, transactions.size());

        System.out.println("Extracting transaction amounts for " + limit + " transactions...");
        double[] amounts = new double[limit];
        for (int i = 0; i < limit; i++) {
            String desc = transactions.get(i).getDescription();
            String[] parts = desc.split(" ");
            double amt = 100.0; // fallback
            if (parts.length >= 3) {
                try { amt = Double.parseDouble(parts[2]); } catch (Exception ignored) {}
            }
            amounts[i] = amt;
        }

        System.out.println("\n--- Part A: Parallel Reduce (ForkJoin) vs Sequential (Brent's Theorem) ---");
        System.out.println(ParallelLedgerAnalytics.analyzeWorkSpanAndBrentsTheorem(amounts));

        System.out.println("\n--- Part B: Blelloch Parallel Prefix-Sum (Scan) for Running Account Balances ---");
        System.out.println("Running Blelloch Parallel Scan (Up-Sweep & Down-Sweep) in O(log N) Span...");
        long scanStart = System.nanoTime();
        double[] runningBalances = ParallelLedgerAnalytics.blellochParallelScan(amounts);
        long scanTime = System.nanoTime() - scanStart;

        System.out.printf("Computed running balances for %,d transactions in %.3f ms (Work O(N), Span O(log N))\n", 
            limit, scanTime / 1_000_000.0);

        System.out.print("How many ledger running balances to view? (e.g., 5, 20, all, default 5): ");
        String viewInput = scanner.nextLine().trim().toLowerCase();
        int viewLimit = 5;
        if (viewInput.equals("all")) {
            viewLimit = limit;
        } else if (!viewInput.isEmpty()) {
            try {
                viewLimit = Integer.parseInt(viewInput);
            } catch (NumberFormatException e) {
                viewLimit = 5;
            }
        }
        int show = Math.min(viewLimit, limit);
        System.out.println("Showing " + show + " Ledger Running Balances:");
        for (int i = 0; i < show; i++) {
            System.out.printf("  Tx #%d: Amount = $%,.2f | Cumulative Ledger Balance = $%,.2f\n", 
                (i + 1), amounts[i], runningBalances[i]);
        }
    }
}
