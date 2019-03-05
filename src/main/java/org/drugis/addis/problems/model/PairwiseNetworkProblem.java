package org.drugis.addis.problems.model;

import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.models.Model;
import org.drugis.addis.problems.model.problemEntry.AbstractProblemEntry;

import java.util.*;

/**
 * Created by connor on 7/3/15.
 */
public class PairwiseNetworkProblem extends NetworkMetaAnalysisProblem {
    public PairwiseNetworkProblem(NetworkMetaAnalysisProblem problem, Pair<Model.DetailNode, Model.DetailNode> pairwiseDetails) {
        TreatmentEntry left = null;
        TreatmentEntry right = null;
        for(TreatmentEntry treatment : problem.getTreatments()){
            if (treatment.getName().equals(pairwiseDetails.getLeft().getName())) {
                left = treatment;
            }
            if (treatment.getName().equals(pairwiseDetails.getRight().getName())) {
                right = treatment;
            }
        }

        Set<AbstractProblemEntry> pairwiseEntries = new HashSet<>();
        for (AbstractProblemEntry entry : problem.getEntries()) {
            Integer treatmentId = entry.getTreatment();
            assert left != null;
            assert right != null;
            if(treatmentId.equals(left.getId()) || treatmentId.equals(right.getId()) ) {
                pairwiseEntries.add(entry);
            }
        }

        List<AbstractProblemEntry> entries = new ArrayList<>();
        entries.addAll(pairwiseEntries);
        this.treatments = Arrays.asList(left, right);
        this.entries = entries;
    }
}
