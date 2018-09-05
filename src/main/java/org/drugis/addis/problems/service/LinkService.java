package org.drugis.addis.problems.service;

import org.drugis.addis.models.Model;
import org.drugis.addis.projects.Project;

import java.net.URI;

public interface LinkService {
  URI getModelSourceLink(Project project, Model model);

  URI getStudySourceLink(Project project, URI studyGraphUri);
}
