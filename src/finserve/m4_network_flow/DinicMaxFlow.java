package finserve.m4_network_flow;

import java.util.*;

/**
 * Module 4: Network Flow
 * 
 * Financial Use Case: High-Throughput RTGS Payment Settlement Engine
 * 
 * Theoretical Concepts Covered:
 * 1. Dinic's Algorithm (Yefim Dinitz, 1970):
 *    - Overcomes the O(V * E^2) limitation of Edmonds-Karp by pushing "blocking flows"
 *      through a layered "Level Graph".
 *    - Time Complexity: O(V^2 * E) on general networks; O(E * sqrt(V)) on unit networks.
 * 
 * 2. Two Alternating Phases:
 *    - Phase 1: Level Graph Construction (BFS)
 *      Assigns each vertex v a level: level[v] = shortest distance from source in residual graph.
 *      Only edges with level[to] == level[from] + 1 are considered.
 *    - Phase 2: Blocking Flow via DFS with Current-Edge Pointer Optimization
 *      Pushes flow until no more paths of length level[t] exist.
 *      The 'ptr[]' array ensures saturated or dead-end edges are never traversed again.
 * 
 * 3. Banking System Performance:
 *    - Critical for large clearing networks with thousands of correspondent banking connections,
 *      ensuring real-time liquidity matching without quadratic path rebuilding overhead.
 */
public class DinicMaxFlow {

    public static class Edge {
        public final int to;
        public final int rev; // Index of reverse edge in adj[to]
        public final int capacity;
        public int flow;
        public final String label;

        public Edge(int to, int rev, int capacity, String label) {
            this.to = to;
            this.rev = rev;
            this.capacity = capacity;
            this.flow = 0;
            this.label = label;
        }

        public int remainingCapacity() {
            return capacity - flow;
        }
    }

    private final int numVertices;
    private final List<List<Edge>> adj;
    private final int[] level;
    private final int[] ptr;
    private final Map<String, Integer> bankToId = new HashMap<>();
    private final Map<Integer, String> idToBank = new HashMap<>();
    private int vertexCounter = 0;

    public DinicMaxFlow(int expectedVertices) {
        this.numVertices = expectedVertices;
        this.adj = new ArrayList<>(expectedVertices);
        for (int i = 0; i < expectedVertices; i++) {
            adj.add(new ArrayList<>());
        }
        this.level = new int[expectedVertices];
        this.ptr = new int[expectedVertices];
    }

    public int getOrAddBank(String name) {
        if (!bankToId.containsKey(name)) {
            int id = vertexCounter++;
            bankToId.put(name, id);
            idToBank.put(id, name);
        }
        return bankToId.get(name);
    }

    public void addCorridor(String fromName, String toName, int capacity, String corridorLabel) {
        int u = getOrAddBank(fromName);
        int v = getOrAddBank(toName);

        Edge forward = new Edge(v, adj.get(v).size(), capacity, corridorLabel);
        Edge backward = new Edge(u, adj.get(u).size(), 0, "Reverse-" + corridorLabel);

        adj.get(u).add(forward);
        adj.get(v).add(backward);
    }

    private boolean bfsLevelGraph(int s, int t) {
        Arrays.fill(level, -1);
        level[s] = 0;
        Queue<Integer> q = new LinkedList<>();
        q.add(s);

        while (!q.isEmpty()) {
            int u = q.poll();
            for (Edge e : adj.get(u)) {
                if (e.remainingCapacity() > 0 && level[e.to] < 0) {
                    level[e.to] = level[u] + 1;
                    q.add(e.to);
                }
            }
        }
        return level[t] >= 0;
    }

    private int dfsBlockingFlow(int u, int t, int pushed) {
        if (pushed == 0 || u == t) return pushed;

        for (int cid = ptr[u]; cid < adj.get(u).size(); cid++) {
            ptr[u] = cid;
            Edge e = adj.get(u).get(cid);
            int tr = e.to;

            if (level[tr] != level[u] + 1 || e.remainingCapacity() == 0) continue;

            int trPushed = dfsBlockingFlow(tr, t, Math.min(pushed, e.remainingCapacity()));
            if (trPushed == 0) continue;

            e.flow += trPushed;
            adj.get(tr).get(e.rev).flow -= trPushed;
            return trPushed;
        }
        return 0;
    }

    public static class DinicResult {
        public final int maxFlow;
        public final int numPhases;
        public final long executionTimeNs;
        public final String complexity;

        public DinicResult(int flow, int phases, long timeNs) {
            this.maxFlow = flow;
            this.numPhases = phases;
            this.executionTimeNs = timeNs;
            this.complexity = "O(V^2 * E) Blocking Flow (Dinic)";
        }
    }

    public DinicResult computeDinicMaxFlow(String sourceName, String sinkName) {
        long start = System.nanoTime();
        int s = getOrAddBank(sourceName);
        int t = getOrAddBank(sinkName);

        int totalFlow = 0;
        int phases = 0;

        while (bfsLevelGraph(s, t)) {
            phases++;
            Arrays.fill(ptr, 0);
            while (true) {
                int pushed = dfsBlockingFlow(s, t, Integer.MAX_VALUE);
                if (pushed == 0) break;
                totalFlow += pushed;
            }
        }

        long time = System.nanoTime() - start;
        return new DinicResult(totalFlow, phases, time);
    }

    public static void main(String[] args) {
        System.out.println("--- Dinic's Algorithm Blocking Flow Demo (FinServe) ---");
        DinicMaxFlow dinic = new DinicMaxFlow(10);
        dinic.addCorridor("CentralVault", "HubEast", 150, "Corridor-1");
        dinic.addCorridor("CentralVault", "HubWest", 120, "Corridor-2");
        dinic.addCorridor("HubEast", "RegionalNorth", 90, "Corridor-3");
        dinic.addCorridor("HubEast", "RegionalSouth", 80, "Corridor-4");
        dinic.addCorridor("HubWest", "RegionalSouth", 70, "Corridor-5");
        dinic.addCorridor("RegionalNorth", "ClearingTerminal", 110, "Corridor-6");
        dinic.addCorridor("RegionalSouth", "ClearingTerminal", 130, "Corridor-7");

        DinicResult res = dinic.computeDinicMaxFlow("CentralVault", "ClearingTerminal");
        System.out.println("Total Settled Liquidity: $" + res.maxFlow + "M");
        System.out.println("BFS Level Graph Phases: " + res.numPhases);
        System.out.println("Time: " + (res.executionTimeNs / 1_000_000.0) + " ms (" + res.complexity + ")");
    }
}
