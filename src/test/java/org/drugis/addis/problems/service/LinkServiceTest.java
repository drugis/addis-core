package org.drugis.addis.problems.service;

import org.drugis.addis.models.Model;
import org.drugis.addis.problems.service.impl.LinkServiceImpl;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.model.mapping.TriplestoreUuidAndOwner;
import org.drugis.addis.trialverse.service.MappingService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class LinkServiceTest {

  @Mock
  private  HostURLCache hostURLCache;

  @Mock
  private  MappingService mappingService;

  @InjectMocks
  private LinkService linkService;

  private Integer projectId = 13;
  private Integer ownerId = 37;
  private String hostUrl = "http://www.host.url";
  private Project project= mock(Project.class, RETURNS_DEEP_STUBS);
  private final String namespaceUuid = "namespaceUuid";

  @Before
  public void setUp() {
    linkService = new LinkServiceImpl();
    initMocks(this);

    when(hostURLCache.get()).thenReturn(hostUrl);

    when(project.getNamespaceUid()).thenReturn(namespaceUuid);
    when(project.getId()).thenReturn(projectId);
    when(project.getOwner().getId()).thenReturn(ownerId);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(mappingService, hostURLCache);
  }

  @Test
  public void testGetModelSourceLink() {
    Model model = mock(Model.class);
    Integer analysisId = 42;
    when(model.getAnalysisId()).thenReturn(analysisId);
    Integer modelId = 1337;
    when(model.getId()).thenReturn(modelId);

    // ex
    URI result = linkService.getModelSourceLink(project, model);

    URI expectedResult = URI.create(hostUrl+
            "/#/users/" + ownerId +
            "/projects/" + projectId +
            "/nma/" + analysisId +
            "/models/" + modelId);
    assertEquals(expectedResult, result);

    verify(hostURLCache).get();
  }

  @Test
  public void getStudySourceLink() {
    String studyGraphUuid = "studyGraphUuid";
    URI studyGraphUri = URI.create("host/graphs/"+ studyGraphUuid);

    TriplestoreUuidAndOwner ownerMock = mock(TriplestoreUuidAndOwner.class);
    when(ownerMock.getOwnerId()).thenReturn(ownerId);
    when(mappingService.getVersionedUuidAndOwner(namespaceUuid)).thenReturn(ownerMock);

    String versionUuid = "versionUuid";
    URI datasetVersion = URI.create("host/versions/" + versionUuid);
    when(project.getDatasetVersion()).thenReturn(datasetVersion);

    // ex
    URI result = linkService.getStudySourceLink(project, studyGraphUri);

    URI expectedResult = URI.create(hostUrl +
            "/#/users/" + ownerId +
            "/datasets/" + namespaceUuid +
            "/versions/" + versionUuid +
            "/studies/" + studyGraphUuid);
    assertEquals(expectedResult, result);

    verify(mappingService).getVersionedUuidAndOwner(namespaceUuid);
    verify(hostURLCache).get();
  }
}