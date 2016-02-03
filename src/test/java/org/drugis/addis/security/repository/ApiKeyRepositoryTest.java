package org.drugis.addis.security.repository;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.security.ApiKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by daan on 15-9-15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class ApiKeyRepositoryTest {

  @Inject
  private ApiKeyRepository apiKeyRepository;

  @Test
  public void testGetKeyBySecretCode() {
    String secretKey = "supersecretkey";
    ApiKey apiKey = apiKeyRepository.getKeyBySecretCode(secretKey);
    assertNotNull(apiKey);
    assertEquals("Test Application", apiKey.getApplicationName());
  }
}