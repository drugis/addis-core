package org.drugis.trialverse.security.repository;

import org.drugis.trialverse.security.ApiKey;

/**
 * Created by daan on 15-9-15.
 */
public interface ApiKeyRepository {
  ApiKey getKeyBySecretCode(String s);
}
