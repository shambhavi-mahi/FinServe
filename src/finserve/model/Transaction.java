package finserve.model;

public class Transaction {
    private String id;
    private String description;

    public Transaction(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return id + " : " + description;
    }
}
