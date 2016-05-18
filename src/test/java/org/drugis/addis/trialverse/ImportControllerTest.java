package org.drugis.addis.trialverse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.drugis.addis.trialverse.controller.ImportController;
import org.drugis.addis.trialverse.service.ClinicalTrialsImportService;
import org.drugis.addis.trialverse.service.impl.ClinicalTrialsImportError;
import org.drugis.addis.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by connor on 12-5-16.
 */
@Configuration
@EnableWebMvc
public class ImportControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  ClinicalTrialsImportService clinicalTrialsImportService = mock(ClinicalTrialsImportService.class);

  @InjectMocks
  private ImportController importController;

  @Before
  public void setUp() throws URISyntaxException {
    reset(clinicalTrialsImportService);
    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(importController).build();
  }


  @After
  public void cleanUp() throws URISyntaxException {
    verifyNoMoreInteractions(clinicalTrialsImportService);
  }

  @Test
  public void testFetchInfo() throws Exception, ClinicalTrialsImportError {
    String nctId = "nct0123";

    JsonNode resultObject = objectMapper.readTree("{\"foo\": \"bar\"}");
    when(clinicalTrialsImportService.fetchInfo(nctId)).thenReturn(resultObject);
    mockMvc.perform(get("/import/" + nctId ))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$.foo", is("bar")));
    verify(clinicalTrialsImportService).fetchInfo(nctId);
  }

  @Test
  public void testFetchInfoNotFound() throws Exception, ClinicalTrialsImportError {
    String nctId = "nct0123";
    when(clinicalTrialsImportService.fetchInfo(nctId)).thenReturn(null);
    ResultActions resultActions = mockMvc.perform(get("/import/" + nctId))
            .andExpect(status().isOk());
    resultActions.andExpect(content().string(""));
    verify(clinicalTrialsImportService).fetchInfo(nctId);
  }
}
