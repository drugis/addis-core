package org.drugis.trialverse.study.controller;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.security.repository.AccountRepository;
import org.drugis.trialverse.study.repository.StudyWriteRepository;
import org.drugis.trialverse.testutils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.security.Principal;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StudyControllerTest {
  private MockMvc mockMvc;
  @Inject
  private WebApplicationContext webApplicationContext;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private StudyWriteRepository studyWriteRepository;

  @InjectMocks
  private StudyController studyController;

  private Account john = new Account(1, "john@apple.co.uk", "John", "Lennon");
  private Principal user;

  @Before
  public void setUp() throws Exception {
    accountRepository = mock(AccountRepository.class);
    studyWriteRepository = mock(StudyWriteRepository.class);
    studyController = new StudyController();

    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(studyController).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn(john.getUsername());
    when(accountRepository.findAccountByUsername("john@apple.co.uk")).thenReturn(john);

  }

  @After
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(accountRepository, studyWriteRepository);
  }

  @Test
  public void testCreateStudy() throws Exception {
    String jsonContent = TestUtils.loadResource(this.getClass(), "/mockStudy.json");
    String datasetUUID = "datasetUUID";
    String studyUUID = "studyUUID";
    BasicStatusLine statusLine = new BasicStatusLine(new ProtocolVersion("mock protocol", 1, 0), HttpStatus.CREATED.value(), "some good reason");
    HttpResponse httpResponse = new BasicHttpResponse(statusLine);
    when(studyWriteRepository.createStudy(studyUUID, jsonContent)).thenReturn(httpResponse);
    mockMvc.perform(
        put("/datasets/" + datasetUUID + "/studies/" + studyUUID)
          .content(jsonContent)
          .principal(user))
            .andExpect(status().isCreated());
    verify(accountRepository).findAccountByUsername(user.getName());
    verify(studyWriteRepository).createStudy(studyUUID, jsonContent);
  }

  @Test
  public void testUpdateStudy() throws Exception {
    String jsonContent = TestUtils.loadResource(this.getClass(), "/mockStudy.json");
    String datasetUUID = "datasetUUID";
    String studyUUID = "studyUUID";
    mockMvc.perform(
            post("/datasets/" + datasetUUID + "/studies/" + studyUUID)
                    .content(jsonContent)
                    .principal(user))
            .andExpect(status().isOk());
    verify(accountRepository).findAccountByUsername(user.getName());
    verify(studyWriteRepository).updateStudy(studyUUID, jsonContent);
  }
}
