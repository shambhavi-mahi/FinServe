package finserve.m5_np_approx;

import java.util.*;

/**
 * Module 5: NP-Completeness and Approximation Algorithms
 * 
 * Financial Use Case: Fraud Ring Surveillance & Mule Account Interception
 * 
 * Problem Formulation:
 * In a financial transaction network G = (V, E), vertices represent bank accounts 
 * and edges represent flagged high-risk or suspicious transfers between two accounts.
 * Regulators require the bank to place under surveillance a subset of accounts C ⊆ V
 * such that EVERY suspicious transfer (edge) is monitored by at least one account in C.
 * This is the canonical Minimum Vertex Cover problem.
 * 
 * NP-Completeness Context (The Reduction Zoo):
 * - Cook-Levin: SAT is NP-Complete.
 * - 3-SAT -> CLIQUE -> INDEPENDENT-SET -> VERTEX-COVER.
 * - Decision Problem: Does there exist a vertex cover of size <= k? (NP-Complete)
 * - Optimisation Problem: Find a vertex cover of minimum possible size |C*|. (NP-Hard)
 * 
 * Approximation Algorithm:
 * - Maximal Matching 2-Approximation:
 *   1. Find a maximal matching M of independent edges (no two edges share a vertex).
 *   2. For each edge (u, v) in M, add BOTH endpoints u and v to cover C.
 * - Ratio Guarantee: |C| <= 2 * |C*|.
 *   Proof: Since M is a matching, no two edges in M share an endpoint. Therefore, any valid
 *   vertex cover must pick at least one endpoint from each edge in M, so |C*| >= |M|.
 *   Our algorithm selects exactly 2 * |M| vertices. Thus |C| = 2 * |M| <= 2 * |C*|.
 * 
 * Parameterized Complexity Preview (FPT):
 * - If regulators give a small budget k of accounts that can be frozen/monitored,
 *   a search-tree branching algorithm solves k-Vertex Cover in O(2^k * (V + E)) time.
 *   The exponential is confined strictly to parameter k, not input size N!
 */
public class VertexCoverApprox {

    public static class Edge {
        public final String u;
        public final String v;
        public final String reason;

        public Edge(String u, String v, String reason) {
            this.u = u;
            this.v = v;
            this.reason = reason;
        }

        @Override
        public String toString() {
            return u + " <--> " + v + " [" + reason + "]";
        }
    }

    public static class SurveillancePlan {
        public final Set<String> monitoredAccounts;
        public final List<Edge> maximalMatchingEdges;
        public final int theoreticalUpperRatio;

        public SurveillancePlan(Set<String> accounts, List<Edge> matching) {
            this.monitoredAccounts = accounts;
            this.maximalMatchingEdges = matching;
            this.theoreticalUpperRatio = 2;
        }
    }

    /**
     * Maximal Matching 2-Approximation for Minimum Vertex Cover.
     * Time Complexity: O(V + E)
     * Approximation Ratio: <= 2.0 (guaranteed at most twice the optimal minimum)
     */
    public static SurveillancePlan compute2Approximation(Set<String> accounts, List<Edge> suspiciousEdges) {
        Set<String> cover = new LinkedHashSet<>();
        List<Edge> matching = new ArrayList<>();
        Set<String> matchedVertices = new HashSet<>();

        // Greedily construct a maximal matching
        for (Edge edge : suspiciousEdges) {
            if (!matchedVertices.contains(edge.u) && !matchedVertices.contains(edge.v)) {
                // Add both endpoints to cover
                cover.add(edge.u);
                cover.add(edge.v);
                matching.add(edge);
                matchedVertices.add(edge.u);
                matchedVertices.add(edge.v);
            }
        }

        return new SurveillancePlan(cover, matching);
    }

    /**
     * Parameterized Complexity (FPT) Bounded Search Tree:
     * Decides if there exists a vertex cover of size <= k in O(2^k * E) time.
     * Practical when regulator budget k is small (e.g., k <= 15).
     */
    public static boolean hasKVertexCoverFPT(List<Edge> edges, int k, Set<String> currentCover) {
        if (edges.isEmpty()) return true;
        if (k <= 0) return false;

        // Pick an uncovered edge (u, v)
        Edge first = edges.get(0);

        // Branch 1: Include u in vertex cover
        List<Edge> remainingU = new ArrayList<>();
        for (Edge e : edges) {
            if (!e.u.equals(first.u) && !e.v.equals(first.u)) {
                remainingU.add(e);
            }
        }
        currentCover.add(first.u);
        if (hasKVertexCoverFPT(remainingU, k - 1, currentCover)) return true;
        currentCover.remove(first.u);

        // Branch 2: Include v in vertex cover
        List<Edge> remainingV = new ArrayList<>();
        for (Edge e : edges) {
            if (!e.u.equals(first.v) && !e.v.equals(first.v)) {
                remainingV.add(e);
            }
        }
        currentCover.add(first.v);
        if (hasKVertexCoverFPT(remainingV, k - 1, currentCover)) return true;
        currentCover.remove(first.v);

        return false;
    }

    /**
     * Practical Engineer's Checklist for Recognizing NP-Hardness in Banking:
     */
    public static String getEngineerChecklist() {
        return "============================================================\n" +
               "PRACTICAL ENGINEER'S NP-HARDNESS CHECKLIST (FINANCIAL SYSTEMS)\n" +
               "============================================================\n" +
               "1. SUBSET / SELECTION PROBLEMS:\n" +
               "   - Selecting optimal loans under reserve budget -> 0/1 KNAPSACK (NP-Hard)\n" +
               "   - Exact ledger settlement balancing -> SUBSET-SUM (NP-Complete)\n" +
               "2. COVERING / SURVEILLANCE PROBLEMS:\n" +
               "   - Intercepting all mule transfers with minimal accounts -> VERTEX-COVER (NP-Complete)\n" +
               "   - Audit team coverage of branches -> SET-COVER (NP-Hard, log n approx)\n" +
               "3. SEQUENCING / ORDERING PROBLEMS:\n" +
               "   - ATM cash van routing -> TSP / HAM-CYCLE (NP-Hard)\n" +
               "   - Optimal batch settlement sequencing with setup costs -> HAM-PATH (NP-Hard)\n" +
               "4. PARTITIONING PROBLEMS:\n" +
               "   - Splitting audit caseload evenly between 2 server nodes -> PARTITION (NP-Complete)\n" +
               "REMEDY MATRIX:\n" +
               " - Exact needed & small parameter k? -> Use FPT (Parameterized O(2^k * N))\n" +
               " - Bounded error acceptable? -> Use Approximation (e.g. 2-approx, FPTAS (1+ε))\n" +
               " - Small integers? -> Pseudo-polynomial DP (e.g. Knapsack O(nW))\n" +
               "============================================================";
    }

    public static void main(String[] args) {
        System.out.println("--- Vertex Cover 2-Approximation & FPT Demo (FinServe) ---");
        List<Edge> edges = Arrays.asList(
            new Edge("Acc-101", "Acc-204", "Layering Transfer"),
            new Edge("Acc-204", "Acc-309", "Wire Transfer"),
            new Edge("Acc-101", "Acc-412", "Cash Smurfing"),
            new Edge("Acc-412", "Acc-505", "Crypto Gateway")
        );
        Set<String> vertices = new HashSet<>(Arrays.asList("Acc-101", "Acc-204", "Acc-309", "Acc-412", "Acc-505"));

        SurveillancePlan plan = compute2Approximation(vertices, edges);
        System.out.println("Maximal Matching Edges: " + plan.maximalMatchingEdges);
        System.out.println("2-Approximation Monitored Accounts: " + plan.monitoredAccounts);

        Set<String> fptCover = new HashSet<>();
        boolean fptPossible = hasKVertexCoverFPT(edges, 2, fptCover);
        System.out.println("FPT k=2 Cover Possible: " + fptPossible + " -> " + fptCover);
    }
}
