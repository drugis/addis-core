package org.drugis.addis.trialverse;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TrialDataQueryTest {

  @Inject
  TriplestoreService triplestoreService;

  @Test
  public void testGetSpecificGraphDataQuery() throws Exception {
    Model model1 = ModelFactory.createDefaultModel();
    InputStream mockStudyGraph1 = new ClassPathResource("trialverseModel/abergWithContrastCompleteStudy.ttl").getInputStream();
    model1.read(mockStudyGraph1, null, "TTL");
    Dataset dataset = DatasetFactory.createGeneral();
    URI studyUri = URI.create("http://trials.drugis.org/graphs/studyUuid");
    dataset.addNamedModel(String.valueOf(studyUri), model1);

    String template = TriplestoreServiceImpl.TRIAL_DATA;
    String queryStr = StringUtils.replace(template, "$graphSelector", "<" + studyUri.toString() + ">");

    URI madrsOutcome = URI.create("http://trials.drugis.org/concepts/9e3462ef-45ae-4d08-8808-00c97dd150e3");
    URI cgiOutcome = URI.create("http://trials.drugis.org/concepts/0e2d3c3d-b484-4f98-bc99-1c416065b745");
    Set<URI> outcomeUris = new HashSet<>(Arrays.asList(madrsOutcome, cgiOutcome));
    queryStr = StringUtils.replace(queryStr, "$outcomeUnionString", TriplestoreServiceImpl.buildOutcomeUnionString(outcomeUris));

    URI paroxetine = URI.create("http://trials.drugis.org/concepts/bde33c0a-384a-4976-9b18-1d0e5df263ce");
    Set<URI> interventionUris = Collections.singleton(paroxetine);
    String interventionUn = TriplestoreServiceImpl.buildInterventionUnionString(interventionUris);
    queryStr = StringUtils.replace(queryStr, "$interventionUnionString", interventionUn);

    Query query = QueryFactory.create(queryStr);
    QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
    ResultSet resultSet = queryExecution.execSelect();

    List<Object> results = new ArrayList<>();
    resultSet.forEachRemaining(results::add);

    assertEquals(4, results.size());
    assertTrue("check that the result contains a titrated dose", results.toString().contains("Titrated"));
  }

  @Test
  public void testAllGraphsDataQuery() throws Exception {
    Model model1 = ModelFactory.createDefaultModel();
    InputStream mockStudyGraph1 = new ClassPathResource("trialverseModel/abergWithContrastCompleteStudy.ttl").getInputStream();
    model1.read(mockStudyGraph1, null, "TTL");
    Dataset dataset = DatasetFactory.createGeneral();
    URI studyUri = URI.create("http://trials.drugis.org/graphs/studyUuid");
    dataset.addNamedModel(String.valueOf(studyUri), model1);

    String template = TriplestoreServiceImpl.TRIAL_DATA;
    String queryStr = StringUtils.replace(template, "$graphSelector", "?graph");

    URI cgiOutcome = URI.create("http://trials.drugis.org/concepts/0e2d3c3d-b484-4f98-bc99-1c416065b745");
    URI contrastOutcome = URI.create("http://trials.drugis.org/concepts/92c20dff-bb75-495d-bc5b-16edb26db6b3");

    Set<URI> outcomeUris = new HashSet<>(Arrays.asList(cgiOutcome, contrastOutcome));
    queryStr = StringUtils.replace(queryStr, "$outcomeUnionString", TriplestoreServiceImpl.buildOutcomeUnionString(outcomeUris));

    URI paroxetine = URI.create("http://trials.drugis.org/concepts/bde33c0a-384a-4976-9b18-1d0e5df263ce");
    URI sertraline = URI.create("http://trials.drugis.org/concepts/82cba011-6fb9-423d-a7a0-6b4d15db162c");
    Set<URI> interventionUris = new HashSet<>(Arrays.asList(paroxetine, sertraline));
    String interventionUn = TriplestoreServiceImpl.buildInterventionUnionString(interventionUris);
    queryStr = StringUtils.replace(queryStr, "$interventionUnionString", interventionUn);

    Query query = QueryFactory.create(queryStr);
    QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
    ResultSet resultSet = queryExecution.execSelect();

    List<Object> results = new ArrayList<>();
    resultSet.forEachRemaining(results::add);
    results.forEach(a -> System.out.println(a.toString()));
    assertEquals(4, results.size());
    assertTrue("check that the result contains a titrated dose", results.toString().contains("Titrated"));
    assertTrue("check that the result contains a reference standard error", results.toString().contains("referenceStandardError"));
    assertTrue("check that the result contains a reference arm", results.toString().contains("referenceArm"));
    assertTrue("check that the result contains a confidence interval width", results.toString().contains("confidenceIntervalWidth"));
    assertTrue("check that the result contains a confidence interval upper bound", results.toString().contains("confidenceIntervalUpperBound"));
  }

  @Test
  public void testFavaGraphDataQuery() throws Exception {
    Model model1 = ModelFactory.createDefaultModel();
    InputStream mockStudyGraph1 = new ClassPathResource("trialverseModel/favaWithContrastCompleteStudy.ttl").getInputStream();
    model1.read(mockStudyGraph1, null, "TTL");
    Dataset dataset = DatasetFactory.createGeneral();
    URI studyUri = URI.create("http://trials.drugis.org/graphs/studyUuid");
    dataset.addNamedModel(String.valueOf(studyUri), model1);

    String template = TriplestoreServiceImpl.TRIAL_DATA;
    String queryStr = StringUtils.replace(template, "$graphSelector", "?graph");

    URI cgiOutcome = URI.create("http://trials.drugis.org/concepts/0e2d3c3d-b484-4f98-bc99-1c416065b745");
    URI contrastOutcome = URI.create("http://trials.drugis.org/concepts/92c20dff-bb75-495d-bc5b-16edb26db6b3");

    Set<URI> outcomeUris = new HashSet<>(Arrays.asList(cgiOutcome, contrastOutcome));
    queryStr = StringUtils.replace(queryStr, "$outcomeUnionString", TriplestoreServiceImpl.buildOutcomeUnionString(outcomeUris));

    URI paroxetine = URI.create("http://trials.drugis.org/concepts/bde33c0a-384a-4976-9b18-1d0e5df263ce");
    URI sertraline = URI.create("http://trials.drugis.org/concepts/82cba011-6fb9-423d-a7a0-6b4d15db162c");
    Set<URI> interventionUris = new HashSet<>(Arrays.asList(paroxetine, sertraline));
    String interventionUn = TriplestoreServiceImpl.buildInterventionUnionString(interventionUris);
    queryStr = StringUtils.replace(queryStr, "$interventionUnionString", interventionUn);

    Query query = QueryFactory.create(queryStr);
    QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
    ResultSet resultSet = queryExecution.execSelect();

    List<Object> results = new ArrayList<>();
    resultSet.forEachRemaining(results::add);
    results.forEach(a -> System.out.println(a.toString()));
    assertTrue("check that the result contains a reference arm", results.toString().contains("referenceArm"));
    assertEquals(3, results.size());
    assertTrue("check that the result contains a titrated dose", results.toString().contains("Titrated"));
    assertTrue("check that the result contains a reference standard error", results.toString().contains("referenceStandardError"));
    assertTrue("check that the result contains a standard error", results.toString().contains("stdErr"));
    assertTrue("check that the result contains an arm or contrast property", results.toString().contains("armOrContrast"));
  }
}
