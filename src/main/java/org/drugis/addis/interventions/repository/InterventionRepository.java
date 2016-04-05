package org.drugis.addis.interventions.repository;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.AbstractInterventionCommand;
import org.drugis.addis.security.Account;

import java.util.List;

/**
 * Created by daan on 3/7/14.
 */
public interface InterventionRepository {
  List<AbstractIntervention> query(Integer projectId);

  AbstractIntervention get(Integer projectId, Integer interventionId) throws ResourceDoesNotExistException;

  AbstractIntervention create(Account user, AbstractInterventionCommand interventionCommand) throws MethodNotAllowedException, ResourceDoesNotExistException;
}
