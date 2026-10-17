package finserve.m4_network_flow;

import java.util.*;

/**
 * Module 4: Network Flow
 * 
 * Financial Use Case: Strategic Banking IT & Fintech Product Selection under Infrastructure Dependencies
 * 
 * Theoretical Concepts Covered:
 * 1. Practical Reductions to Min-Cut:
 *    - The Project Selection Problem is a classic closure problem reducible to Minimum s-t Cut.
 *    - A commercial bank evaluates a portfolio of digital banking features:
 *      * Revenue-generating products P_i with expected revenue p_i > 0.
 *      * Prerequisite backend infrastructure / compliance modules R_j with cost c_j > 0.
 *      * Dependency Constraints: Feature P_i CANNOT be launched without building infrastructure R_j.
 *      * Shared Infrastructure: If multiple products require the same database/cloud module,
 *        the cost is paid only ONCE!
 * 
 * 2. Network Construction for Min-Cut Reduction:
 *    - Connect Source (s) -> each Revenue Product P_i with capacity = revenue p_i.
 *    - For each dependency "P_i requires R_j", add directed edge (P_i -> R_j) with capacity = ∞.
 *    - Connect each Infrastructure module R_j -> Sink (t) with capacity = cost c_j.
 * 
 * 3. Optimality via Max-Flow Min-Cut:
 *    - Find the Minimum Cut (S, T) where s ∈ S and t ∈ T.
 *    - The optimal portfolio to select is precisely the set of vertices in S \ {s}.
 *    - Maximum Net Profit = (Total Potential Revenue) - Capacity(Min-Cut).
 *    - Because ∞-capacity edges can NEVER be cut, no dependency constraint is ever violated!
 */
public class FintechProjectSelection {

    public static class RevenueProduct {
        public final String id;
        public final String name;
        public final int expectedRevenue; // in $K
        public final List<String> requiredPrerequisites;

        public RevenueProduct(String id, String name, int revenue, String... prereqs) {
            this.id = id;
            this.name = name;
            this.expectedRevenue = revenue;
            this.requiredPrerequisites = Arrays.asList(prereqs);
        }
    }

    public static class InfrastructureModule {
        public final String id;
        public final String name;
        public final int capitalCost; // in $K

        public InfrastructureModule(String id, String name, int cost) {
            this.id = id;
            this.name = name;
            this.capitalCost = cost;
        }
    }

    public static class PortfolioDecision {
        public final int totalPotentialRevenue;
        public final int minCutCost;
        public final int maxNetProfit;
        public final List<RevenueProduct> selectedProducts;
        public final List<InfrastructureModule> fundedInfrastructure;
        public final List<RevenueProduct> rejectedProducts;
        public final long executionTimeNs;

        public PortfolioDecision(int potential, int cut, int profit,
                                 List<RevenueProduct> selected, List<InfrastructureModule> infra,
                                 List<RevenueProduct> rejected, long timeNs) {
            this.totalPotentialRevenue = potential;
            this.minCutCost = cut;
            this.maxNetProfit = profit;
            this.selectedProducts = selected;
            this.fundedInfrastructure = infra;
            this.rejectedProducts = rejected;
            this.executionTimeNs = timeNs;
        }
    }

    public static PortfolioDecision optimizeFintechPortfolio(
            List<RevenueProduct> products, 
            List<InfrastructureModule> infrastructure) {

        long start = System.nanoTime();
        String SOURCE = "SOURCE_PORTFOLIO";
        String SINK = "SINK_PORTFOLIO";

        InterbankMaxFlow net = new InterbankMaxFlow();
        net.addBank(SOURCE);
        net.addBank(SINK);

        int totalPotentialRevenue = 0;
        int INF = 1_000_000_000; // Represents infinity (cannot be cut)

        // 1. Source -> Revenue Products
        for (RevenueProduct p : products) {
            net.addCreditChannel(SOURCE, p.id, p.expectedRevenue, "ProductRevenue");
            totalPotentialRevenue += p.expectedRevenue;
        }

        // 2. Dependencies: Product -> Prerequisite Infrastructure (Capacity = INF)
        for (RevenueProduct p : products) {
            for (String reqInfraId : p.requiredPrerequisites) {
                net.addCreditChannel(p.id, reqInfraId, INF, "PrerequisiteDependency");
            }
        }

        // 3. Infrastructure -> Sink (Capacity = Cost)
        for (InfrastructureModule infra : infrastructure) {
            net.addCreditChannel(infra.id, SINK, infra.capitalCost, "InfrastructureCost");
        }

        // Compute Min-Cut via Max-Flow
        InterbankMaxFlow.SettlementResult res = net.computeMaxSettlementFlow(SOURCE, SINK);

        // Selected nodes are in S \ {SOURCE}
        Map<String, RevenueProduct> prodMap = new HashMap<>();
        for (RevenueProduct p : products) prodMap.put(p.id, p);

        Map<String, InfrastructureModule> infraMap = new HashMap<>();
        for (InfrastructureModule m : infrastructure) infraMap.put(m.id, m);

        List<RevenueProduct> selectedProds = new ArrayList<>();
        List<RevenueProduct> rejectedProds = new ArrayList<>();
        List<InfrastructureModule> fundedInfra = new ArrayList<>();

        for (RevenueProduct p : products) {
            if (res.reachableBanksS.contains(p.id)) {
                selectedProds.add(p);
            } else {
                rejectedProds.add(p);
            }
        }

        for (InfrastructureModule m : infrastructure) {
            if (res.reachableBanksS.contains(m.id)) {
                fundedInfra.add(m);
            }
        }

        int maxNetProfit = totalPotentialRevenue - res.maxLiquidityFlow;
        long time = System.nanoTime() - start;

        return new PortfolioDecision(totalPotentialRevenue, res.maxLiquidityFlow, maxNetProfit,
                                     selectedProds, fundedInfra, rejectedProds, time);
    }

    public static void main(String[] args) {
        System.out.println("--- Fintech Product Portfolio Selection via Min-Cut Demo (FinServe) ---");
        List<InfrastructureModule> infra = Arrays.asList(
            new InfrastructureModule("INF-1", "Cloud Core Banking Migration", 120),
            new InfrastructureModule("INF-2", "Real-Time AML Streaming Pipeline", 80),
            new InfrastructureModule("INF-3", "HSM Hardware Security Crypto Vault", 150)
        );

        List<RevenueProduct> prods = Arrays.asList(
            new RevenueProduct("PRD-1", "Instant Cross-Border Payments", 180, "INF-1", "INF-2"),
            new RevenueProduct("PRD-2", "AI Robo-Advisor Wealth Engine", 140, "INF-1"),
            new RevenueProduct("PRD-3", "Institutional Crypto Custody", 90, "INF-3"), // Requires 150K infra, revenue only 90K -> Should be rejected!
            new RevenueProduct("PRD-4", "High-Frequency FX Hedging", 110, "INF-2")
        );

        PortfolioDecision decision = optimizeFintechPortfolio(prods, infra);
        System.out.println("Total Potential Gross Revenue: $" + decision.totalPotentialRevenue + "K");
        System.out.println("Min-Cut Penalty:               $" + decision.minCutCost + "K");
        System.out.println("Maximum Guaranteed Net Profit: $" + decision.maxNetProfit + "K");
        System.out.println("\n[APPROVED PRODUCTS]:");
        for (RevenueProduct p : decision.selectedProducts) {
            System.out.println("  * " + p.name + " (+$" + p.expectedRevenue + "K)");
        }
        System.out.println("\n[FUNDED PREREQUISITE INFRASTRUCTURE]:");
        for (InfrastructureModule m : decision.fundedInfrastructure) {
            System.out.println("  * " + m.name + " (-$" + m.capitalCost + "K)");
        }
        System.out.println("\n[REJECTED PRODUCTS (Unprofitable Prerequisite Overhead)]:");
        for (RevenueProduct p : decision.rejectedProducts) {
            System.out.println("  * " + p.name + " (Avoided unprofitable infra overhead!)");
        }
    }
}
