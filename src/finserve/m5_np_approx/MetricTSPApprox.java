package finserve.m5_np_approx;

import java.util.*;

/**
 * Module 5: NP-Completeness and Approximation Algorithms
 * 
 * Financial Use Case: Metropolitan ATM Cash Replenishment Logistics (Scalable Beyond Bitmask DP)
 * 
 * Problem Formulation:
 * A bank's cash logistics division dispatches armored vans from the central vault (depot)
 * to replenish cash across N metropolitan ATM locations and return to the vault.
 * The bank must minimize total travel distance and fuel cost.
 * 
 * NP-Completeness Context (The Reduction Zoo):
 * - HAM-CYCLE -> TSP:
 *   Given an unweighted graph G, assign edge weight 1 if an edge exists, and 2 (or M > 1) if not.
 *   G has a Hamiltonian Cycle if and only if TSP has a tour of cost exactly |V|.
 *   Hence, general TSP is NP-Hard and cannot be approximated within any constant ratio unless P = NP!
 * 
 * Metric-TSP 2-Approximation (Triangle Inequality):
 * - In real geographical road networks, distances satisfy the Triangle Inequality:
 *   cost(u, w) <= cost(u, v) + cost(v, w).
 * - Algorithm:
 *   1. Compute Minimum Spanning Tree (MST) of all ATM locations.
 *   2. Traverse MST using Depth-First Search (Eulerian doubling).
 *   3. Shortcut already visited ATMs.
 * - Ratio Guarantee Proof:
 *   - Removing any edge from the optimal TSP tour yields a spanning tree.
 *   - Therefore: weight(MST) <= OPT_TSP.
 *   - Doubling the MST edges gives an Eulerian tour of cost 2 * weight(MST) <= 2 * OPT_TSP.
 *   - By Triangle Inequality, shortcutting never increases tour length.
 *   - Tour Cost <= 2 * weight(MST) <= 2 * OPT_TSP (Guaranteed 2-Approximation!).
 */
public class MetricTSPApprox {

    public static class ATMNode {
        public final int id;
        public final String name;
        public final double x; // Coordinates in km
        public final double y;

        public ATMNode(int id, String name, double x, double y) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
        }

        public double distanceTo(ATMNode other) {
            return Math.hypot(this.x - other.x, this.y - other.y);
        }

        @Override
        public String toString() {
            return String.format("%s (%.1f, %.1f)", name, x, y);
        }
    }

    public static class TSPTourResult {
        public final List<ATMNode> tour;
        public final double totalDistanceKm;
        public final double mstWeightKm;
        public final double theoreticalUpperBoundKm;
        public final long executionTimeNs;

        public TSPTourResult(List<ATMNode> tour, double totalDist, double mstWeight, long timeNs) {
            this.tour = tour;
            this.totalDistanceKm = totalDist;
            this.mstWeightKm = mstWeight;
            this.theoreticalUpperBoundKm = 2.0 * mstWeight;
            this.executionTimeNs = timeNs;
        }
    }

    /**
     * Metric TSP 2-Approximation using MST Doubling & Shortcutting.
     * Time Complexity: O(V^2)
     * Approximation Ratio: <= 2.0 * OPT
     */
    public static TSPTourResult solveMetricTSP(List<ATMNode> atms) {
        long start = System.nanoTime();
        int n = atms.size();
        if (n <= 1) {
            return new TSPTourResult(atms, 0.0, 0.0, 0);
        }

        // 1. Build distance matrix
        double[][] dist = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = atms.get(i).distanceTo(atms.get(j));
            }
        }

        // 2. Prim's Algorithm to compute MST
        boolean[] inMST = new boolean[n];
        double[] minEdge = new double[n];
        int[] parent = new int[n];
        Arrays.fill(minEdge, Double.MAX_VALUE);
        Arrays.fill(parent, -1);
        minEdge[0] = 0.0;

        double mstWeight = 0.0;
        List<List<Integer>> mstAdj = new ArrayList<>();
        for (int i = 0; i < n; i++) mstAdj.add(new ArrayList<>());

        for (int step = 0; step < n; step++) {
            int u = -1;
            for (int i = 0; i < n; i++) {
                if (!inMST[i] && (u == -1 || minEdge[i] < minEdge[u])) {
                    u = i;
                }
            }

            inMST[u] = true;
            mstWeight += minEdge[u];
            if (parent[u] != -1) {
                mstAdj.get(parent[u]).add(u);
                mstAdj.get(u).add(parent[u]);
            }

            for (int v = 0; v < n; v++) {
                if (!inMST[v] && dist[u][v] < minEdge[v]) {
                    minEdge[v] = dist[u][v];
                    parent[v] = u;
                }
            }
        }

        // 3. DFS Traversal for Preorder Walk (Shortcutting)
        List<Integer> tourIndices = new ArrayList<>();
        boolean[] visited = new boolean[n];
        dfsWalk(0, mstAdj, visited, tourIndices);
        tourIndices.add(0); // Return to depot

        // Calculate tour length
        double totalTourDist = 0.0;
        List<ATMNode> tourNodes = new ArrayList<>();
        for (int idx : tourIndices) {
            tourNodes.add(atms.get(idx));
        }

        for (int i = 0; i < tourIndices.size() - 1; i++) {
            int u = tourIndices.get(i);
            int v = tourIndices.get(i + 1);
            totalTourDist += dist[u][v];
        }

        long time = System.nanoTime() - start;
        return new TSPTourResult(tourNodes, totalTourDist, mstWeight, time);
    }

    private static void dfsWalk(int u, List<List<Integer>> adj, boolean[] visited, List<Integer> tour) {
        visited[u] = true;
        tour.add(u);
        for (int v : adj.get(u)) {
            if (!visited[v]) {
                dfsWalk(v, adj, visited, tour);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("--- Metric-TSP 2-Approximation Demo (FinServe) ---");
        List<ATMNode> atms = Arrays.asList(
            new ATMNode(0, "Central Vault", 0, 0),
            new ATMNode(1, "North Branch", 5, 12),
            new ATMNode(2, "Airport Kiosk", 18, 8),
            new ATMNode(3, "Downtown ATM", 8, 3)
        );
        TSPTourResult res = solveMetricTSP(atms);
        System.out.printf("Tour Distance: %.2f km (MST: %.2f km, Bound: %.2f km)\n", 
            res.totalDistanceKm, res.mstWeightKm, res.theoreticalUpperBoundKm);
        System.out.println("Tour: " + res.tour);
    }
}
