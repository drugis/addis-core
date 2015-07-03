package org.drugis.addis.models;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by connor on 7/3/15.
 */
public class ModelTest {

    private Integer analysisId = 1;
    private String title = "title";
    private String linearModel = "fixed";
    private String networkType = "{'type': 'network'}";
    private String pairwiseType = "{'type': 'pairwise', 'details': {'to': 'study1', 'from': 'study2'}}";
    private Model networkModel = new Model(analysisId, title, linearModel, networkType);
    private Model pairwiseModel = new Model(analysisId, title, linearModel, pairwiseType);

    @Test
    public void testGetNetworkModelType() {
        String type = networkModel.getModelType();
        assertEquals(Model.NETWORK_MODEL_TYPE, type);
    }

    @Test
    public void testGetPairwiseModelType() {
        String type = pairwiseModel.getModelType();
        assertEquals(Model.PAIRWISE_MODEL_TYPE, type);
    }

    @Test
    public void testGetPairWiseDetailsFromNetworkModel() {
        Pair details = networkModel.getPairwiseDetails();
        assertNull(details);
    }

    @Test
    public void testGetPairWiseDetailsFromPairWiseModel() {
        Pair<String, String> details = pairwiseModel.getPairwiseDetails();
        assertNotNull(details);
        assertEquals(details.getLeft(), "study1");
        assertEquals(details.getRight(), "study2");
    }
}
