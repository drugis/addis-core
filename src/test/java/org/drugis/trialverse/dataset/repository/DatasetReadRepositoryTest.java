package org.drugis.trialverse.dataset.repository;


import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.drugis.trialverse.dataset.repository.impl.DatasetReadRepositoryImpl;
import org.drugis.trialverse.security.Account;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class DatasetReadRepositoryTest {

  DatasetReadRepository datasetReadRepository;

  @Before
  public void init() {
    datasetReadRepository = new DatasetReadRepositoryImpl();
  }

  @Ignore
  @Test
  public void testQueryDatasets() throws Exception {
    Account account = mock(Account.class);

    HttpResponse httpResponse = datasetReadRepository.queryDatasets(account);
    assertNotNull(httpResponse);
    String responce = parseResponse(httpResponse);
    assertNotNull(responce);

  }

  /**
   * parses the response from the HttpResponse
   *
   * @param response
   * @return
   * @throws Exception
   */
  private static String parseResponse(HttpResponse response) throws Exception {
    String result = null;
    BufferedReader reader = null;
    try {
      Header contentEncoding = response.getFirstHeader("Content-Encoding");
      if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
        reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(response.getEntity().getContent())));
      } else {
        reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      }
      StringBuilder sb = new StringBuilder();
      String line = null;
      while ((line = reader.readLine()) != null) {
        sb.append(line + "\n");
      }
      result = sb.toString();
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
    return result;
  }
}