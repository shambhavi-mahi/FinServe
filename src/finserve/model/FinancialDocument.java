package finserve.model;

public class FinancialDocument {
    private String docId;
    private String content;

    public FinancialDocument(String docId, String content) {
        this.docId = docId;
        this.content = content;
    }

    public String getDocId() { return docId; }
    public String getContent() { return content; }
}
