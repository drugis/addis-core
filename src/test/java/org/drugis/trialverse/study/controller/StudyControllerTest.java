package org.drugis.trialverse.study.controller;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.study.repository.StudyWriteRepository;
import org.drugis.trialverse.study.service.StudyService;
import org.drugis.trialverse.testutils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.inject.Inject;
import java.security.Principal;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Configuration
@EnableWebMvc
public class StudyControllerTest {

  private MockMvc mockMvc;

  @Inject
  private WebApplicationContext webApplicationContext;

  @Mock
  private StudyWriteRepository studyWriteRepository;

  @Mock
  private DatasetReadRepository datasetReadRepository;

  @Mock
  private StudyService studyService;

  @InjectMocks
  private StudyController studyController;

  private Account john = new Account(1, "john@apple.co.uk", "John", "Lennon");
  private Principal user;

  @Before
  public void setUp() throws Exception {
    studyWriteRepository = mock(StudyWriteRepository.class);
    datasetReadRepository = mock(DatasetReadRepository.class);
    studyService = mock(StudyService.class);
    studyController = new StudyController();

    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(studyController).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn(john.getUsername());
  }

  @After
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(studyWriteRepository);
  }

  @Test
  public void testCreateStudy() throws Exception {
    String jsonContent = TestUtils.loadResource(this.getClass(), "/mockStudy.json");
    String datasetUUID = "datasetUUID";
    String studyUUID = "studyUUID";

    BasicStatusLine statusLine = new BasicStatusLine(new ProtocolVersion("mock protocol", 1, 0), HttpStatus.CREATED.value(), "some good reason");
    HttpResponse httpResponse = new BasicHttpResponse(statusLine);
    when(datasetReadRepository.isOwner(datasetUUID, user)).thenReturn(true);
    when(studyService.createStudy(datasetUUID, studyUUID, jsonContent)).thenReturn(httpResponse);

    mockMvc.perform(
            put("/datasets/" + datasetUUID + "/studies/" + studyUUID)
                    .content(jsonContent)
                    .principal(user))
            .andExpect(status().isCreated());

    verify(datasetReadRepository).isOwner(datasetUUID, user);
    verify(studyService).createStudy(datasetUUID, studyUUID, jsonContent);
  }

  @Test
  public void testCreateStudyUserIsNotDatasetOwner() throws Exception {
    String jsonContent = TestUtils.loadResource(this.getClass(), "/mockStudy.json");
    String datasetUUID = "datasetUUID";
    String studyUUID = "studyUUID";
    when(datasetReadRepository.isOwner(datasetUUID, user)).thenReturn(false);

    mockMvc.perform(
            put("/datasets/" + datasetUUID + "/studies/" + studyUUID)
                    .content(jsonContent)
                    .principal(user)).andExpect(status().isForbidden());

    verify(datasetReadRepository).isOwner(datasetUUID, user);
    verifyZeroInteractions(studyWriteRepository);
  }

  @Test
  public void testUpdateStudy() throws Exception {
    String jsonContent = TestUtils.loadResource(this.getClass(), "/mockStudy.json");
    MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
    String datasetUUID = "datasetUUID";
    String studyUUID = "studyUUID";
    BasicStatusLine statusLine = new BasicStatusLine(new ProtocolVersion("mock protocol", 1, 0), HttpStatus.OK.value(), "some good reason");
    HttpResponse httpResponse = new BasicHttpResponse(statusLine);
    when(datasetReadRepository.isOwner(datasetUUID, user)).thenReturn(true);
    when(studyWriteRepository.updateStudy(studyUUID, jsonContent)).thenReturn(httpResponse);

    mockMvc.perform(
            post("/datasets/" + datasetUUID + "/studies/" + studyUUID)
                    .content(jsonContent)
                    .principal(user))
            .andExpect(status().isOk());

    verify(datasetReadRepository).isOwner(datasetUUID, user);
    verify(studyWriteRepository).updateStudy(studyUUID, jsonContent);
  }

  @Test
  public void testUpdateStudyUserNotDatasetOwner() throws Exception {
    String jsonContent = TestUtils.loadResource(this.getClass(), "/mockStudy.json");
    String datasetUUID = "datasetUUID";
    String studyUUID = "studyUUID";
    when(datasetReadRepository.isOwner(datasetUUID, user)).thenReturn(false);

    mockMvc.perform(
            post("/datasets/" + datasetUUID + "/studies/" + studyUUID)
                    .content(jsonContent)
                    .principal(user)).andExpect(status().isForbidden());

    verify(datasetReadRepository).isOwner(datasetUUID, user);
  }
}
