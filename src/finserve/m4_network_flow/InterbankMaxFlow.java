package finserve.m4_network_flow;

import java.util.*;

/**
 * Module 4: Network Flow
 * 
 * Financial Use Case: Interbank Liquidity Clearing & Systemic Risk Bottleneck Identification
 * 
 * Theoretical Concepts Covered:
 * 1. The Max-Flow Problem:
 *    - Source (s): Central Bank / Originating Liquidity Pool.
 *    - Sink (t): Settlement Clearing House / Destination Bank.
 *    - Capacity c(u, v): Bilateral interbank credit limit / maximum transfer volume per hour ($ millions).
 *    - Conservation of Flow: For every intermediary bank, total inflow equals total outflow.
 *    - Integer Capacity Setup: Integrality Theorem guarantees that all settled flows are whole integers.
 * 
 * 2. The Ford-Fulkerson Method & Edmonds-Karp Algorithm:
 *    - Augmenting paths found in the residual graph using Breadth-First Search (BFS).
 *    - Time Complexity: O(V * E^2).
 *    - Correctness: Based on the Max-Flow Min-Cut Theorem.
 * 
 * 3. Min-Cut as the Dual of Max-Flow:
 *    - A cut (S, T) partitions vertices into S (reachable from s in residual graph) and T (the rest).
 *    - Capacity of the minimum cut equals the value of the maximum flow (Max-Flow Min-Cut Theorem).
 *    - In banking: Saturated edges crossing from S to T represent the critical interbank credit bottlenecks
 *      whose exhaustion limits the liquidity of the entire banking system (Systemic Risk Indicator).
 */
public class InterbankMaxFlow {

    public static class FlowEdge {
        public final String from;
        public final String to;
        public final int capacity;
        public int flow;
        public final String channelName;

        public FlowEdge(String from, String to, int capacity, String channelName) {
            this.from = from;
            this.to = to;
            this.capacity = capacity;
            this.flow = 0;
            this.channelName = channelName;
        }

        public int residualCapacityTo(String target) {
            if (target.equals(to)) return capacity - flow;
            else if (target.equals(from)) return flow;
            throw new IllegalArgumentException("Invalid edge endpoint");
        }

        public void addResidualFlowTo(String target, int delta) {
            if (target.equals(to)) flow += delta;
            else if (target.equals(from)) flow -= delta;
            else throw new IllegalArgumentException("Invalid edge endpoint");
        }

        @Override
        public String toString() {
            return String.format("%s -> %s [%s]: Flow = $%dM / Cap = $%dM (%.1f%% utilized)", 
                from, to, channelName, flow, capacity, capacity > 0 ? (flow * 100.0 / capacity) : 0);
        }
    }

    public static class SettlementResult {
        public final int maxLiquidityFlow;
        public final List<FlowEdge> allChannels;
        public final List<FlowEdge> minCutBottleneckChannels;
        public final Set<String> reachableBanksS;
        public final Set<String> unreachableBanksT;
        public final long executionTimeNs;

        public SettlementResult(int maxFlow, List<FlowEdge> edges, List<FlowEdge> cutEdges,
                                Set<String> setS, Set<String> setT, long timeNs) {
            this.maxLiquidityFlow = maxFlow;
            this.allChannels = edges;
            this.minCutBottleneckChannels = cutEdges;
            this.reachableBanksS = setS;
            this.unreachableBanksT = setT;
            this.executionTimeNs = timeNs;
        }
    }

    private final Map<String, List<FlowEdge>> adj = new HashMap<>();
    private final List<FlowEdge> edgeList = new ArrayList<>();
    private final Set<String> banks = new LinkedHashSet<>();

    public void addBank(String bank) {
        banks.add(bank);
        adj.putIfAbsent(bank, new ArrayList<>());
    }

    public void addCreditChannel(String from, String to, int capacity, String channelName) {
        addBank(from);
        addBank(to);
        FlowEdge edge = new FlowEdge(from, to, capacity, channelName);
        adj.get(from).add(edge);
        adj.get(to).add(edge); // Back edge reference for residual flow
        edgeList.add(edge);
    }

    /**
     * Edmonds-Karp Algorithm:
     * Uses BFS to find shortest augmenting paths in residual network.
     * Computes Max Flow and dual Min Cut.
     */
    public SettlementResult computeMaxSettlementFlow(String source, String sink) {
        long start = System.nanoTime();
        int maxFlow = 0;

        // Reset flows
        for (FlowEdge e : edgeList) e.flow = 0;

        while (true) {
            // BFS to find augmenting path
            Map<String, FlowEdge> edgeTo = new HashMap<>();
            Queue<String> queue = new LinkedList<>();
            Set<String> visited = new HashSet<>();

            queue.add(source);
            visited.add(source);

            while (!queue.isEmpty() && !visited.contains(sink)) {
                String u = queue.poll();
                for (FlowEdge edge : adj.get(u)) {
                    String v = edge.from.equals(u) ? edge.to : edge.from;
                    if (!visited.contains(v) && edge.residualCapacityTo(v) > 0) {
                        visited.add(v);
                        edgeTo.put(v, edge);
                        queue.add(v);
                    }
                }
            }

            // If sink is not reachable in residual graph, max flow reached
            if (!visited.contains(sink)) break;

            // Find bottleneck capacity along path
            int bottleneck = Integer.MAX_VALUE;
            for (String v = sink; !v.equals(source); ) {
                FlowEdge edge = edgeTo.get(v);
                String u = edge.from.equals(v) ? edge.to : edge.from;
                bottleneck = Math.min(bottleneck, edge.residualCapacityTo(v));
                v = u;
            }

            // Augment flow along path
            for (String v = sink; !v.equals(source); ) {
                FlowEdge edge = edgeTo.get(v);
                String u = edge.from.equals(v) ? edge.to : edge.from;
                edge.addResidualFlowTo(v, bottleneck);
                v = u;
            }

            maxFlow += bottleneck;
        }

        // Compute Min-Cut via BFS reachable set from source in residual graph
        Set<String> setS = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(source);
        setS.add(source);

        while (!queue.isEmpty()) {
            String u = queue.poll();
            for (FlowEdge edge : adj.get(u)) {
                String v = edge.from.equals(u) ? edge.to : edge.from;
                if (!setS.contains(v) && edge.residualCapacityTo(v) > 0) {
                    setS.add(v);
                    queue.add(v);
                }
            }
        }

        Set<String> setT = new HashSet<>(banks);
        setT.removeAll(setS);

        // Min-Cut edges are saturated edges directed from S to T
        List<FlowEdge> minCutEdges = new ArrayList<>();
        for (FlowEdge edge : edgeList) {
            if (setS.contains(edge.from) && setT.contains(edge.to) && edge.flow == edge.capacity) {
                minCutEdges.add(edge);
            }
        }

        long time = System.nanoTime() - start;
        return new SettlementResult(maxFlow, edgeList, minCutEdges, setS, setT, time);
    }

    public static void main(String[] args) {
        System.out.println("--- Interbank Max-Flow & Min-Cut Demo (FinServe) ---");
        InterbankMaxFlow net = new InterbankMaxFlow();
        // Construct interbank liquidity network
        net.addCreditChannel("CentralReserve", "Bank_A", 100, "FedCreditLine");
        net.addCreditChannel("CentralReserve", "Bank_B", 80, "RepoWindow");
        net.addCreditChannel("Bank_A", "Bank_C", 50, "BilateralRTGS");
        net.addCreditChannel("Bank_A", "Bank_D", 60, "CorrespondentLine");
        net.addCreditChannel("Bank_B", "Bank_D", 70, "LiquiditySwap");
        net.addCreditChannel("Bank_C", "ClearingHouse", 60, "ACHSettlement");
        net.addCreditChannel("Bank_D", "ClearingHouse", 90, "CHIPSSettlement");

        SettlementResult res = net.computeMaxSettlementFlow("CentralReserve", "ClearingHouse");
        System.out.println("Max Hourly Settled Liquidity: $" + res.maxLiquidityFlow + "M");
        System.out.println("Systemic Bottleneck Channels (Min-Cut):");
        for (FlowEdge e : res.minCutBottleneckChannels) {
            System.out.println("  * " + e);
        }
    }
}
