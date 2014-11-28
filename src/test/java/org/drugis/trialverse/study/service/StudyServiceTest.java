package org.drugis.trialverse.study.service;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.study.repository.StudyWriteRepository;
import org.drugis.trialverse.study.service.impl.StudyServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.DelegatingServletInputStream;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by connor on 27-11-14.
 */
public class StudyServiceTest {

  @Mock
  private StudyWriteRepository studyWriteRepository;

  @Mock
  private DatasetReadRepository datasetReadRepository;

  @InjectMocks
  private StudyService studyService;

  @Before
  public void setUp() {
    studyWriteRepository = mock(StudyWriteRepository.class);

    studyService = new StudyServiceImpl();
    initMocks(this);
  }

  @Test
  public void testCreateStudy() throws IOException {

    String mockRequestBody = loadMockObject();
    InputStream inputStream = IOUtils.toInputStream(mockRequestBody);
    DelegatingServletInputStream delegatingServletInputStream = new DelegatingServletInputStream(inputStream);


    String datasetUUID = "uuid-1";
    String studyUUID = "s-uuid-1";
    String shortName = "short name";
    when(datasetReadRepository.containsStudyWithShortname(datasetUUID, shortName)).thenReturn(false);
    HttpResponse mockResponse = mock(HttpResponse.class);
    when(studyWriteRepository.createStudy(studyUUID, mockRequestBody)).thenReturn(mockResponse);

    HttpResponse response = studyService.createStudy(datasetUUID, studyUUID, mockRequestBody);

    assertNotNull(response);
  }

  private String loadMockObject() {
    return "{\n" +
            "  \"@graph\": [{\n" +
            "    \"@id\": \"study:5ff3021f-d6f2-4f51-9a18-11026dd6a91d\",\n" +
            "    \"@type\": \"ontology:Study\",\n" +
            "    \"label\": \"yrdydry\",\n" +
            "    \"comment\": \"dydyrdydyrdy\"\n" +
            "  }],\n" +
            "  \"@id\": \"urn:x-arq:DefaultGraphNode\",\n" +
            "  \"@context\": {\n" +
            "    \"atc\": \"http://www.whocc.no/ATC2011/\",\n" +
            "    \"comment\": \"http://www.w3.org/2000/01/rdf-schema#comment\",\n" +
            "    \"dataset\": \"http://trials.drugis.org/datasets/\",\n" +
            "    \"dc\": \"http://purl.org/dc/elements/1.1/\",\n" +
            "    \"label\": \"http://www.w3.org/2000/01/rdf-schema#label\",\n" +
            "    \"ontology\": \"http://trials.drugis.org/ontology#\",\n" +
            "    \"owl\": \"http://www.w3.org/2002/07/owl#\",\n" +
            "    \"rdf\": \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\n" +
            "    \"rdfs\": \"http://www.w3.org/2000/01/rdf-schema#\",\n" +
            "    \"study\": \"http://trials.drugis.org/studies/\"\n" +
            "  }\n" +
            "}";
  }


}
