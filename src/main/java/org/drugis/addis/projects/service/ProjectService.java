package org.drugis.addis.projects.service;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.service.impl.UpdateProjectException;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.impl.ReadValueException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by connor on 16-4-14.
 */
public interface ProjectService {
  void checkOwnership(Integer projectId, Principal principal) throws MethodNotAllowedException, ResourceDoesNotExistException;

  void checkProjectExistsAndModifiable(Account user, Integer projectId) throws ResourceDoesNotExistException, MethodNotAllowedException;

  List<TrialDataStudy> queryMatchedStudies(Integer projectId) throws ResourceDoesNotExistException, ReadValueException, URISyntaxException, IOException;

  Project updateProject(Integer projectId, String name, String description) throws UpdateProjectException, ResourceDoesNotExistException;

  Integer copy(Account user, Integer projectId, String newTitle) throws ResourceDoesNotExistException, SQLException;

  Integer createUpdated(Account user, Integer projectId) throws ResourceDoesNotExistException, ReadValueException, URISyntaxException, SQLException, IOException;
}
