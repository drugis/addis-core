package org.drugis.trialverse.dataset.controller;

import org.drugis.trialverse.dataset.controller.command.DatasetCommand;
import org.drugis.trialverse.dataset.repository.DatasetRepository;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.security.repository.AccountRepository;
import org.drugis.trialverse.testutils.TestUtils;
import org.drugis.trialverse.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.inject.Inject;
import java.security.Principal;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by connor on 6-11-14.
 */
@Configuration
@EnableWebMvc
public class DatasetControllerTests {

  private MockMvc mockMvc;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private DatasetRepository datasetRepository;

  @Inject
  private WebApplicationContext webApplicationContext;

  @InjectMocks
  private DatasetController datasetController;


  private Principal user;

  private Account john = new Account(1, "john@apple.co.uk", "John", "Lennon"),
          paul = new Account(2, "paul@apple.co.uk", "Paul", "MC Cartney"),
          george = new Account(3, "george@apple.co.uk", "George", "Harrison");


  @Before
  public void setUp() {
    accountRepository = mock(AccountRepository.class);
    datasetRepository = mock(DatasetRepository.class);
    datasetController = new DatasetController();

    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(datasetController).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn(john.getUsername());
    when(accountRepository.findAccountByUsername("john@apple.co.uk")).thenReturn(john);
  }


  @After
  public void tearDown() {
    verifyNoMoreInteractions(accountRepository, datasetRepository);
  }

  @Test
  public void testCreateProject() throws Exception {
    String newDatasetUid = "http://some.thing.like/this/asd123";
    DatasetCommand datasetCommand = new DatasetCommand("dataset title", "some description");
    String jsonContent = TestUtils.createJson(datasetCommand);
    when(datasetRepository.createDataset(datasetCommand.getTitle(), datasetCommand.getDescription(), john)).thenReturn(newDatasetUid);
    mockMvc
            .perform(post("/datasets")
                            .principal(user)
                            .content(jsonContent)
                            .contentType(WebConstants.APPLICATION_JSON_UTF8)
            )
            .andExpect(status().isCreated())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.uid", is(newDatasetUid)));
    verify(accountRepository).findAccountByUsername(john.getUsername());
    verify(datasetRepository).createDataset(datasetCommand.getTitle(), datasetCommand.getDescription(), john);
  }
}
