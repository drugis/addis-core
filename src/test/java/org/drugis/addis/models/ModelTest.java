package org.drugis.addis.models;

import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;
import org.junit.Before;
import org.junit.BeforeClass;
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
    private String networkType = "network";
    private String pairwiseType = "pairwise";
    private Model networkModel;
    private Model pairwiseModel;

    @Before
    public void setup() throws InvalidModelTypeException {
        networkModel = new Model(analysisId, title, linearModel, networkType, null, null);
        pairwiseModel = new Model(analysisId, title, linearModel, pairwiseType, "treatment1", "treatment2");
    }

    @Test
    public void testGetNetworkModelType() {
        String type = networkModel.getModelTypeAsTypeString();
        assertEquals(Model.NETWORK_MODEL_TYPE, type);
    }

    @Test
    public void testGetPairwiseModelType() {
        String type = pairwiseModel.getModelTypeAsTypeString();
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
        assertEquals(details.getLeft(), "treatment1");
        assertEquals(details.getRight(), "treatment2");
    }
}
