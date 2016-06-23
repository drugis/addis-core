package org.drugis.addis.projects.service;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.impl.ReadValueException;

import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;

/**
 * Created by connor on 16-4-14.
 */
public interface ProjectService {
  void checkOwnership(Integer projectId, Principal principal) throws MethodNotAllowedException, ResourceDoesNotExistException;

  void checkProjectExistsAndModifiable(Account user, Integer projectId) throws ResourceDoesNotExistException, MethodNotAllowedException;

  List<TrialDataStudy> queryMatchedStudies(Integer projectId) throws ResourceDoesNotExistException, ReadValueException, URISyntaxException;
}
