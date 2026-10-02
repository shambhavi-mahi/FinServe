package finserve.m1_string;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Aho-Corasick Algorithm
 * Banking use case: Detect multiple fraud patterns simultaneously.
 * Time Complexity: O(N + M + Z) where N is total pattern length, M is text length, Z is matches.
 * Space Complexity: O(N * alphabet)
 */
public class AhoCorasick {

    static class TrieNode {
        TrieNode[] children = new TrieNode[256];
        TrieNode fail;
        List<String> output = new ArrayList<>();
    }

    private TrieNode root;

    public AhoCorasick() {
        root = new TrieNode();
    }

    public void build(String[] words) {
        for (String word : words) {
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                if (node.children[c] == null) {
                    node.children[c] = new TrieNode();
                }
                node = node.children[c];
            }
            node.output.add(word);
        }

        Queue<TrieNode> queue = new LinkedList<>();
        for (int i = 0; i < 256; i++) {
            if (root.children[i] != null) {
                root.children[i].fail = root;
                queue.add(root.children[i]);
            } else {
                root.children[i] = root;
            }
        }

        while (!queue.isEmpty()) {
            TrieNode current = queue.poll();
            for (int i = 0; i < 256; i++) {
                if (current.children[i] != null && current.children[i] != root) {
                    TrieNode failNode = current.fail;
                    while (failNode.children[i] == null) {
                        failNode = failNode.fail;
                    }
                    current.children[i].fail = failNode.children[i];
                    current.children[i].output.addAll(current.children[i].fail.output);
                    queue.add(current.children[i]);
                }
            }
        }
    }

    public List<String> search(String text) {
        List<String> matches = new ArrayList<>();
        TrieNode node = root;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            while (node.children[c] == null) {
                node = node.fail;
            }
            node = node.children[c];
            for (String pattern : node.output) {
                if (!matches.contains(pattern)) {
                    matches.add(pattern);
                }
            }
        }
        return matches;
    }

    public static void main(String[] args) {
        AhoCorasick ac = new AhoCorasick();
        String[] patterns = {"OTP", "PHISHING", "SUSPICIOUS", "UNAUTHORIZED"};
        ac.build(patterns);
        
        System.out.println("Matches for TX2034: " + ac.search("TX2034 : USER RECEIVED PHISHING EMAIL"));
        System.out.println("Matches for TX2041: " + ac.search("TX2041 : UNAUTHORIZED TRANSACTION DETECTED"));
    }
}
