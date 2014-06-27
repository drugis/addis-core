package org.drugis.addis.trialverse.factory.impl;

import org.drugis.addis.trialverse.factory.RestOperationsFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * Created by connor on 27-6-14.
 */
@Component
public class RestOperationsFactoryImpl implements RestOperationsFactory {
  @Override
  public RestOperations build() {
    return new RestTemplate();
  }
}
