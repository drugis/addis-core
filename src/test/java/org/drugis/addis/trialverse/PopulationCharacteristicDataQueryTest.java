package org.drugis.addis.trialverse;

import org.apache.commons.io.IOUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
public class PopulationCharacteristicDataQueryTest {

  @Test
  public void meanValueTest() throws IOException {
    String ageEntity = "ageEntity";
    ResultSet results = doQueryForResult("/trialverseModel/studyWithAgeData.ttl", ageEntity);
    QuerySolution querySolution = results.nextSolution();
    double value = querySolution.getLiteral("value").getDouble();
    assertEquals(39.6, value, 0.0000001);
  }

  @Test
  public void rationalValueTest() throws IOException {
    String anxietyEntity = "anxietyEntity";
    ResultSet results = doQueryForResult("/trialverseModel/studyWithInitialAnxietyData.ttl", anxietyEntity);
    QuerySolution querySolution = results.nextSolution();
    double value = querySolution.getLiteral("value").getDouble();
    assertEquals(0.1, value, 0.0000001);
  }

  @Test
  public void testBigStudy() throws IOException {
    String populationCharacteristicUuid = "efdec39b-8e43-42dc-a927-4259ff51cdaf";
    ResultSet results = doQueryForResult("/trialverseModel/abergCompleteStudy.ttl", populationCharacteristicUuid);
    List<QuerySolution> studyResults = resultsToList(results);
    assertEquals(1, studyResults.size());
    double value = studyResults.get(0).getLiteral("value").getDouble();
    assertEquals(43, value, 0.0000001);
  }

  @Test
  public void testStudyWithNoOverallPopulationGetsNullValue() throws IOException {
    String populationCharacteristicUuid = "efdec39b-8e43-42dc-a927-4259ff51cdaf";
    ResultSet results = doQueryForResult("/trialverseModel/studyWithoutOverallPopulation.ttl", populationCharacteristicUuid);
    List<QuerySolution> studyResults = resultsToList(results);
    assertEquals(1, studyResults.size());
    Object value = studyResults.get(0).getLiteral("value");
    assertEquals(null, value);
  }

  private List<QuerySolution> resultsToList(ResultSet results) {
    List<QuerySolution> solutions = new ArrayList<>();
    while (results.hasNext()) {
      solutions.add(results.nextSolution());
    }
    return solutions;
  }

  private ResultSet doQueryForResult(String pathToMockStudy, String populationCharacteristicUuid) throws IOException {
    InputStream datasetsModelStream = new ClassPathResource(pathToMockStudy).getInputStream();
    Model model = ModelFactory.createDefaultModel();
    model.read(datasetsModelStream, null, "TTL");
    Dataset dataset = DatasetFactory.createGeneral();
    dataset.addNamedModel("3e6794f1-c95c-486e-ac8d-55e9259d0f4a", model);

    InputStream inputStream = new ClassPathResource("/sparql/populationCharacteristicCovariateData.sparql").getInputStream();
    String query = IOUtils.toString(inputStream, "UTF-8");
    query = query.replace("$populationCharacteristicUuid", populationCharacteristicUuid);
    QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
    return queryExecution.execSelect();
  }

}
