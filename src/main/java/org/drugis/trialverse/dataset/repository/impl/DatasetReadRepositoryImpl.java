package org.drugis.trialverse.dataset.repository.impl;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.util.WebConstants;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * Created by daan on 7-11-14.
 */
@Repository
public class DatasetReadRepositoryImpl implements DatasetReadRepository {

  private final static String SINGLE_STUDY_MEASUREMENTS = loadResource("queryDatasetsConstruct.sparql");

  private static String loadResource(String filename) {
    try {
      Resource myData = new ClassPathResource(filename);
      InputStream stream = myData.getInputStream();
      String query = IOUtils.toString(stream, "UTF-8");
      if (query.isEmpty()) {

      }
      return query;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "";
  }

  @Override
  public HttpResponse queryDatasets(Account currentUserAccount) {
    try {
      HttpClient client = HttpClients.createDefault();
      URIBuilder builder = new URIBuilder(WebConstants.TRIPLESTORE_BASE_URI + "/current/query");
      builder.setParameter("query", SINGLE_STUDY_MEASUREMENTS);
      HttpGet request = new HttpGet(builder.build());
      request.setHeader("Accept", "text/turtle");
      HttpResponse response = client.execute(request);
      return response;
    } catch (URISyntaxException e) {
      e.printStackTrace();
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
