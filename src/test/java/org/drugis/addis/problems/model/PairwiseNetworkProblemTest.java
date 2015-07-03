package org.drugis.addis.problems.model;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by connor on 7/3/15.
 */
public class PairwiseNetworkProblemTest {

    @Test
    public void testCreatePairwiseNetworkProblem() {

        Long sampleSize = 33L;
        Double mean = 2.2;
        Double stdDev = 0.2;

        String study1 = "study1";
        String study2 = "study2";
        String study3 = "study3";
        String study4 = "study4";

        Integer treatment1 = 101;
        Integer treatment2 = 102;
        Integer treatment3 = 103;

        AbstractNetworkMetaAnalysisProblemEntry entry1 = new ContinuousNetworkMetaAnalysisProblemEntry(study1, treatment1, sampleSize, mean, stdDev);
        AbstractNetworkMetaAnalysisProblemEntry entry2 = new ContinuousNetworkMetaAnalysisProblemEntry(study2, treatment2, sampleSize, mean, stdDev);
        AbstractNetworkMetaAnalysisProblemEntry entry3 = new ContinuousNetworkMetaAnalysisProblemEntry(study3, treatment3, sampleSize, mean, stdDev);
        AbstractNetworkMetaAnalysisProblemEntry entry4 = new ContinuousNetworkMetaAnalysisProblemEntry(study4, treatment1, sampleSize, mean, stdDev);
        List<AbstractNetworkMetaAnalysisProblemEntry> entries = Arrays.asList(entry1, entry2, entry3, entry4);

        TreatmentEntry treatmentEntry1 = new TreatmentEntry(treatment1, "treatment1");
        TreatmentEntry treatmentEntry2 = new TreatmentEntry(treatment2, "treatment2");
        TreatmentEntry treatmentEntry3 = new TreatmentEntry(treatment3, "treatment3");
        List<TreatmentEntry> treatments = Arrays.asList(treatmentEntry1,treatmentEntry2, treatmentEntry3);

        NetworkMetaAnalysisProblem problem = new NetworkMetaAnalysisProblem(entries, treatments);

        Pair details = Pair.of("treatment1", "treatment3");
        PairwiseNetworkProblem pairwiseNetworkProblem = new PairwiseNetworkProblem(problem, details);

        assertNotNull(pairwiseNetworkProblem);
        assertEquals(2, pairwiseNetworkProblem.getTreatments().size());
        assertTrue(pairwiseNetworkProblem.getTreatments().contains(treatmentEntry1));
        assertTrue(pairwiseNetworkProblem.getTreatments().contains(treatmentEntry3));
        assertEquals(3, pairwiseNetworkProblem.getEntries().size());
        assertTrue(pairwiseNetworkProblem.getEntries().contains(entry1));
        assertTrue(pairwiseNetworkProblem.getEntries().contains(entry3));
        assertTrue(pairwiseNetworkProblem.getEntries().contains(entry4));
    }
 }
