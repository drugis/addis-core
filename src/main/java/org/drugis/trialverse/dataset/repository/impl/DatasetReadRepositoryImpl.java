package org.drugis.trialverse.dataset.repository.impl;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.util.WebConstants;

import java.io.IOException;

/**
 * Created by daan on 7-11-14.
 */
public class DatasetReadRepositoryImpl implements DatasetReadRepository {

  @Override
  public HttpResponse queryDatasets(Account currentUserAccount) {
    HttpGet request = new HttpGet(WebConstants.TRIPLESTORE_BASE_URI + "/current/query");
    HttpClient client = HttpClients.createDefault();
    try {
      return  client.execute(request);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
