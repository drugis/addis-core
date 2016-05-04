package org.drugis.addis.models;

import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

;

/**
 * Created by connor on 7/3/15.
 */
public class ModelTest {

  private Integer analysisId = 1;
  private String title = "title";
  private Model networkModel;
  private Model stdDevHeterogeneityPriorNetworkModel;
  private Model varianceHeterogeneityPriorNetworkModel;
  private Model precisionHeterogeneityPriorNetworkModel;
  private Model pairwiseModel;
  private Model nodeSplitModel;
  private Double lower = 0.5;
  private Double upper = 1.0;
  private Double mean = 1.1;
  private Double stdDev = 0.1;
  private Double rate = 2.2;
  private Double shape = 3.3;

  @Before
  public void setup() throws InvalidModelException  {
    networkModel = new Model.ModelBuilder(analysisId, title)
            .link(Model.LINK_IDENTITY)
            .linearModel(Model.LINEAR_MODEL_FIXED)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .build();
    stdDevHeterogeneityPriorNetworkModel = new Model.ModelBuilder(analysisId, title)
            .linearModel(Model.LINEAR_MODEL_FIXED)
            .link(Model.LINK_IDENTITY)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .heterogeneityPriorType(Model.STD_DEV_HETEROGENEITY_PRIOR_TYPE)
            .lower(lower)
            .upper(upper)
            .build();
    varianceHeterogeneityPriorNetworkModel = new Model.ModelBuilder(analysisId, title)
            .linearModel(Model.LINEAR_MODEL_FIXED)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .link(Model.LINK_IDENTITY)
            .heterogeneityPriorType(Model.VARIANCE_HETEROGENEITY_PRIOR_TYPE)
            .mean(mean)
            .stdDev(stdDev)
            .build();
    precisionHeterogeneityPriorNetworkModel = new Model.ModelBuilder(analysisId, title)
            .linearModel(Model.LINEAR_MODEL_FIXED)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .link(Model.LINK_IDENTITY)
            .heterogeneityPriorType(Model.PRECISION_HETEROGENEITY_PRIOR_TYPE)
            .rate(rate)
            .shape(shape)
            .build();
    pairwiseModel = new Model.ModelBuilder(analysisId, title)
            .linearModel(Model.LINEAR_MODEL_FIXED)
            .modelType(Model.PAIRWISE_MODEL_TYPE)
            .link(Model.LINK_IDENTITY)
            .from(new Model.DetailNode(-1, "treatment1"))
            .to(new Model.DetailNode(-2, "treatment2"))
            .build();
    nodeSplitModel = new Model.ModelBuilder(analysisId, title)
            .linearModel(Model.LINEAR_MODEL_FIXED)
            .link(Model.LINK_IDENTITY)
            .modelType(Model.NODE_SPLITTING_MODEL_TYPE)
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
  public void testGetNodeSplitModelType() {
    String type = nodeSplitModel.getModelTypeTypeAsString();
    assertEquals(Model.NODE_SPLITTING_MODEL_TYPE, type);
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

  @Test
  public void testGetPairWiseDetailsFromNodeSplitModel() {
    Pair<Model.DetailNode, Model.DetailNode> details = nodeSplitModel.getPairwiseDetails();
    assertNotNull(details);
    assertEquals("treatment1", details.getLeft().getName());
    assertEquals("treatment2", details.getRight().getName());
  }

  @Test
  public void testGetAutomaticHeterogeneityPrior() {
    assertNull(networkModel.getHeterogeneityPrior());
  }

  @Test
  public void testeGetNonAutomaticHeterogeneityPriorType() {
    assertEquals(Model.PRECISION_HETEROGENEITY_PRIOR_TYPE, precisionHeterogeneityPriorNetworkModel.getHeterogeneityPrior().getType());
  }

  @Test
  public void getStdDevValuesFromNetwork() {
    Model.HeterogeneityStdDevValues values = (Model.HeterogeneityStdDevValues) stdDevHeterogeneityPriorNetworkModel.getHeterogeneityPrior().getValues();
    assertEquals(lower, values.getLower());
    assertEquals(upper, values.getUpper());
  }

  @Test
  public void getVarianceValuesFromNetwork() {
    Model.HeterogeneityVarianceValues values = (Model.HeterogeneityVarianceValues) varianceHeterogeneityPriorNetworkModel.getHeterogeneityPrior().getValues();
    assertEquals(mean, values.getMean());
    assertEquals(stdDev, values.getStdDev());
  }

  @Test
  public void getPrecisionValuesFromNetwork() {
    Model.HeterogeneityPrecisionValues values = (Model.HeterogeneityPrecisionValues) precisionHeterogeneityPriorNetworkModel.getHeterogeneityPrior().getValues();
    assertEquals(rate, values.getRate());
    assertEquals(shape, values.getShape());
  }
}
