package org.drugis.addis.interventions.repository;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.interventions.InterventionCommand;
import org.drugis.addis.security.Account;

import java.util.List;

/**
 * Created by daan on 3/7/14.
 */
public interface InterventionRepository {
  List<Intervention> query(Integer projectId);

  Intervention get(Integer projectId, Integer interventionId) throws ResourceDoesNotExistException;

  Intervention create(Account user, InterventionCommand interventionCommand) throws MethodNotAllowedException, ResourceDoesNotExistException;
}
