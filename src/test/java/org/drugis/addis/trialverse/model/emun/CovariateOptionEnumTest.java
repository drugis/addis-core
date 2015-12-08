package org.drugis.addis.trialverse.model.emun;


import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by connor on 12/8/15.
 */
public class CovariateOptionEnumTest {

  @Test
  public void AllocationRondomizedTest() throws IOException {

    CovariateOption covariate = CovariateOption.ALLOCATION_RANDOMIZED;

    ResultSet results = doQueryForResult("/trialverseModel/studyWithAllocationRondomized.ttl", covariate);
    QuerySolution querySolution = results.nextSolution();
    Literal value = querySolution.getLiteral("value");
    assertEquals((Double) 1D, (Double) value.getDouble());

    results = doQueryForResult("/trialverseModel/studyWithOutAllocationRondomized.ttl", covariate);
    querySolution = results.nextSolution();
    value = querySolution.getLiteral("value");
    assertEquals((Double) 0D, (Double) value.getDouble());

    results = doQueryForResult("/trialverseModel/studyWithNoInfo.ttl", covariate);
    querySolution = results.nextSolution();
    assertNull(querySolution.getLiteral("value"));
  }

  @Test
  public void AtLeastSingleBlindTest() throws IOException {

    CovariateOption covariate = CovariateOption.BLINDING_AT_LEAST_SINGLE_BLIND;

    ResultSet results = doQueryForResult("/trialverseModel/studyWithSingleBlind.ttl", covariate);
    QuerySolution querySolution = results.nextSolution();
    Literal value = querySolution.getLiteral("value");
    assertEquals((Double) 1D, (Double) value.getDouble());

    results = doQueryForResult("/trialverseModel/studyWithTripleBlind.ttl", covariate);
    querySolution = results.nextSolution();
    value = querySolution.getLiteral("value");
    assertEquals((Double) 1D, (Double) value.getDouble());

    results = doQueryForResult("/trialverseModel/studyWithOpenBlind.ttl", covariate);
    querySolution = results.nextSolution();
    value = querySolution.getLiteral("value");
    assertEquals((Double) 0D, (Double) value.getDouble());

    results = doQueryForResult("/trialverseModel/studyWithNoInfo.ttl", covariate);
    querySolution = results.nextSolution();
    assertNull(querySolution.getLiteral("value"));
  }

  @Test
  public void AtLeastDoubleBlindTest() throws IOException {

    CovariateOption covariate = CovariateOption.BLINDING_AT_LEAST_DOUBLE_BLIND;

    ResultSet results = doQueryForResult("/trialverseModel/studyWithSingleBlind.ttl", covariate);
    QuerySolution querySolution = results.nextSolution();
    Literal value = querySolution.getLiteral("value");
    assertEquals((Double) 0D, (Double) value.getDouble());

    results = doQueryForResult("/trialverseModel/studyWithTripleBlind.ttl", covariate);
    querySolution = results.nextSolution();
    value = querySolution.getLiteral("value");
    assertEquals((Double) 1D, (Double) value.getDouble());

    results = doQueryForResult("/trialverseModel/studyWithOpenBlind.ttl", covariate);
    querySolution = results.nextSolution();
    value = querySolution.getLiteral("value");
    assertEquals((Double) 0D, (Double) value.getDouble());

    results = doQueryForResult("/trialverseModel/studyWithNoInfo.ttl", covariate);
    querySolution = results.nextSolution();
    assertNull(querySolution.getLiteral("value"));

  }

  @Test
  public void IsMultiCenterTest() throws IOException {

    CovariateOption covariate = CovariateOption.MULTI_CENTER_STUDY;

    ResultSet results = doQueryForResult("/trialverseModel/studyWithSingleCenter.ttl", covariate);
    QuerySolution querySolution = results.nextSolution();
    Literal value = querySolution.getLiteral("value");
    assertEquals((Double) 0D, (Double) value.getDouble());


    results = doQueryForResult("/trialverseModel/studyWithMultipleCenters.ttl", covariate);
    querySolution = results.nextSolution();
    value = querySolution.getLiteral("value");
    assertEquals((Double) 1D, (Double) value.getDouble());

    results = doQueryForResult("/trialverseModel/studyWithNoInfo.ttl", covariate);
    querySolution = results.nextSolution();
    assertNull(querySolution.getLiteral("value"));

  }

  private ResultSet doQueryForResult(String pathToMockStudy, CovariateOption covariate) throws IOException {
    InputStream datasetsModelStream = new ClassPathResource(pathToMockStudy).getInputStream();
    Model model = ModelFactory.createDefaultModel();
    model.read(datasetsModelStream, null, "TTL");
    Dataset dataset = DatasetFactory.createMem();
    dataset.addNamedModel("3e6794f1-c95c-486e-ac8d-55e9259d0f4a", model);

    String query = covariate.getQuery();
    QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
    return queryExecution.execSelect();
  }
}
