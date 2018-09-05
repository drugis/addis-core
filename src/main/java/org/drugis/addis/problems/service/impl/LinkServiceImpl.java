package org.drugis.addis.problems.service.impl;

import org.drugis.addis.models.Model;
import org.drugis.addis.problems.service.HostURLCache;
import org.drugis.addis.problems.service.LinkService;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.service.MappingService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.net.URI;

@Service
public class LinkServiceImpl implements LinkService {

  @Inject
  private HostURLCache hostURLCache;

  @Inject
  private MappingService mappingService;

  @Override
  public URI getModelSourceLink(Project project, Model model) {
    Integer modelAnalysisId = model.getAnalysisId();
    Integer modelProjectId = project.getId();
    Integer ownerId = project.getOwner().getId();
    String hostURL = hostURLCache.get();
    return URI.create(hostURL +
            "/#/users/" + ownerId +
            "/projects/" + modelProjectId +
            "/nma/" + modelAnalysisId +
            "/models/" + model.getId());
  }

  @Override
  public URI getStudySourceLink(Project project, URI studyGraphUri) {
    Integer ownerId = mappingService.getVersionedUuidAndOwner(project.getNamespaceUid()).getOwnerId();
    String hostURL = hostURLCache.get();
    String versionUuid = project.getDatasetVersion().toString().split("/versions/")[1]; // https://trials.drugis.org/versions/aaaa-bbb-ccc
    String studyGraphUuid = studyGraphUri.toString().split("/graphs/")[1];
    return URI.create(hostURL +
            "/#/users/" + ownerId +
            "/datasets/" + project.getNamespaceUid() +
            "/versions/" + versionUuid +
            "/studies/" + studyGraphUuid);
  }
}
