package org.drugis.addis.projects.service.impl;

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
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.util.Namespaces;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
  private
  AccountRepository accountRepository;

  @Inject
  private
  ProjectRepository projectRepository;

  @Inject
  private
  TriplestoreService triplestoreService;

  @Inject
  private
  OutcomeRepository outcomeRepository;

  @Inject
  private
  InterventionRepository interventionRepository;

  @Inject
  private
  MappingService mappingService;

  @Inject
  private
  CovariateRepository covariateRepository;

  @Inject
  VersionMappingRepository versionMappingRepository;

  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  @Inject
  EntityManager em;

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
  public Integer copy(Account user, Integer sourceProjectId) throws ResourceDoesNotExistException, ReadValueException, URISyntaxException {
    Project sourceProject = projectRepository.get(sourceProjectId);
    ProjectCommand command = sourceProject.getCommand();
    String addisDatasetUuid = sourceProject.getNamespaceUid();
    URI datasetUri = URI.create(Namespaces.DATASET_NAMESPACE + addisDatasetUuid);
    VersionMapping mapping = versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri);
    String trialverseDatasetUuid = mapping.getVersionedDatasetUri().toString().split("/datasets/")[1];
    URI headVersion = URI.create(triplestoreService.getHeadVersion(mapping.getVersionedDatasetUri()));
    command.setDatasetVersion(headVersion);
    Project newProject = projectRepository.create(user, command);

    //Outcomes
    Collection<Outcome> sourceOutcomes = outcomeRepository.query(sourceProjectId);
    List<SemanticVariable> semanticOutcomes = triplestoreService.getOutcomes(trialverseDatasetUuid, headVersion);
    List<URI> unitConcepts = triplestoreService.getUnitUris(trialverseDatasetUuid, headVersion);

    sourceOutcomes.stream()
        .filter(sourceOutcome ->
            semanticOutcomes.stream().anyMatch(semanticOutcome -> semanticOutcome.getUri().equals(sourceOutcome.getSemanticOutcomeUri())))
        .forEach(outcome -> {
          SemanticVariable semanticVariable = new SemanticVariable(outcome.getSemanticOutcomeUri(), outcome.getSemanticOutcomeLabel());
          try {
            em.persist(new Outcome(newProject.getId(), outcome.getName(), outcome.getDirection(), outcome.getMotivation(), semanticVariable));
          } catch (Exception e) {
            e.printStackTrace();
          }
        });

    //Covariates
    Collection<Covariate> sourceCovariates = covariateRepository.findByProject(sourceProjectId);
    List<SemanticVariable> semanticCovariates = triplestoreService.getPopulationCharacteristics(trialverseDatasetUuid, headVersion);

    sourceCovariates.stream()
        .filter(sourceCovariate -> sourceCovariate.getType().equals(CovariateOptionType.STUDY_CHARACTERISTIC) ||
            semanticCovariates.stream().anyMatch(semanticCovariate -> semanticCovariate.getUri().toString().equals(sourceCovariate.getDefinitionKey())))
        .forEach(covariate -> em.persist(new Covariate(newProject.getId(), covariate.getName(), covariate.getMotivation(), covariate.getDefinitionKey(), covariate.getType())));

    //Interventions
    Set<AbstractIntervention> sourceInterventions = interventionRepository.query(sourceProjectId);
    List<SemanticInterventionUriAndName> semanticInterventions = triplestoreService.getInterventions(trialverseDatasetUuid, headVersion);

    Map<Integer, Integer> oldIdToNewId = new HashMap<>();

    sourceInterventions.stream()
        .filter(intervention -> intervention instanceof SingleIntervention)
        .map(intervention -> (SingleIntervention) intervention)
        .filter(sourceIntervention -> semanticInterventions.stream().anyMatch(semanticIntervention -> semanticIntervention.getUri().equals(sourceIntervention.getSemanticInterventionUri())))
        .filter(intervention -> checkInterventionUnits(intervention, unitConcepts))
        .forEach(intervention -> {
          try {
            SingleIntervention newIntervention = buildSingleIntervention(newProject.getId(), intervention);
            assert newIntervention != null;
            em.persist(newIntervention);
            oldIdToNewId.put(intervention.getId(), newIntervention.getId());
          } catch (InvalidConstraintException e) {
            e.printStackTrace();
          }
        });

    //combination interventions before intervention sets because intervention sets may contain combination interventions
    sourceInterventions.stream()
        .filter(intervention -> (intervention instanceof CombinationIntervention))
        .map(intervention -> (CombinationIntervention) intervention)
        .filter(intervention -> intervention.getInterventionIds().stream().allMatch(id -> oldIdToNewId.get(id) != null))
        .forEach(intervention -> {
          MultipleIntervention multipleIntervention = buildMultipleIntervention(newProject.getId(), intervention, oldIdToNewId);
          assert multipleIntervention != null;
          em.persist(multipleIntervention);
          oldIdToNewId.put(intervention.getId(), multipleIntervention.getId());
        });
    // intervention sets
    sourceInterventions.stream()
            .filter(intervention -> (intervention instanceof InterventionSet))
            .map(intervention -> (InterventionSet) intervention)
            .filter(intervention -> intervention.getInterventionIds().stream().allMatch(id -> oldIdToNewId.get(id) != null))
            .forEach(intervention -> {
              MultipleIntervention multipleIntervention = buildMultipleIntervention(newProject.getId(), intervention, oldIdToNewId);
              assert multipleIntervention != null;
              em.persist(multipleIntervention);
            });

    return newProject.getId();
  }

  private Boolean checkInterventionUnits(SingleIntervention intervention, List<URI> unitConcepts) {
    if(intervention instanceof SimpleIntervention) {
      return true; // simple interventions don't have units -> always ok
    }
    if(intervention instanceof FixedDoseIntervention) {
      FixedDoseIntervention cast = (FixedDoseIntervention) intervention;
      return areConstraintUnitsKnown(cast.getConstraint(), unitConcepts);
    }
    if(intervention instanceof TitratedDoseIntervention) {
      TitratedDoseIntervention cast = (TitratedDoseIntervention) intervention;
      return areConstraintUnitsKnown(cast.getMinConstraint(), unitConcepts) && areConstraintUnitsKnown(cast.getMaxConstraint(), unitConcepts);
    }
    if(intervention instanceof BothDoseTypesIntervention) {
      BothDoseTypesIntervention cast = (BothDoseTypesIntervention) intervention;
      return areConstraintUnitsKnown(cast.getMinConstraint(), unitConcepts) && areConstraintUnitsKnown(cast.getMaxConstraint(), unitConcepts);
    }
    return true;
  }

  private Boolean areConstraintUnitsKnown(DoseConstraint constraint, List<URI> uriConcepts) {
    return constraint == null ||
            (
              (constraint.getLowerBound() == null || uriConcepts.contains(constraint.getLowerBound().getUnitConcept())) &&
              (constraint.getUpperBound() == null || uriConcepts.contains(constraint.getUpperBound().getUnitConcept()))
            );
  }

  private MultipleIntervention buildMultipleIntervention(Integer newProjectId, MultipleIntervention intervention, Map<Integer, Integer> oldIdToNewId) {
    Set<Integer> updatedInterventionIds = intervention.getInterventionIds().stream()
        .map(oldIdToNewId::get)
        .collect(Collectors.toSet());
    if (intervention instanceof CombinationIntervention) {
      return new CombinationIntervention(newProjectId, intervention.getName(), intervention.getMotivation(),
          updatedInterventionIds);
    } else if (intervention instanceof InterventionSet) {
      return new InterventionSet(newProjectId, intervention.getName(), intervention.getMotivation(),
          updatedInterventionIds);
    }
    return null;
  }

  private SingleIntervention buildSingleIntervention(Integer newProjectId, SingleIntervention intervention) throws InvalidConstraintException {
    if (intervention instanceof SimpleIntervention) {
      SemanticInterventionUriAndName semanticInterventionUuidAndName = new SemanticInterventionUriAndName(intervention.getSemanticInterventionUri(), intervention.getSemanticInterventionLabel());
      return new SimpleIntervention(newProjectId, intervention.getName(), intervention.getMotivation(), semanticInterventionUuidAndName);
    } else if (intervention instanceof FixedDoseIntervention) {
      FixedDoseIntervention cast = (FixedDoseIntervention) intervention;
      return new FixedDoseIntervention(newProjectId, cast.getName(), cast.getMotivation(), cast.getSemanticInterventionUri(), cast.getSemanticInterventionLabel(), cast.getConstraint());
    } else if (intervention instanceof TitratedDoseIntervention) {
      TitratedDoseIntervention cast = (TitratedDoseIntervention) intervention;
      return new TitratedDoseIntervention(newProjectId, cast.getName(), cast.getMotivation(), cast.getSemanticInterventionUri(), cast.getSemanticInterventionLabel(), cast.getMinConstraint(), cast.getMaxConstraint());
    } else if (intervention instanceof BothDoseTypesIntervention){
      BothDoseTypesIntervention cast = (BothDoseTypesIntervention) intervention;
      return new BothDoseTypesIntervention(newProjectId, cast.getName(), cast.getMotivation(), cast.getSemanticInterventionUri(), cast.getSemanticInterventionLabel(), cast.getMinConstraint(), cast.getMaxConstraint());
    }
    return null;
  }

}
