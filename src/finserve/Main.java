package finserve;

import finserve.m1_string.AhoCorasick;
import finserve.m1_string.KMP;
import finserve.m1_string.RabinKarp;
import finserve.m1_string.ZFunction;
import finserve.m3_dp.BitmaskDP;
import finserve.m3_dp.DamerauLevenshtein;
import finserve.m3_dp.Levenshtein;
import finserve.m3_dp.MatrixChain;
import finserve.m3_dp.WeightedEditDistance;
import finserve.model.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static List<Transaction> transactions = new ArrayList<>();

    public static void main(String[] args) {
        // Initialize dummy data based on user requirements
        transactions.add(new Transaction("TX1001", "ONLINE PAYMENT AMAZON"));
        transactions.add(new Transaction("TX1002", "ONLINE PAYMENT FLIPKART"));
        transactions.add(new Transaction("TX1003", "ATM CASH WITHDRAWAL"));
        transactions.add(new Transaction("TX1004", "ONLINE PAYMENT AMAZON"));
        transactions.add(new Transaction("TX1005", "INTERNATIONAL PAYMENT"));
        transactions.add(new Transaction("TX2034", "PHISHING DETECTED IN LOGIN"));
        transactions.add(new Transaction("TX2041", "UNAUTHORIZED TRANSFER TO UNKNOWN ACCOUNT"));
        transactions.add(new Transaction("TX2050", "OTP SENT TO MOBILE"));
        transactions.add(new Transaction("TX2099", "SUSPICIOUS LOGIN ATTEMPT"));

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n============================================================");
            System.out.println("FINSERVE BANKING ANALYTICS - MAIN MENU");
            System.out.println("============================================================");
            System.out.println("--- General Data ---");
            System.out.println("1. Customer & Account Management");
            System.out.println("--- Module 1: String Algorithms ---");
            System.out.println("2. Transaction Search (KMP)");
            System.out.println("3. Fraud Pattern Detection (Aho-Corasick)");
            System.out.println("4. Repeated Transaction Pattern (Z-Function)");
            System.out.println("5. Search Financial Codes (Rabin-Karp)");
            System.out.println("--- Module 2: Suffix Structures ---");
            System.out.println("6. Financial Document Search & Indexing (Suffix Array / SA-IS)");
            System.out.println("7. Transaction Pattern Analysis (LCP / Suffix Tree)");
            System.out.println("--- Module 3: Advanced Dynamic Programming ---");
            System.out.println("8. Transaction Description Correction (Levenshtein)");
            System.out.println("9. Fast Typo Error Handling (Damerau-Levenshtein)");
            System.out.println("10. Severity-Based Error Detection (Weighted Edit Distance)");
            System.out.println("11. Financial Operation Route Optimization (Bitmask DP)");
            System.out.println("12. Batch Processing Sequence Optimization (Matrix Chain)");
            System.out.println("13. Exit");
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
                    System.out.println("Customer & Account Management module is a placeholder.");
                    break;
                case 2:
                    transactionSearchMenu(scanner);
                    break;
                case 3:
                    fraudPatternDetectionMenu();
                    break;
                case 4:
                    zFunctionMenu(scanner);
                    break;
                case 5:
                    rabinKarpMenu(scanner);
                    break;
                case 6:
                case 7:
                    System.out.println("Module 2 (Suffix Structures) called! Make sure the classes are pulled from Git.");
                    break;
                case 8:
                    levenshteinMenu(scanner);
                    break;
                case 9:
                    damerauLevenshteinMenu(scanner);
                    break;
                case 10:
                    weightedEditDistanceMenu(scanner);
                    break;
                case 11:
                    bitmaskDPMenu();
                    break;
                case 12:
                    matrixChainMenu();
                    break;
                case 13:
                    System.out.println("Exiting FinServe Analytics. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void transactionSearchMenu(Scanner scanner) {
        System.out.print("Enter pattern to search (e.g., 'ONLINE PAYMENT'): ");
        String pattern = scanner.nextLine();
        System.out.println("Searching for: '" + pattern + "'...");
        boolean found = false;
        for (Transaction tx : transactions) {
            if (!KMP.search(tx.getDescription(), pattern).isEmpty()) {
                System.out.println(tx);
                found = true;
            }
        }
        if (!found) System.out.println("No matches found.");
    }

    private static void fraudPatternDetectionMenu() {
        System.out.println("Running fraud pattern detection on all transactions...");
        String[] fraudPatterns = {"OTP", "PHISHING", "SUSPICIOUS", "UNAUTHORIZED"};
        AhoCorasick ac = new AhoCorasick();
        ac.build(fraudPatterns);
        boolean foundFraud = false;
        for (Transaction tx : transactions) {
            List<String> matches = ac.search(tx.getDescription());
            if (!matches.isEmpty()) {
                System.out.println(tx.getId() + " -> " + String.join(", ", matches) + " detected");
                foundFraud = true;
            }
        }
        if (!foundFraud) System.out.println("No fraud patterns detected.");
    }

    private static void zFunctionMenu(Scanner scanner) {
        System.out.print("Enter transaction pattern to find repetition (e.g., 'AMAZON'): ");
        String pattern = scanner.nextLine();
        for (Transaction tx : transactions) {
            List<Integer> matches = ZFunction.search(tx.getDescription(), pattern);
            if (!matches.isEmpty()) {
                System.out.println(tx.getId() + " -> Match at index: " + matches);
            }
        }
    }

    private static void rabinKarpMenu(Scanner scanner) {
        System.out.print("Enter financial code/keyword to search: ");
        String pattern = scanner.nextLine();
        for (Transaction tx : transactions) {
            List<Integer> matches = RabinKarp.search(tx.getDescription(), pattern);
            if (!matches.isEmpty()) {
                System.out.println(tx.getId() + " -> Code found at: " + matches);
            }
        }
    }

    private static void levenshteinMenu(Scanner scanner) {
        System.out.print("Enter a mistyped transaction description (e.g., AMAZN PAYMENT): ");
        String input = scanner.nextLine();
        System.out.print("Enter expected description (e.g., AMAZON PAYMENT): ");
        String expected = scanner.nextLine();
        int dist = Levenshtein.calculate(input, expected);
        System.out.println("Levenshtein Distance: " + dist);
        System.out.println(dist < 3 ? "Recommendation: AUTOCORRECT" : "Recommendation: MANUAL REVIEW");
    }

    private static void damerauLevenshteinMenu(Scanner scanner) {
        System.out.print("Enter a description with a swapped character (e.g., AMZAON PAYMENT): ");
        String input = scanner.nextLine();
        System.out.print("Enter expected description (e.g., AMAZON PAYMENT): ");
        String expected = scanner.nextLine();
        int dist = DamerauLevenshtein.calculate(input, expected);
        System.out.println("Damerau-Levenshtein Distance (handles swaps): " + dist);
    }

    private static void weightedEditDistanceMenu(Scanner scanner) {
        System.out.print("Enter suspicious account/amount code (e.g., 1005): ");
        String input = scanner.nextLine();
        System.out.print("Enter expected code (e.g., 100): ");
        String expected = scanner.nextLine();
        int dist = WeightedEditDistance.calculateWeighted(input, expected);
        System.out.println("Weighted Edit Distance (Penalty Score): " + dist);
    }

    private static void bitmaskDPMenu() {
        System.out.println("Calculating optimal route for 4 ATM cash refills...");
        int[][] operationsCost = {
            {0, 10, 15, 20},
            {10, 0, 35, 25},
            {15, 35, 0, 30},
            {20, 25, 30, 0}
        };
        int cost = BitmaskDP.optimize(operationsCost);
        System.out.println("Minimum operational cost: " + cost);
    }

    private static void matrixChainMenu() {
        System.out.println("Optimizing batch processing of end-of-day financial datasets...");
        int[] dimensions = {10, 30, 5, 60}; 
        int cost = MatrixChain.matrixChainOrder(dimensions);
        System.out.println("Minimum scalar multiplications required: " + cost);
    }
}
