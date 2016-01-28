package org.drugis.trialverse.dataset.factory;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;

/**
 * Created by connor on 12-11-14.
 */
@Component
public class HttpClientFactory {

  public HttpClient build() {
    return HttpClients.createDefault();
  }
}
