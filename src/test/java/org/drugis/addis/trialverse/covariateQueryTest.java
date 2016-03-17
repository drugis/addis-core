package org.drugis.addis.trialverse;

import org.apache.commons.io.IOUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.trialverse.model.emun.CovariateOptionType;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by daan on 16-3-16.
 */
public class covariateQueryTest {

  @Test
  public void meanValueTest() throws IOException {
    Covariate age = new Covariate(1, "age", "", "ageEntity", CovariateOptionType.POPULATION_CHARACTERISTIC);

    ResultSet results = doQueryForResult("/trialverseModel/studyWithAgeData.ttl", age);
    QuerySolution querySolution = results.nextSolution();
    double value = querySolution.getLiteral("value").getDouble();
    assertEquals(39.6, value, 0.0000001);
  }

  @Test
  public void rationalValueTest() throws IOException {
    Covariate age = new Covariate(1, "age", "", "anxietyEntity", CovariateOptionType.POPULATION_CHARACTERISTIC);

    ResultSet results = doQueryForResult("/trialverseModel/studyWithInitialAnxietyData.ttl", age);
    QuerySolution querySolution = results.nextSolution();
    double value = querySolution.getLiteral("value").getDouble();
    assertEquals(0.1, value, 0.0000001);
  }

  @Test
  public void testBigStudy() throws IOException {
    Covariate age = new Covariate(1, "age", "", "efdec39b-8e43-42dc-a927-4259ff51cdaf", CovariateOptionType.POPULATION_CHARACTERISTIC);

    ResultSet results = doQueryForResult("/trialverseModel/realstudyAberg.ttl", age);
  List<QuerySolution> solutions = new ArrayList<>();
    while (results.hasNext()){
    solutions.add(results.nextSolution());
  }
    System.out.println("solution lenghth = "  + solutions.size());

    double value = solutions.get(0).getLiteral("value").getDouble();
    assertEquals(43, value, 0.0000001);

  }

  @Test
  public void testNoPop() throws IOException {
    Covariate age = new Covariate(1, "age", "", "efdec39b-8e43-42dc-a927-4259ff51cdaf", CovariateOptionType.POPULATION_CHARACTERISTIC);

    ResultSet results = doQueryForResult("/trialverseModel/no_population_group.ttl", age);
    List<QuerySolution> solutions = new ArrayList<>();
    while (results.hasNext()){
      solutions.add(results.nextSolution());
    }
    System.out.println("solution lenghth = "  + solutions.size());

    Object value = solutions.get(0).getLiteral("value");
    assertEquals(null, value);
    //assertEquals(43, value, 0.0000001);

  }

  private ResultSet doQueryForResult(String pathToMockStudy, Covariate covariate) throws IOException {
    InputStream datasetsModelStream = new ClassPathResource(pathToMockStudy).getInputStream();
    Model model = ModelFactory.createDefaultModel();
    model.read(datasetsModelStream, null, "TTL");
    Dataset dataset = DatasetFactory.createMem();
    dataset.addNamedModel("3e6794f1-c95c-486e-ac8d-55e9259d0f4a", model);

    InputStream inputStream = new ClassPathResource("/sparql/populationCharacteristicCovariateData.sparql").getInputStream();
    String query = IOUtils.toString(inputStream, "UTF-8");
    query = query.replace("$populationCharacteristicUuid", covariate.getDefinitionKey());
    QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
    return queryExecution.execSelect();
  }

}
