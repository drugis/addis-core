package org.drugis.trialverse.dataset.service;

import java.security.Principal;

/**
 * Created by connor on 26-11-14.
 */
public interface DatasetService {
  public boolean isOwner(Principal currentUser);
}
