package finserve.m4_network_flow;

import java.util.*;

/**
 * Module 4: Network Flow
 * 
 * Financial Use Case: Loan Officer & KYC Compliance Underwriting Assignment
 * 
 * Theoretical Concepts Covered:
 * 1. Bipartite Matching via Max-Flow Reduction:
 *    - Bipartite Graph G = (L ∪ R, E):
 *      L: Loan Applicants / High-Risk KYC Cases requiring investigation.
 *      R: Certified Credit Officers / Underwriters with specialized skillsets.
 *    - Network Construction:
 *      1. Connect Super-Source (s) -> each Applicant u ∈ L with capacity = 1.
 *      2. Connect each Applicant u -> qualified Officer v ∈ R with capacity = 1.
 *      3. Connect each Officer v -> Super-Sink (t) with capacity = Case Limit (e.g., 1).
 * 
 * 2. The Integrality Theorem:
 *    - If all capacities are integers, the maximum flow will naturally be integral (all edge flows ∈ {0, 1}).
 *    - This guarantees a valid 1-to-1 or quota-based assignment without fractional split cases!
 * 
 * 3. König's Theorem & Bottleneck Analysis:
 *    - "In any bipartite graph, Maximum Matching Size = Minimum Vertex Cover Size."
 *    - The minimum vertex cover identifies the exact bottleneck set of officers/applicants
 *      capping the bank's daily underwriting capacity, showing where additional staff must be hired.
 */
public class BipartiteMatchingAssignment {

    public static class AssignmentPair {
        public final String applicantId;
        public final String applicantName;
        public final String loanType;
        public final String officerId;
        public final String officerName;
        public final String specialization;

        public AssignmentPair(String appId, String appName, String loanType, 
                              String offId, String offName, String spec) {
            this.applicantId = appId;
            this.applicantName = appName;
            this.loanType = loanType;
            this.officerId = offId;
            this.officerName = offName;
            this.specialization = spec;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s (%s) === Assigned To ===> [%s] %s (%s)",
                applicantId, applicantName, loanType, officerId, officerName, specialization);
        }
    }

    public static class MatchingResult {
        public final int totalAssigned;
        public final int totalApplicants;
        public final int totalOfficers;
        public final List<AssignmentPair> assignments;
        public final List<String> unassignedApplicants;
        public final Set<String> minVertexCoverBottlenecks;
        public final long executionTimeNs;

        public MatchingResult(int totalAssigned, int totalApps, int totalOffs,
                              List<AssignmentPair> pairs, List<String> unassigned,
                              Set<String> konigCover, long timeNs) {
            this.totalAssigned = totalAssigned;
            this.totalApplicants = totalApps;
            this.totalOfficers = totalOffs;
            this.assignments = pairs;
            this.unassignedApplicants = unassigned;
            this.minVertexCoverBottlenecks = konigCover;
            this.executionTimeNs = timeNs;
        }
    }

    public static class Applicant {
        public final String id;
        public final String name;
        public final String requiredSpecialization;

        public Applicant(String id, String name, String spec) {
            this.id = id;
            this.name = name;
            this.requiredSpecialization = spec;
        }
    }

    public static class Officer {
        public final String id;
        public final String name;
        public final Set<String> certifiedSpecializations = new HashSet<>();

        public Officer(String id, String name, String... specs) {
            this.id = id;
            this.name = name;
            Collections.addAll(this.certifiedSpecializations, specs);
        }
    }

    /**
     * Solves Maximum Bipartite Matching using Edmonds-Karp Max-Flow.
     * Computes the matching and extracts the Minimum Vertex Cover via König's Theorem.
     */
    public static MatchingResult solveUnderwritingAssignment(List<Applicant> applicants, List<Officer> officers) {
        long start = System.nanoTime();

        String SOURCE = "SOURCE_SUPER";
        String SINK = "SINK_SUPER";

        InterbankMaxFlow flowNet = new InterbankMaxFlow();
        flowNet.addBank(SOURCE);
        flowNet.addBank(SINK);

        // 1. Source -> Applicants
        for (Applicant app : applicants) {
            flowNet.addCreditChannel(SOURCE, app.id, 1, "AppDispatch");
        }

        // 2. Applicants -> Qualified Officers
        for (Applicant app : applicants) {
            for (Officer off : officers) {
                if (off.certifiedSpecializations.contains(app.requiredSpecialization)) {
                    flowNet.addCreditChannel(app.id, off.id, 1, "UnderwriteEligibility");
                }
            }
        }

        // 3. Officers -> Sink
        for (Officer off : officers) {
            flowNet.addCreditChannel(off.id, SINK, 1, "OfficerCapacity");
        }

        // Run Max Flow
        InterbankMaxFlow.SettlementResult flowRes = flowNet.computeMaxSettlementFlow(SOURCE, SINK);

        // Extract assignments where flow == 1 between applicant and officer
        Map<String, Applicant> appMap = new HashMap<>();
        for (Applicant a : applicants) appMap.put(a.id, a);

        Map<String, Officer> offMap = new HashMap<>();
        for (Officer o : officers) offMap.put(o.id, o);

        List<AssignmentPair> pairs = new ArrayList<>();
        Set<String> assignedAppIds = new HashSet<>();

        for (InterbankMaxFlow.FlowEdge edge : flowRes.allChannels) {
            if (appMap.containsKey(edge.from) && offMap.containsKey(edge.to) && edge.flow == 1) {
                Applicant a = appMap.get(edge.from);
                Officer o = offMap.get(edge.to);
                pairs.add(new AssignmentPair(a.id, a.name, a.requiredSpecialization, o.id, o.name, a.requiredSpecialization));
                assignedAppIds.add(a.id);
            }
        }

        List<String> unassigned = new ArrayList<>();
        for (Applicant a : applicants) {
            if (!assignedAppIds.contains(a.id)) {
                unassigned.add(a.id + " (" + a.name + ", Needs: " + a.requiredSpecialization + ")");
            }
        }

        // König's Theorem Minimum Vertex Cover extraction:
        // Vertices in L \ S (unreachable from source in residual graph) ∪ Vertices in R ∩ S (reachable in residual)
        Set<String> konigCover = new LinkedHashSet<>();
        for (Applicant a : applicants) {
            if (!flowRes.reachableBanksS.contains(a.id)) {
                konigCover.add("Applicant: " + a.name + " [" + a.id + "]");
            }
        }
        for (Officer o : officers) {
            if (flowRes.reachableBanksS.contains(o.id)) {
                konigCover.add("Officer: " + o.name + " [" + o.id + "]");
            }
        }

        long time = System.nanoTime() - start;
        return new MatchingResult(pairs.size(), applicants.size(), officers.size(), pairs, unassigned, konigCover, time);
    }

    public static void main(String[] args) {
        System.out.println("--- Bipartite Matching & König's Theorem Assignment Demo (FinServe) ---");
        List<Applicant> applicants = Arrays.asList(
            new Applicant("APP-1", "Apex Corp", "CommercialCredit"),
            new Applicant("APP-2", "GreenFuture Ltd", "GreenEnergyBond"),
            new Applicant("APP-3", "BioGen Pharma", "CommercialCredit"),
            new Applicant("APP-4", "Global Logistics", "ForexHedging"),
            new Applicant("APP-5", "Metropolis Dev", "RealEstateMortgage")
        );

        List<Officer> officers = Arrays.asList(
            new Officer("OFF-1", "Sarah Chen", "CommercialCredit", "ForexHedging"),
            new Officer("OFF-2", "David Miller", "GreenEnergyBond"),
            new Officer("OFF-3", "Elena Rossi", "CommercialCredit"),
            new Officer("OFF-4", "Marcus Vance", "RealEstateMortgage")
        );

        MatchingResult res = solveUnderwritingAssignment(applicants, officers);
        System.out.println("Total Successfully Assigned: " + res.totalAssigned + " / " + res.totalApplicants);
        for (AssignmentPair p : res.assignments) {
            System.out.println("  " + p);
        }
        System.out.println("König's Theorem Bottleneck Cover (Size = " + res.minVertexCoverBottlenecks.size() + "): " + res.minVertexCoverBottlenecks);
    }
}
