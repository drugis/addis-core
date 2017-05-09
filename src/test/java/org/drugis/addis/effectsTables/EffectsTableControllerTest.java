package org.drugis.addis.effectsTables;

import org.drugis.addis.TestUtils;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.effectsTables.repository.EffectsTableRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.util.WebConstants;
import org.junit.After;
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
import java.security.Principal;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by joris on 5-4-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class EffectsTableControllerTest {
  @Inject
  private WebApplicationContext webApplicationContext;

  @Inject
  private EffectsTableRepository effectsTableRepository;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private ProjectService projectService;
  private MockMvc mockMvc;

  private Integer analysisId = 2;
  private Principal user;
  private Account gert = new Account(3, "gert", "Gert", "van Valkenhoef", "gert@test.com");

  @Before
  public void setup() {
    reset(effectsTableRepository);
    reset(accountRepository);
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
    when(accountRepository.getAccount(user)).thenReturn(gert);
    when(accountRepository.findAccountByUsername("gert")).thenReturn(gert);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(effectsTableRepository, accountRepository);
  }

  @Test
  public void testGetEffectsTable() throws Exception {
    when(effectsTableRepository.getEffectsTableExclusions(analysisId))
            .thenReturn(Arrays.asList(new EffectsTableExclusion(analysisId, "1"), new EffectsTableExclusion(analysisId, "2")));
    mockMvc.perform(get("/projects/1/analyses/2/effectsTable"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$", hasSize(2)));
    verify(effectsTableRepository).getEffectsTableExclusions(analysisId);
  }

  @Test
  public void testSetEffectsTableExclusion() throws Exception {
    AlternativeIdCommand alternativeIdCommand = new AlternativeIdCommand("abc3");
    String body = TestUtils.createJson(alternativeIdCommand);
    mockMvc.perform(post("/projects/1/analyses/2/effectsTable")
            .principal(user)
            .content(body)
            .contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(status().isOk());
    verify(accountRepository).findAccountByUsername("gert");
    verify(effectsTableRepository).setEffectsTableExclusion(analysisId, "abc3");
  }
}