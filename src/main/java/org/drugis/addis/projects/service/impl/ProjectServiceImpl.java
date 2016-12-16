package org.drugis.addis.projects.service.impl;

import com.sun.org.apache.xpath.internal.operations.Mult;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.*;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.ProjectCommand;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.SemanticInterventionUriAndName;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.drugis.addis.trialverse.model.emun.CovariateOptionType;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.drugis.addis.util.WebConstants;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by connor on 16-4-14.
 */
@Service
public class ProjectServiceImpl implements ProjectService {

  @Inject
  AccountRepository accountRepository;

  @Inject
  ProjectRepository projectRepository;

  @Inject
  TriplestoreService triplestoreService;

  @Inject
  OutcomeRepository outcomeRepository;

  @Inject
  InterventionRepository interventionRepository;

  @Inject
  MappingService mappingService;

  @Inject
  CovariateRepository covariateRepository;

  @Override
  public void checkOwnership(Integer projectId, Principal principal) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(principal.getName());
    Project project = projectRepository.get(projectId);

    if (project == null || !project.getOwner().equals(user)) {
      throw new MethodNotAllowedException();
    }
  }

  @Override
  public void checkProjectExistsAndModifiable(Account user, Integer projectId) throws ResourceDoesNotExistException, MethodNotAllowedException {
    Project project = projectRepository.get(projectId);
    if (!project.getOwner().getId().equals(user.getId())) {
      throw new MethodNotAllowedException();
    }
  }

  @Override
  public List<TrialDataStudy> queryMatchedStudies(Integer projectId) throws ResourceDoesNotExistException, ReadValueException, URISyntaxException {
    Project project = projectRepository.get(projectId);
    Set<AbstractIntervention> interventions = interventionRepository.query(projectId);
    Set<URI> singleInterventionUris = interventions.stream()
        .filter(ai -> ai instanceof SingleIntervention)
        .map(ai -> (SingleIntervention) ai)
        .map(SingleIntervention::getSemanticInterventionUri)
        .collect(Collectors.toSet());

    Set<URI> outcomeUris = outcomeRepository.query(projectId)
        .stream()
        .map(Outcome::getSemanticOutcomeUri)
        .collect(Collectors.toSet());
    List<TrialDataStudy> studies = triplestoreService.getAllTrialData(mappingService.getVersionedUuid(project.getNamespaceUid()), project.getDatasetVersion(), outcomeUris, singleInterventionUris);
    studies = triplestoreService.addMatchingInformation(interventions, studies);
    return studies;
  }

  @Override
  public Project updateProject(Integer projectId, String name, String description) throws UpdateProjectException, ResourceDoesNotExistException {
    if (projectRepository.isExistingProjectName(projectId, name)) {
      throw new UpdateProjectException("Can not update project; duplicate project name");
    }
    return projectRepository.updateNameAndDescription(projectId, name, description);
  }

  @Override
  public Integer copy(Account user, Integer sourceProjectId) throws ResourceDoesNotExistException, ReadValueException {
    Project sourceProject = projectRepository.get(sourceProjectId);
    ProjectCommand command = sourceProject.getCommand();
    URI datasetUri = WebConstants.buildDatasetUri(sourceProject.getNamespaceUid());
    URI headVersion = URI.create(triplestoreService.getHeadVersion(datasetUri));
    command.setDatasetVersion(headVersion);
    Project newProject = projectRepository.create(user, command);

    //Outcomes
    Collection<Outcome> sourceOutcomes = outcomeRepository.query(sourceProjectId);
    List<SemanticVariable> semanticOutcomes = triplestoreService.getOutcomes(sourceProject.getNamespaceUid(), headVersion);

    sourceOutcomes.stream()
        .filter(sourceOutcome ->
            semanticOutcomes.stream().anyMatch(semanticOutcome -> semanticOutcome.getUri().equals(sourceOutcome.getSemanticOutcomeUri())))
        .forEach(outcome -> {
          SemanticVariable semanticVariable = new SemanticVariable(outcome.getSemanticOutcomeUri(), outcome.getSemanticOutcomeLabel());
          try {
            new Outcome(newProject.getId(), outcome.getName(), outcome.getDirection(), outcome.getMotivation(), semanticVariable);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });

    //Covariates
    Collection<Covariate> sourceCovariates = covariateRepository.findByProject(sourceProjectId);
    List<SemanticVariable> semanticCovariates = triplestoreService.getPopulationCharacteristics(sourceProject.getNamespaceUid(), headVersion);

    sourceCovariates.stream()
        .filter(sourceCovariate -> sourceCovariate.getType().equals(CovariateOptionType.STUDY_CHARACTERISTIC) ||
            semanticCovariates.stream().anyMatch(semanticCovariate -> semanticCovariate.getUri().toString().equals(sourceCovariate.getDefinitionKey())))
        .forEach(covariate -> new Covariate(newProject.getId(), covariate.getName(), covariate.getMotivation(), covariate.getDefinitionKey(), covariate.getType()));

    //Interventions
    Set<AbstractIntervention> sourceInterventions = interventionRepository.query(sourceProjectId);
    List<SemanticInterventionUriAndName> semanticInterventions = triplestoreService.getInterventions(sourceProject.getNamespaceUid(), headVersion);

    Map<Integer, Integer> oldIdToNewId = new HashMap<>();

    sourceInterventions.stream()
        .filter(intervention -> intervention instanceof SingleIntervention)
        .map(intervention -> (SingleIntervention) intervention)
        .filter(sourceIntervention -> semanticInterventions.stream().anyMatch(semanticIntervention -> semanticIntervention.getUri().equals(sourceIntervention.getSemanticInterventionUri())))
        .forEach(intervention -> {
          try {
            SingleIntervention newIntervention = buildSingleIntervention(newProject.getId(), intervention);
            oldIdToNewId.put(intervention.getId(), newIntervention.getId());
            return newIntervention;
          } catch (InvalidConstraintException e) {
            e.printStackTrace();
          }
          return null;
        });
    sourceInterventions.stream()
        .filter(intervention -> (intervention instanceof MultipleIntervention))
        .map(intervention -> (MultipleIntervention) intervention)
        .forEach(intervention -> {
          createMultipleIntervention(newProject.getId(), intervention, oldIdToNewId);
        });
    return newProject.getId();
  }

  private void createMultipleIntervention(Integer newProjectId, MultipleIntervention intervention, Map<Integer, Integer> oldIdToNewId) {
    if (intervention.getInterventionIds().stream().anyMatch(id -> oldIdToNewId.get(id) == null)) {
      return;
    }
    Set<Integer> updatedInterventionIds = intervention.getInterventionIds().stream()
        .map(oldIdToNewId::get)
        .collect(Collectors.toSet());
    if (intervention instanceof CombinationIntervention) {
      new CombinationIntervention(newProjectId, intervention.getName(), intervention.getMotivation(),
          updatedInterventionIds);
    } else if (intervention instanceof InterventionSet) {
      new InterventionSet(newProjectId, intervention.getName(), intervention.getMotivation(),
          updatedInterventionIds);
    }
  }

  private SingleIntervention buildSingleIntervention(Integer newProjectId, SingleIntervention intervention) throws InvalidConstraintException {
    if (intervention instanceof SimpleIntervention) {
      SemanticInterventionUriAndName semanticInterventionUriAndName = new SemanticInterventionUriAndName(intervention.getSemanticInterventionUri(), intervention.getSemanticInterventionLabel());
      return new SimpleIntervention(newProjectId, intervention.getName(), intervention.getMotivation(), semanticInterventionUriAndName);
    } else if (intervention instanceof FixedDoseIntervention) {
      FixedDoseIntervention cast = (FixedDoseIntervention) intervention;
      return new FixedDoseIntervention(newProjectId, cast.getName(), cast.getMotivation(), cast.getSemanticInterventionUri(), cast.getSemanticInterventionLabel(), cast.getConstraint());
    } else if (intervention instanceof TitratedDoseIntervention) {
      TitratedDoseIntervention cast = (TitratedDoseIntervention) intervention;
      return new TitratedDoseIntervention(newProjectId, cast.getName(), cast.getMotivation(), cast.getSemanticInterventionUri(), cast.getSemanticInterventionLabel(), cast.getMinConstraint(), cast.getMaxConstraint());
    } else {
      BothDoseTypesIntervention cast = (BothDoseTypesIntervention) intervention;
      return new BothDoseTypesIntervention(newProjectId, cast.getName(), cast.getMotivation(), cast.getSemanticInterventionUri(), cast.getSemanticInterventionLabel(), cast.getMinConstraint(), cast.getMaxConstraint());
    }
  }

}
