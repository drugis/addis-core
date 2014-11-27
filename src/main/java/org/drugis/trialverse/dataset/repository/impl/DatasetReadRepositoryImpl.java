package org.drugis.trialverse.dataset.repository.impl;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.rdf.model.Model;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.Principal;

/**
 * Created by daan on 7-11-14.
 */
@Repository
public class DatasetReadRepositoryImpl implements DatasetReadRepository {

  private final static Logger logger = LoggerFactory.getLogger(DatasetReadRepositoryImpl.class);
  private final static String SINGLE_STUDY_MEASUREMENTS = loadResource("queryDatasetsConstruct.sparql");
  private final static String STUDIES_WITH_DETAILS = loadResource("constructStudiesWithDetails.sparql");
  private final static String IS_OWNER_QUERY = loadResource("askIsOwner.sparql");
  private final static String CONTAINS_STUDY_WITH_SHORTNAME = loadResource("askContainsStudyWithLabel.sparql");

  public final static String QUERY_AFFIX = "/current/query";

  @Inject
  private HttpClientFactory httpClientFactory;

  @Inject
  private WebConstants webConstants;

  @Inject
  private JenaFactory jenaFactory;

  private static String loadResource(String filename) {
    try {
      Resource myData = new ClassPathResource(filename);
      InputStream stream = myData.getInputStream();
      return IOUtils.toString(stream, "UTF-8");
    } catch (IOException e) {
      e.printStackTrace();
    }
    throw new LoadResourceException("could not load resource " + filename);
  }

  private HttpResponse doQuery(String query) {
    try {
      HttpClient client = httpClientFactory.build();
      URIBuilder builder = new URIBuilder(webConstants.getTriplestoreBaseUri() + QUERY_AFFIX);
      builder.setParameter("query", query);
      builder.setParameter("output", "json");
      HttpGet request = new HttpGet(builder.build());
      request.setHeader("Accept", "application/json");
      return client.execute(request);
    } catch (URISyntaxException | IOException e) {
      logger.error(e.toString());
    }
    throw new QueryException("Could not execute query " + query);
  }

  @Override
  public HttpResponse queryDatasets(Account currentUserAccount) {
    String query = StringUtils.replace(SINGLE_STUDY_MEASUREMENTS, "$owner", "'" + currentUserAccount.getUsername() + "'");
    return doQuery(query);
  }

  @Override
  public Model getDataset(String datasetUUID) {
    DatasetAccessor datasetAccessor = jenaFactory.getDatasetAccessor();
    return datasetAccessor.getModel(Namespaces.DATASET_NAMESPACE + datasetUUID);
  }

  @Override
  public HttpResponse queryDatasetsWithDetail(String datasetUUID) {
    String query = StringUtils.replace(STUDIES_WITH_DETAILS, "$datasetUUID", datasetUUID);
    return doQuery(query);
  }

  @Override
  public boolean isOwner(String datasetUUID, Principal principal) {
    boolean isOwner = false;
    String query = StringUtils.replace(IS_OWNER_QUERY, "$owner", "'" + principal.getName() + "'");
    query = StringUtils.replace(query, "$dataset", datasetUUID);
    HttpResponse response = doQuery(query);

    JSONParser jsonParser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
    try {
      JSONObject jsonObject = (JSONObject) jsonParser.parse(response.getEntity().getContent());
      isOwner = (Boolean) jsonObject.get("boolean");
    } catch (ParseException | IOException e) {
      logger.error("could not parse result from check owner query");
      logger.error(e.toString());
    }
    return isOwner;
  }

  @Override
  public boolean containsStudyWithShortname(String datasetUUID, String shortName) {
    Boolean containsStudyWithShortname = false;
    String query = StringUtils.replace(CONTAINS_STUDY_WITH_SHORTNAME, "$dataset", datasetUUID);
    query = StringUtils.replace(query, "$shortName", "'" + shortName + "'");
    HttpResponse response = doQuery(query);
    try {
      containsStudyWithShortname = JsonPath.read(response.getEntity().getContent(), "$.boolean");
    } catch (IOException e) {
      logger.error(e.toString());
    }

    return containsStudyWithShortname;
  }

  private static class LoadResourceException extends RuntimeException {
    public LoadResourceException(String s) {
      super(s);
    }
  }
}
