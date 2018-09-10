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

/**
 * Created by connor on 15-4-16.
 */
public class TripleStoreServiceQueryTest {

  @Inject
  TriplestoreService triplestoreService;

  @Test
  public void testGetSpecificGraphDataQuery() throws Exception {
    Model model1 = ModelFactory.createDefaultModel();
    InputStream mockStudyGraph1 = new ClassPathResource("trialverseModel/abergCompleteStudy.ttl").getInputStream();
    model1.read(mockStudyGraph1, null, "TTL");
    Dataset dataset = DatasetFactory.createGeneral();
    URI studyUri = URI.create("http://trials.drugis.org/graphs/studyUuid");
    dataset.addNamedModel(String.valueOf(studyUri), model1);

    String template = TriplestoreServiceImpl.TRIAL_DATA;
    String queryStr = StringUtils.replace(template, "$graphSelector", "<" + studyUri.toString() + ">");

    URI madrsOutcome = URI.create("http://trials.drugis.org/concepts/923bb7f2-0b90-4e2f-92d0-8d3e3a487cc0");
    URI cgiOutcome = URI.create("http://trials.drugis.org/concepts/16a99c68-8e05-4625-ad59-1ce2acc5e574");
    Set<URI> outcomeUris = new HashSet<>(Arrays.asList(madrsOutcome, cgiOutcome));
    queryStr = StringUtils.replace(queryStr, "$outcomeUnionString", TriplestoreServiceImpl.buildOutcomeUnionString(outcomeUris));

    URI paroxetine = URI.create("http://trials.drugis.org/concepts/c017057f-bfc9-4e1a-8ece-3fb5671f3746");
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
    InputStream mockStudyGraph1 = new ClassPathResource("trialverseModel/abergCompleteStudy.ttl").getInputStream();
    model1.read(mockStudyGraph1, null, "TTL");
    Dataset dataset = DatasetFactory.createGeneral();
    URI studyUri = URI.create("http://trials.drugis.org/graphs/studyUuid");
    dataset.addNamedModel(String.valueOf(studyUri), model1);

    String template = TriplestoreServiceImpl.TRIAL_DATA;
    String queryStr = StringUtils.replace(template, "$graphSelector", "?graph");

    URI cgiOutcome = URI.create("http://trials.drugis.org/concepts/16a99c68-8e05-4625-ad59-1ce2acc5e574");
    Set<URI> outcomeUris = new HashSet<>(Collections.singletonList(cgiOutcome));
    queryStr = StringUtils.replace(queryStr, "$outcomeUnionString", TriplestoreServiceImpl.buildOutcomeUnionString(outcomeUris));

    URI paroxetine = URI.create("http://trials.drugis.org/concepts/c017057f-bfc9-4e1a-8ece-3fb5671f3746");
    Set<URI> interventionUris = new HashSet<>(Collections.singletonList(paroxetine));
    String interventionUn = TriplestoreServiceImpl.buildInterventionUnionString(interventionUris);
    queryStr = StringUtils.replace(queryStr, "$interventionUnionString", interventionUn);

    Query query = QueryFactory.create(queryStr);
    QueryExecution queryExecution = QueryExecutionFactory.create(query, dataset);
    ResultSet resultSet = queryExecution.execSelect();

    List<Object> results = new ArrayList<>();
    resultSet.forEachRemaining(results::add);

    assertEquals(2, results.size());
    assertTrue("check that the result contains a titrated dose", results.toString().contains("Titrated"));
  }



}
