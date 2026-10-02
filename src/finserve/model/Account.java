package finserve.model;

public class Account {
    private String accountId;
    private String customerId;
    private double balance;

    public Account(String accountId, String customerId, double balance) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.balance = balance;
    }

    public String getAccountId() { return accountId; }
    public String getCustomerId() { return customerId; }
    public double getBalance() { return balance; }
}
