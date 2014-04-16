package org.drugis.addis.projects.service;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;

import java.security.Principal;

/**
 * Created by connor on 16-4-14.
 */
public interface ProjectService {
  public void checkOwnership(Integer projectId, Principal principal) throws MethodNotAllowedException, ResourceDoesNotExistException;
}
