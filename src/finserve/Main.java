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
import finserve.model.Account;
import finserve.model.Transaction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
            System.out.println("\n============================================================");
            System.out.println("FINSERVE BANKING ANALYTICS - MAIN MENU");
            System.out.println("============================================================");
            System.out.println("--- General Data ---");
            System.out.println("1. View Account Details & Transactions (O(1) Search)");
            System.out.println("--- Module 1: String Algorithms ---");
            System.out.println("2. Customer Name Search (Naive) [with limit]");
            System.out.println("3. Transaction Search (KMP) [with limit]");
            System.out.println("4. Fraud Pattern Detection (Aho-Corasick) [with limit]");
            System.out.println("5. Repeated Transaction Pattern (Z-Function) [with limit]");
            System.out.println("6. Search Financial Codes (Rabin-Karp) [with limit]");
            System.out.println("--- Module 2: Suffix Structures ---");
            System.out.println("7. Financial Document Search & Indexing (Suffix Array / SA-IS) [with limit]");
            System.out.println("8. Transaction Pattern Analysis (LCP) [Analysis Scope Limit]");
            System.out.println("--- Module 3: Advanced Dynamic Programming ---");
            System.out.println("9. KYC & AML Sanctions Screening (Levenshtein)");
            System.out.println("10. IBAN & Account Number Transposition Detection (Damerau-Levenshtein)");
            System.out.println("11. Merchant Entity Resolution (Weighted Edit Distance)");
            System.out.println("12. ATM Cash Replenishment Routing (Bitmask DP)");
            System.out.println("13. Optimal Multi-Currency Conversion Cost (Matrix Chain)");
            System.out.println("14. Exit");
            System.out.println("============================================================");
            System.out.print("Select an option: ");

            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            
            int choice = -1;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    accountManagementMenu(scanner);
                    break;
                case 2:
                    naiveNameSearchMenu(scanner);
                    break;
                case 3:
                    transactionSearchMenu(scanner);
                    break;
                case 4:
                    fraudPatternDetectionMenu(scanner);
                    break;
                case 5:
                    zFunctionMenu(scanner);
                    break;
                case 6:
                    rabinKarpMenu(scanner);
                    break;
                case 7:
                    suffixArrayMenu(scanner);
                    break;
                case 8:
                    lcpMenu(scanner);
                    break;
                case 9:
                    levenshteinMenu(scanner);
                    break;
                case 10:
                    damerauLevenshteinMenu(scanner);
                    break;
                case 11:
                    weightedEditDistanceMenu(scanner);
                    break;
                case 12:
                    bitmaskDPMenu();
                    break;
                case 13:
                    matrixChainMenu();
                    break;
                case 14:
                    System.out.println("Exiting FinServe Analytics. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
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
}
