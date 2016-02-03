package org.drugis.addis.security.repository;

import org.drugis.addis.security.ApiKey;

/**
 * Created by daan on 15-9-15.
 */
public interface ApiKeyRepository {
  ApiKey getKeyBySecretCode(String secretCode);

  ApiKey get(Integer id);
}
