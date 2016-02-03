package org.drugis.addis.config;

import org.drugis.addis.security.Account;
import org.drugis.addis.security.ApiKey;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.security.repository.ApiKeyRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.util.Date;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by daan on 16-9-15.
 */
@ContextConfiguration(classes = {org.drugis.addis.config.SecurityConfig.class, SecurityConfigTestConfig.class})
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class SecurityConfigTest {

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private ApiKeyRepository apiKeyRepository;

  @Inject
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @Before
  public void setUp() throws Exception {

    reset(accountRepository, apiKeyRepository);
    mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();
  }

  @Test
  public void testForbiddenOnSecurePathWithNoApiKey() throws Exception {
            mockMvc.perform(get("/anything"))
                    .andExpect(status().isForbidden());
  }

  @Test
  public void testInsecurePath() throws Exception {
    mockMvc.perform(get("/"))
            .andExpect(status().isNotFound());
  }

  @Test
  public void testApiKey() throws Exception {
    String supersecretkey = "supersecretkey";
    Account account = new Account("username", "firstName", "lastName", "hash  ");
    when(accountRepository.findAccountByActiveApplicationKey(supersecretkey)).thenReturn(account);
    ApiKey apiKey = new ApiKey(1, supersecretkey, 1, "appNAme", new Date(), new Date());
    when(apiKeyRepository.getKeyBySecretCode(supersecretkey)).thenReturn(apiKey);

            mockMvc.perform(get("/users")
                      .header("X-Auth-Application-Key", supersecretkey))
                      .andExpect(status().isNotFound());
  }

  @Test
  public void testInvalidApiKey() throws Exception {
    mockMvc.perform(post("/bla")
            .header("X-Auth-Application-Key", "invalidkey"))
            .andExpect(status().isUnauthorized());
  }

}