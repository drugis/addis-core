package org.drugis.addis.trialverse.factory;

import org.springframework.web.client.RestOperations;

/**
 * Created by connor on 27-6-14.
 */
public interface RestOperationsFactory {
  public RestOperations build();
}
