package finserve.m2_suffix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Suffix Tree Implementation for FinServe Banking Analytics.
 * 
 * Banking Use Case:
 * Enables fast sub-linear substring queries, keyword searches, and pattern counting on 
 * financial documents (e.g. searching for terms like "CASH", "WITHDRAWAL", "AMAZON").
 * 
 * Time Complexity:
 * - Construction: O(N^2) for explicit suffix insertion (clean and beginner-friendly).
 * - Pattern Search: O(M + K) where M = pattern length and K = number of occurrences.
 * Space Complexity:
 * - O(N) space for nodes and edge representations.
 */
public class SuffixTree {

    public static class Node {
        public int start;
        public int end; // -1 represents terminal (text.length())
        public Map<Character, Node> children = new HashMap<>();
        public List<Integer> suffixIndices = new ArrayList<>();

        public Node(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int edgeLength(int textLength) {
            int actualEnd = (end == -1) ? textLength : end;
            return actualEnd - start;
        }
    }

    private final String rawText;
    private final String text;
    private final Node root;

    public SuffixTree(String rawText) {
        this.rawText = (rawText == null) ? "" : rawText;
        this.text = this.rawText.endsWith("$") ? this.rawText : this.rawText + "$";
        this.root = new Node(-1, -1);
        buildTree();
    }

    private void buildTree() {
        for (int i = 0; i < text.length(); i++) {
            insertSuffix(i);
        }
    }

    private void insertSuffix(int suffixIdx) {
        Node curr = root;
        int i = suffixIdx;
        int n = text.length();

        while (i < n) {
            char c = text.charAt(i);
            if (!curr.children.containsKey(c)) {
                Node leaf = new Node(i, -1);
                leaf.suffixIndices.add(suffixIdx);
                curr.children.put(c, leaf);
                return;
            }

            Node child = curr.children.get(c);
            int edgeLen = child.edgeLength(n);
            int childStart = child.start;
            int matchLen = 0;

            while (matchLen < edgeLen && i + matchLen < n && text.charAt(childStart + matchLen) == text.charAt(i + matchLen)) {
                matchLen++;
            }

            if (matchLen == edgeLen) {
                i += matchLen;
                curr = child;
            } else {
                Node splitNode = new Node(childStart, childStart + matchLen);
                child.start = childStart + matchLen;
                splitNode.children.put(text.charAt(child.start), child);

                Node newLeaf = new Node(i + matchLen, -1);
                newLeaf.suffixIndices.add(suffixIdx);
                splitNode.children.put(text.charAt(i + matchLen), newLeaf);

                curr.children.put(c, splitNode);
                return;
            }
        }
        curr.suffixIndices.add(suffixIdx);
    }

    /**
     * Searches for pattern in the suffix tree.
     * 
     * @param pattern Query pattern string.
     * @return List of starting indices where pattern occurs in the text.
     */
    public List<Integer> search(String pattern) {
        List<Integer> result = new ArrayList<>();
        if (pattern == null || pattern.isEmpty() || rawText.isEmpty()) return result;

        Node curr = root;
        int i = 0;
        int m = pattern.length();
        int n = text.length();

        while (i < m) {
            char c = pattern.charAt(i);
            if (!curr.children.containsKey(c)) {
                return result; // Pattern not found
            }

            Node child = curr.children.get(c);
            int edgeLen = child.edgeLength(n);
            int childStart = child.start;
            int matchLen = 0;

            while (matchLen < edgeLen && i + matchLen < m && text.charAt(childStart + matchLen) == pattern.charAt(i + matchLen)) {
                matchLen++;
            }

            if (i + matchLen == m) {
                collectLeafIndices(child, result);
                return result;
            }

            if (matchLen < edgeLen) {
                return result;
            }

            i += edgeLen;
            curr = child;
        }

        collectLeafIndices(curr, result);
        return result;
    }

    private void collectLeafIndices(Node node, List<Integer> result) {
        if (node == null) return;
        for (int idx : node.suffixIndices) {
            if (idx < rawText.length() && !result.contains(idx)) {
                result.add(idx);
            }
        }
        for (Node child : node.children.values()) {
            collectLeafIndices(child, result);
        }
    }

    public boolean contains(String pattern) {
        return !search(pattern).isEmpty();
    }

    public int countOccurrences(String pattern) {
        return search(pattern).size();
    }

    public static SuffixTree buildSuffixTree(String text) {
        return new SuffixTree(text);
    }

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("FinServe M2 - Suffix Tree Querying");
        System.out.println("=========================================");

        String financialDoc = "TX1001:ONLINE PAYMENT AMAZON TX1002:ONLINE PAYMENT FLIPKART TX1003:ATM CASH WITHDRAWAL TX1004:ONLINE PAYMENT AMAZON TX1005:INTERNATIONAL PAYMENT";
        System.out.println("Input Financial Document:");
        System.out.println(financialDoc);
        System.out.println();

        System.out.println("Building Suffix Tree...");
        SuffixTree tree = SuffixTree.buildSuffixTree(financialDoc);

        String[] searchQueries = {"ONLINE PAYMENT", "AMAZON", "WITHDRAWAL", "CREDIT CARD"};
        for (String query : searchQueries) {
            List<Integer> occurrences = tree.search(query);
            System.out.println("\nQuery: \"" + query + "\"");
            System.out.println("  Exists: " + tree.contains(query));
            System.out.println("  Count: " + tree.countOccurrences(query));
            System.out.println("  Indices: " + occurrences);
            for (int idx : occurrences) {
                System.out.println("    Match at index " + idx + ": \"" + financialDoc.substring(idx, Math.min(idx + 25, financialDoc.length())) + "...\"");
            }
        }
    }
}
