package finserve;

import finserve.m1_string.AhoCorasick;
import finserve.m1_string.KMP;
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
            System.out.println("\n========================================");
            System.out.println("FINSERVE BANKING ANALYTICS");
            System.out.println("========================================");
            System.out.println("1. Customer Management");
            System.out.println("2. Account Management");
            System.out.println("3. Transaction Search (KMP)");
            System.out.println("4. Fraud Pattern Detection (Aho-Corasick)");
            System.out.println("5. Financial Document Search (M2 - Teammate)");
            System.out.println("6. Transaction Description Analysis (M2 - Teammate)");
            System.out.println("7. Error Correction (M3 - Teammate)");
            System.out.println("8. Financial Operation Optimization (M3 - Teammate)");
            System.out.println("9. Transaction Analytics (M3 - Teammate)");
            System.out.println("10. Exit");
            System.out.print("Select an option: ");

            if (!scanner.hasNextLine()) {
                break;
            }
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }
            
            int choice = -1;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and 10.");
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.println("Customer Management module is a placeholder.");
                    break;
                case 2:
                    System.out.println("Account Management module is a placeholder.");
                    break;
                case 3:
                    transactionSearchMenu(scanner);
                    break;
                case 4:
                    fraudPatternDetectionMenu();
                    break;
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                    System.out.println("Feature " + choice + " is under development by teammates (M2/M3 modules).");
                    break;
                case 10:
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
        boolean foundAny = false;
        for (Transaction tx : transactions) {
            List<Integer> matches = KMP.search(tx.getDescription(), pattern);
            if (!matches.isEmpty()) {
                System.out.println(tx);
                foundAny = true;
            }
        }
        if (!foundAny) {
            System.out.println("No matching transactions found.");
        }
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
        if (!foundFraud) {
            System.out.println("No fraud patterns detected.");
        }
    }
}
