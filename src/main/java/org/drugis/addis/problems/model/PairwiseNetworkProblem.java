package org.drugis.addis.problems.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created by connor on 7/3/15.
 */
public class PairwiseNetworkProblem extends NetworkMetaAnalysisProblem {
    public PairwiseNetworkProblem(NetworkMetaAnalysisProblem problem, Pair<String, String> pairwiseDetails) {

        TreatmentEntry left = null;
        TreatmentEntry right = null;
        for(TreatmentEntry treatment : problem.getTreatments()){
            if(treatment.getName().equals(pairwiseDetails.getLeft())) {
                left = treatment;
            }
            if(treatment.getName().equals(pairwiseDetails.getRight())) {
                right = treatment;
            }
        }

        Set<AbstractNetworkMetaAnalysisProblemEntry> pairwiseEntries = new HashSet<>();
        for (AbstractNetworkMetaAnalysisProblemEntry entry : problem.getEntries()) {
            Integer treatmentId = entry.getTreatment();
            if(treatmentId.equals(left.getId()) || treatmentId.equals(right.getId()) ) {
                pairwiseEntries.add(entry);
            }
        }

        List entries = new ArrayList();
        entries.addAll(pairwiseEntries);
        this.treatments = Arrays.asList(left, right);
        this.entries = entries;
    }
}
