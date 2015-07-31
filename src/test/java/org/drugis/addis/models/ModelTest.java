package org.drugis.addis.models;

import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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
    networkModel = new Model.ModelBuilder()
            .analysisId(analysisId)
            .title(title)
            .linearModel(linearModel)
            .modelType(networkType)
            .build();
    pairwiseModel = new Model.ModelBuilder()
            .analysisId(analysisId)
            .title(title)
            .linearModel(linearModel)
            .modelType(pairwiseType)
            .from(new Model.DetailNode(-1, "treatment1"))
            .to(new Model.DetailNode(-2, "treatment2"))
            .build();
  }

  @Test
  public void testGetNetworkModelType() {
    String type = networkModel.getModelTypeTypeAsString();
    assertEquals(Model.NETWORK_MODEL_TYPE, type);
  }

  @Test
  public void testGetPairwiseModelType() {
    String type = pairwiseModel.getModelTypeTypeAsString();
    assertEquals(Model.PAIRWISE_MODEL_TYPE, type);
  }

  @Test
  public void testGetPairWiseDetailsFromNetworkModel() {
    Pair details = networkModel.getPairwiseDetails();
    assertNull(details);
  }

  @Test
  public void testGetPairWiseDetailsFromPairWiseModel() {
    Pair<Model.DetailNode, Model.DetailNode> details = pairwiseModel.getPairwiseDetails();
    assertNotNull(details);
    assertEquals("treatment1", details.getLeft().getName());
    assertEquals("treatment2", details.getRight().getName());
  }
}
