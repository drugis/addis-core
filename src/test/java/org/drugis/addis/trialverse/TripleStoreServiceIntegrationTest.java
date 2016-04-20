package org.drugis.addis.trialverse;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import javax.inject.Inject;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by connor on 15-4-16.
 */
public class TripleStoreServiceIntegrationTest {

  @Inject
  TriplestoreService triplestoreService;

  @Test
  public void testGetSpecificGraphDataQuery() throws Exception {
    Model model1 = ModelFactory.createDefaultModel();
    InputStream mockStudyGraph1 = new ClassPathResource("trialverseModel/abergCompleteStudy.ttl").getInputStream();
    model1.read(mockStudyGraph1, null, "TTL");
    Dataset dataset = DatasetFactory.createMem();
    URI studyUri = URI.create("http://trials.drugis.org/graphs/studyUid");
    dataset.addNamedModel(String.valueOf(studyUri), model1);

    String template = TriplestoreServiceImpl.TRIAL_DATA;
    String queryStr = StringUtils.replace(template, "$graphSelector", "<" + studyUri.toString() + ">");

    String ageOutcome = "16a99c68-8e05-4625-ad59-1ce2acc5e574";
    List<String> outcomeUids = Arrays.asList(ageOutcome);
    queryStr = StringUtils.replace(queryStr, "$outcomeUnionString", buildOutcomeUnionString(outcomeUids));

    URI paroxetine = URI.create("http://trials.drugis.org/concepts/c017057f-bfc9-4e1a-8ece-3fb5671f3746");
    List<URI> interventionUris = Arrays.asList(paroxetine);
    String interventionUn = buildInterventionUnionString(interventionUris);
    queryStr = StringUtils.replace(queryStr, "$interventionUnionString", interventionUn);

    Query query = QueryFactory.create(queryStr);
    QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
    ResultSet resultSet = queryExecution.execSelect();

    List<Object> results = new ArrayList<>();
    resultSet.forEachRemaining(results::add);

    assertEquals(2, results.size());
    assertTrue("check that the result contains a titrated dose", results.toString().contains("Titrated"));
  }

  @Test
  public void testAllGraphsDataQuery() throws Exception {
    Model model1 = ModelFactory.createDefaultModel();
    InputStream mockStudyGraph1 = new ClassPathResource("trialverseModel/abergCompleteStudy.ttl").getInputStream();
    model1.read(mockStudyGraph1, null, "TTL");
    Dataset dataset = DatasetFactory.createMem();
    URI studyUri = URI.create("http://trials.drugis.org/graphs/studyUid");
    dataset.addNamedModel(String.valueOf(studyUri), model1);

    String template = TriplestoreServiceImpl.TRIAL_DATA;
    String queryStr = StringUtils.replace(template, "$graphSelector", "?graph");

    String ageOutcome = "16a99c68-8e05-4625-ad59-1ce2acc5e574";
    List<String> outcomeUids = Arrays.asList(ageOutcome);
    queryStr = StringUtils.replace(queryStr, "$outcomeUnionString", buildOutcomeUnionString(outcomeUids));

    URI paroxetine = URI.create("http://trials.drugis.org/concepts/c017057f-bfc9-4e1a-8ece-3fb5671f3746");
    List<URI> interventionUris = Arrays.asList(paroxetine);
    String interventionUn = buildInterventionUnionString(interventionUris);
    queryStr = StringUtils.replace(queryStr, "$interventionUnionString", interventionUn);

    Query query = QueryFactory.create(queryStr);
    QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
    ResultSet resultSet = queryExecution.execSelect();

    List<Object> results = new ArrayList<>();
    resultSet.forEachRemaining(results::add);

    assertEquals(2, results.size());
    assertTrue("check that the result contains a titrated dose", results.toString().contains("Titrated"));
  }

  private String buildInterventionUnionString(List<URI> interventionUris) {
    String result = "";
    for (URI interventionUri : interventionUris) {
      result += " { ?interventionInstance owl:sameAs <" + interventionUri + "> } UNION \n";
    }

    return result.substring(0, result.lastIndexOf("UNION"));
  }

  private String buildOutcomeUnionString(List<String> outcomeUids) {
    String result = "";
    for (String outcomeUid : outcomeUids) {
      result += " { ?outcomeInstance ontology:of_variable [ owl:sameAs concept:" + outcomeUid + " ] } UNION \n";
    }
    return result.substring(0, result.lastIndexOf("UNION"));
  }
}
