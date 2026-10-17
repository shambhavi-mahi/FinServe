package finserve.m4_network_flow;

import java.util.*;

/**
 * Module 4: Network Flow
 * 
 * Financial Use Case: Cross-Border Multi-Currency Payment Routing & Fee Minimization (MCMF)
 * 
 * Theoretical Concepts Covered:
 * 1. Min-Cost Max-Flow (MCMF) Formulation:
 *    - Each interbank payment corridor has:
 *        1. Capacity u(e): Maximum transferable liquidity per settlement cycle ($ millions).
 *        2. Unit Cost c(e): Transaction routing fee, SWIFT messaging cost, or FX margin per unit ($K per $M).
 *    - Objective: Route target liquidity F from Source (Originating Bank) to Sink (Beneficiary Bank)
 *      such that Total Routing Cost ∑ c(e) * f(e) is minimized while satisfying capacity constraints.
 * 
 * 2. Successive Shortest Path (SSP) Algorithm:
 *    - In the residual graph, a reverse edge has capacity 0, flow -f(e), and unit cost -c(e).
 *    - SSP repeatedly finds the shortest path (minimum fee path) in the residual graph using
 *      SPFA (Shortest Path Faster Algorithm) / Bellman-Ford to handle negative residual costs.
 *    - Augments flow along the cheapest path until the required liquidity is routed or no paths remain.
 * 
 * 3. Cycle-Cancelling Intuition:
 *    - A flow is of minimum cost if and only if its residual network contains NO negative-cost directed cycles.
 *    - Pushing flow around a negative cycle decreases total cost while preserving flow conservation.
 */
public class MinCostPaymentRouting {

    public static class CorridorEdge {
        public final int from;
        public final int to;
        public final int capacity;
        public int flow;
        public final double unitFee; // Fee in $K per $1M transferred
        public final String corridorName;
        public int revIndex;

        public CorridorEdge(int from, int to, int capacity, double fee, String name) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.flow = 0;
            this.unitFee = fee;
            this.corridorName = name;
        }

        public int remainingCapacity() {
            return capacity - flow;
        }

        @Override
        public String toString() {
            return String.format("[%s]: Routed = $%dM / Cap = $%dM | Fee Rate = $%.2fK/M | Total Fee = $%.2fK",
                corridorName, flow, capacity, unitFee, flow * unitFee);
        }
    }

    public static class RoutingPlan {
        public final int totalLiquidityRouted;
        public final double totalFeesPaid;
        public final List<CorridorEdge> activeCorridors;
        public final long executionTimeNs;

        public RoutingPlan(int flow, double fees, List<CorridorEdge> active, long timeNs) {
            this.totalLiquidityRouted = flow;
            this.totalFeesPaid = fees;
            this.activeCorridors = active;
            this.executionTimeNs = timeNs;
        }
    }

    private final int V;
    private final List<List<CorridorEdge>> adj;
    private final Map<String, Integer> bankIds = new HashMap<>();
    private final Map<Integer, String> bankNames = new HashMap<>();
    private int counter = 0;

    public MinCostPaymentRouting(int expectedVertices) {
        this.V = expectedVertices;
        this.adj = new ArrayList<>(expectedVertices);
        for (int i = 0; i < expectedVertices; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public int getOrAddBank(String name) {
        if (!bankIds.containsKey(name)) {
            int id = counter++;
            bankIds.put(name, id);
            bankNames.put(id, name);
        }
        return bankIds.get(name);
    }

    public void addCorridor(String from, String to, int capacity, double feePerUnit, String corridorName) {
        int u = getOrAddBank(from);
        int v = getOrAddBank(to);

        CorridorEdge forward = new CorridorEdge(u, v, capacity, feePerUnit, corridorName);
        CorridorEdge reverse = new CorridorEdge(v, u, 0, -feePerUnit, "Rev-" + corridorName);

        forward.revIndex = adj.get(v).size();
        reverse.revIndex = adj.get(u).size();

        adj.get(u).add(forward);
        adj.get(v).add(reverse);
    }

    /**
     * Successive Shortest Path (SSP) Min-Cost Max-Flow:
     * Routes up to targetLiquidity while minimizing total fees.
     */
    public RoutingPlan routeOptimalPayment(String sourceBank, String sinkBank, int targetLiquidity) {
        long start = System.nanoTime();
        int s = getOrAddBank(sourceBank);
        int t = getOrAddBank(sinkBank);

        int totalFlow = 0;
        double totalCost = 0.0;

        while (totalFlow < targetLiquidity) {
            // SPFA to find minimum-cost augmenting path
            double[] dist = new double[V];
            int[] parentEdge = new int[V];
            int[] parentNode = new int[V];
            boolean[] inQueue = new boolean[V];
            Arrays.fill(dist, Double.MAX_VALUE);
            Arrays.fill(parentNode, -1);

            Queue<Integer> q = new LinkedList<>();
            q.add(s);
            dist[s] = 0.0;
            inQueue[s] = true;

            while (!q.isEmpty()) {
                int u = q.poll();
                inQueue[u] = false;

                for (int i = 0; i < adj.get(u).size(); i++) {
                    CorridorEdge e = adj.get(u).get(i);
                    if (e.remainingCapacity() > 0 && dist[e.to] > dist[u] + e.unitFee + 1e-9) {
                        dist[e.to] = dist[u] + e.unitFee;
                        parentNode[e.to] = u;
                        parentEdge[e.to] = i;

                        if (!inQueue[e.to]) {
                            q.add(e.to);
                            inQueue[e.to] = true;
                        }
                    }
                }
            }

            // No augmenting path exists
            if (parentNode[t] == -1) break;

            // Find bottleneck capacity along path
            int push = targetLiquidity - totalFlow;
            for (int curr = t; curr != s; curr = parentNode[curr]) {
                CorridorEdge e = adj.get(parentNode[curr]).get(parentEdge[curr]);
                push = Math.min(push, e.remainingCapacity());
            }

            // Augment flow
            for (int curr = t; curr != s; curr = parentNode[curr]) {
                CorridorEdge forward = adj.get(parentNode[curr]).get(parentEdge[curr]);
                CorridorEdge backward = adj.get(curr).get(forward.revIndex);

                forward.flow += push;
                backward.flow -= push;
                totalCost += push * forward.unitFee;
            }

            totalFlow += push;
        }

        // Collect all forward corridors carrying flow
        List<CorridorEdge> active = new ArrayList<>();
        for (List<CorridorEdge> edges : adj) {
            for (CorridorEdge e : edges) {
                if (e.capacity > 0 && e.flow > 0) {
                    active.add(e);
                }
            }
        }

        long time = System.nanoTime() - start;
        return new RoutingPlan(totalFlow, totalCost, active, time);
    }

    public static void main(String[] args) {
        System.out.println("--- Min-Cost Max-Flow Payment Routing Demo (FinServe) ---");
        MinCostPaymentRouting router = new MinCostPaymentRouting(10);
        // Correspondent banking payment corridors: from, to, capacity ($M), fee per $1M ($K), label
        router.addCorridor("NewYork_Fed", "London_Hub", 60, 2.5, "USD_GBP_Corridor");
        router.addCorridor("NewYork_Fed", "Frankfurt_Hub", 40, 1.8, "USD_EUR_Corridor");
        router.addCorridor("London_Hub", "Tokyo_BOJ", 50, 4.0, "GBP_JPY_Corridor");
        router.addCorridor("Frankfurt_Hub", "Tokyo_BOJ", 30, 3.2, "EUR_JPY_Direct");
        router.addCorridor("Frankfurt_Hub", "London_Hub", 25, 0.5, "Inter-European_Relay");

        int targetTransfer = 70; // Desire to route $70M
        RoutingPlan plan = router.routeOptimalPayment("NewYork_Fed", "Tokyo_BOJ", targetTransfer);

        System.out.println("Total Transferred: $" + plan.totalLiquidityRouted + "M / $" + targetTransfer + "M");
        System.out.printf("Total Routing Fees Incurred: $%.2fK\n", plan.totalFeesPaid);
        System.out.println("Active Routing Channels:");
        for (CorridorEdge e : plan.activeCorridors) {
            System.out.println("  " + e);
        }
    }
}
