package org.drugis.addis.projects.repository;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.interventions.InterventionCommand;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.OutcomeCommand;
import org.drugis.addis.projects.Project;
import org.drugis.addis.security.Account;

import java.util.Collection;

/**
 * Created by daan on 2/6/14.
 */
public interface ProjectRepository {
  Collection<Project> query();

  Project getProjectById(Integer projectId) throws ResourceDoesNotExistException;

  Collection<Project> queryByOwnerId(Integer ownerId);

  Project create(Account owner, String name, String description, Integer trialverse);

  Outcome getProjectOutcome(Integer projectId, Integer outcomeId) throws ResourceDoesNotExistException;

  Outcome createOutcome(Account user, Integer projectId, OutcomeCommand outcome) throws MethodNotAllowedException, ResourceDoesNotExistException;

  Intervention getProjectIntervention(Integer projectId, Integer interventionId) throws ResourceDoesNotExistException;

  Intervention createIntervention(Account user, Integer projectId, InterventionCommand interventionCommand) throws ResourceDoesNotExistException, MethodNotAllowedException;
}