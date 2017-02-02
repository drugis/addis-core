package org.drugis.addis.projects.service.impl;

import org.drugis.addis.analyses.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.MetaBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.controller.command.*;
import org.drugis.addis.interventions.model.*;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.ProjectCommand;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by connor on 16-4-14.
 */
@Service
public class ProjectServiceImpl implements ProjectService {

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private AnalysisRepository analysisRepository;

  @Inject
  private AnalysisService analysisService;

  @Inject
  private MetaBenefitRiskAnalysisRepository metaBenefitRiskAnalysisRepository;

  @Inject
  private ProjectRepository projectRepository;

  @Inject
  private TriplestoreService triplestoreService;

  @Inject
  private OutcomeRepository outcomeRepository;

  @Inject
  private InterventionRepository interventionRepository;

  @Inject
  private MappingService mappingService;

  @Inject
  private CovariateRepository covariateRepository;

  @Inject
  private VersionMappingRepository versionMappingRepository;

  @Inject
  private ModelRepository modelRepository;

  @Inject
  private ScenarioRepository scenarioRepository;

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
  public Integer copy(Account user, Integer sourceProjectId) throws ResourceDoesNotExistException, SQLException {
    Project sourceProject = projectRepository.get(sourceProjectId);
    ProjectCommand command = sourceProject.getCommand();
    command.setDatasetVersion(sourceProject.getDatasetVersion());
    Project newProject = projectRepository.create(user, command);

    //outcomes
    Map<Integer, Integer> oldToNewOutcomeId = new HashMap<>();
    Collection<Outcome> sourceOutcomes = outcomeRepository.query(sourceProjectId);
    sourceOutcomes.forEach(outcomeCreator(user, newProject, oldToNewOutcomeId));

    //covariates
    Map<Integer, Integer> oldToNewCovariateId = new HashMap<>();
    Collection<Covariate> sourceCovariates = covariateRepository.findByProject(sourceProjectId);
    sourceCovariates.forEach(covariateCreator(newProject, oldToNewCovariateId));

    //interventions
    Map<Integer, Integer> oldToNewInterventionId = new HashMap<>();
    Set<AbstractIntervention> sourceInterventions = interventionRepository.query(sourceProjectId);
    sourceInterventions.stream()
            .filter(intervention -> intervention instanceof SingleIntervention)
            .map(intervention -> (SingleIntervention) intervention)
            .forEach(singleInterventionCreator(user, newProject, oldToNewInterventionId));
    sourceInterventions.stream()
            .filter(intervention -> (intervention instanceof CombinationIntervention))
            .map(intervention -> (CombinationIntervention) intervention)
            .forEach(combinationInterventionCreator(user, newProject, oldToNewInterventionId));
    sourceInterventions.stream()
            .filter(intervention -> (intervention instanceof InterventionSet))
            .map(intervention -> (InterventionSet) intervention)
            .forEach(interventionSetCreator(user, newProject, oldToNewInterventionId));

    //analyses
    Map<Integer, Integer> oldToNewAnalysisId = new HashMap<>();
    Collection<AbstractAnalysis> sourceAnalyses = analysisRepository.query(sourceProjectId);
    sourceAnalyses.stream()
            .filter(analysis -> analysis instanceof SingleStudyBenefitRiskAnalysis)
            .map(analysis -> (SingleStudyBenefitRiskAnalysis) analysis)
            .forEach(singleStudyBenefitRiskAnalysisCreator(newProject, user, oldToNewInterventionId, oldToNewOutcomeId));
    sourceAnalyses.stream()
            .filter(analysis -> analysis instanceof NetworkMetaAnalysis)
            .map(analysis -> (NetworkMetaAnalysis) analysis)
            .forEach(netWorkMetaAnalysisCreator(user, newProject, oldToNewAnalysisId, oldToNewInterventionId, oldToNewCovariateId));


    //models
    Map<Integer, Integer> oldToNewModelId = new HashMap<>();
    Collection<Model> sourceModels = modelRepository.findNetworkModelsByProject(sourceProjectId);
    sourceModels.forEach(modelCreator(oldToNewAnalysisId, oldToNewModelId));

    //mbr analyses
    sourceAnalyses.stream()
            .filter(analysis -> analysis instanceof MetaBenefitRiskAnalysis)
            .map(analysis -> (MetaBenefitRiskAnalysis) analysis)
            .forEach(metaBenefitRiskCreator(user, newProject, oldToNewOutcomeId, oldToNewInterventionId, oldToNewAnalysisId, oldToNewModelId));

    //scenario's
    Collection<Scenario> sourceScenarios = scenarioRepository.queryByProject(sourceProjectId);
    sourceScenarios.forEach(scenario -> scenarioRepository.create(oldToNewAnalysisId.get(scenario.getWorkspace()),
            scenario.getTitle(), scenario.getState()));

    return newProject.getId();
  }

  private Consumer<MetaBenefitRiskAnalysis> metaBenefitRiskCreator(Account user, Project newProject, Map<Integer, Integer> oldToNewOutcomeId, Map<Integer, Integer> oldToNewInterventionId, Map<Integer, Integer> oldToNewAnalysisId, Map<Integer, Integer> oldToNewModelId) {
    return oldAnalysis -> {
      AnalysisCommand analysisCommand = new AnalysisCommand(newProject.getId(), oldAnalysis.getTitle(),
              AnalysisType.EVIDENCE_SYNTHESIS);
      try {
        MetaBenefitRiskAnalysis newAnalysis = metaBenefitRiskAnalysisRepository.create(user, analysisCommand);
        newAnalysis.setFinalized(oldAnalysis.isFinalized());
        updateIncludedInterventions(oldAnalysis, newAnalysis, oldToNewInterventionId);
        List<MbrOutcomeInclusion> updateMBROutcomeInclusions = oldAnalysis.getMbrOutcomeInclusions().stream()
                .map(inclusion -> new MbrOutcomeInclusion(newAnalysis.getId(),
                        oldToNewOutcomeId.get(inclusion.getOutcomeId()),
                        oldToNewAnalysisId.get(inclusion.getNetworkMetaAnalysisId()),
                        oldToNewModelId.get(inclusion.getModelId())))
                .collect(Collectors.toList());
        newAnalysis.setMbrOutcomeInclusions(updateMBROutcomeInclusions);
      } catch (ResourceDoesNotExistException | MethodNotAllowedException | IOException | SQLException e) {
        e.printStackTrace();
      }
    };
  }

  private Consumer<Model> modelCreator(Map<Integer, Integer> oldIdToNewAnalysisId, Map<Integer, Integer> oldToNewModelId) {
    return oldModel -> {
      try {
        Model newModel = new Model(oldModel);
        newModel.setAnalysisId(oldIdToNewAnalysisId.get(oldModel.getAnalysisId()));
        newModel = modelRepository.persist(newModel);
        oldToNewModelId.put(oldModel.getId(), newModel.getId());
      } catch (InvalidModelException e) {
        e.printStackTrace();
      }
    };
  }

  private Consumer<? super SingleStudyBenefitRiskAnalysis> singleStudyBenefitRiskAnalysisCreator(Project newProject, Account user, Map<Integer, Integer> oldIdToNewInterventionId, Map<Integer, Integer> oldIdToNewOutcomeId) {
    return analysis -> {
      AnalysisCommand analysisCommand = new AnalysisCommand(newProject.getId(), analysis.getTitle(),
              AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL);
      try {
        final SingleStudyBenefitRiskAnalysis newAnalysis = analysisService.createSingleStudyBenefitRiskAnalysis(user, analysisCommand);
        newAnalysis.setStudyGraphUri(analysis.getStudyGraphUri());
        List<Outcome> updatedOutcomes = analysis.getSelectedOutcomes().stream()
                .map(outcome -> {
                  try {
                    return outcomeRepository.get(oldIdToNewOutcomeId.get(outcome.getId()));
                  } catch (ResourceDoesNotExistException e) {
                    e.printStackTrace();
                  }
                  return null;
                })
                .collect(Collectors.toList());
        newAnalysis.updateSelectedOutcomes(updatedOutcomes);
        updateIncludedInterventions(analysis, newAnalysis, oldIdToNewInterventionId);
      } catch (MethodNotAllowedException | ResourceDoesNotExistException e) {
        e.printStackTrace();
      }
    };
  }

  private void updateIncludedInterventions(AbstractAnalysis oldAnalysis, AbstractAnalysis newAnalysis, Map<Integer, Integer> oldIdToNewInterventionId) {
    Set<InterventionInclusion> interventionInclusions = oldAnalysis.getInterventionInclusions().stream()
            .map(inclusion -> new InterventionInclusion(newAnalysis.getId(), oldIdToNewInterventionId.get(inclusion.getInterventionId())))
            .collect(Collectors.toSet());
    newAnalysis.updateIncludedInterventions(interventionInclusions);
  }

  private Consumer<? super NetworkMetaAnalysis> netWorkMetaAnalysisCreator(
          Account user, Project newProject, Map<Integer, Integer> oldToNewAnalysisId,
          Map<Integer, Integer> oldToNewInterventionId, Map<Integer, Integer> oldToNewCovariateId) {
    return oldAnalysis -> {
      AnalysisCommand command = new AnalysisCommand(newProject.getId(), oldAnalysis.getTitle(),
              AnalysisType.EVIDENCE_SYNTHESIS);
      try {
        final NetworkMetaAnalysis newAnalysis = analysisService.createNetworkMetaAnalysis(user, command);
        updateIncludedInterventions(oldAnalysis, newAnalysis, oldToNewInterventionId);
        if (oldAnalysis.getOutcome() != null) {
          newAnalysis.setOutcome(outcomeRepository.get(oldAnalysis.getOutcome().getId()));
        }
        List<CovariateInclusion> updatedCovariateInclusions = oldAnalysis.getCovariateInclusions().stream()
                .map(inclusion -> new CovariateInclusion(newAnalysis.getId(), oldToNewCovariateId.get(inclusion.getCovariateId())))
                .collect(Collectors.toList());
        newAnalysis.updateCovariateInclusions(updatedCovariateInclusions);
        Set<MeasurementMomentInclusion> updatedMMInclusions = oldAnalysis.getIncludedMeasurementMoments().stream()
                .map(inclusion -> new MeasurementMomentInclusion(newAnalysis.getId(), inclusion.getStudy(), inclusion.getMeasurementMoment()))
                .collect(Collectors.toSet());
        newAnalysis.updateMeasurementMomentInclusions(updatedMMInclusions);
        Set<ArmExclusion> updatedArmExclusions = oldAnalysis.getExcludedArms().stream()
                .map(exclusion -> new ArmExclusion(newAnalysis.getId(), exclusion.getTrialverseUid()))
                .collect(Collectors.toSet());
        newAnalysis.updateArmExclusions(updatedArmExclusions);
        oldToNewAnalysisId.put(oldAnalysis.getId(), newAnalysis.getId());
      } catch (ResourceDoesNotExistException | MethodNotAllowedException e) {
        e.printStackTrace();
      }
    };
  }

  @Override
  public Integer createUpdated(Account user, Integer sourceProjectId) throws ResourceDoesNotExistException,
          ReadValueException, URISyntaxException {
    Project sourceProject = projectRepository.get(sourceProjectId);
    ProjectCommand command = sourceProject.getCommand();
    URI datasetUri = URI.create(Namespaces.DATASET_NAMESPACE + sourceProject.getNamespaceUid());
    VersionMapping mapping = versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri);
    String trialverseDatasetUuid = mapping.getVersionedDatasetUri().toString().split("/datasets/")[1];
    URI headVersion = URI.create(triplestoreService.getHeadVersion(mapping.getVersionedDatasetUri()));
    command.setDatasetVersion(headVersion);
    Project newProject = projectRepository.create(user, command);

    //Outcomes
    createOutcomes(user, sourceProjectId, trialverseDatasetUuid, headVersion, newProject);

    //Covariates
    createCovariates(sourceProjectId, trialverseDatasetUuid, headVersion, newProject);

    //Interventions
    createInterventions(user, sourceProjectId, trialverseDatasetUuid, headVersion, newProject);

    return newProject.getId();
  }

  private void createOutcomes(Account user, Integer sourceProjectId, String trialverseDatasetUuid, URI headVersion, Project newProject) throws ReadValueException {
    Collection<Outcome> sourceOutcomes = outcomeRepository.query(sourceProjectId);
    List<SemanticVariable> semanticOutcomes = triplestoreService.getOutcomes(trialverseDatasetUuid, headVersion);

    sourceOutcomes.stream()
            .filter(sourceOutcome ->
                    semanticOutcomes.stream().anyMatch(semanticOutcome -> semanticOutcome.getUri().equals(sourceOutcome.getSemanticOutcomeUri())))
            .forEach(outcomeCreator(user, newProject, new HashMap<>()));
  }

  private Consumer<Outcome> outcomeCreator(Account user, Project newProject, Map<Integer, Integer> oldToNewOutcomeId) {
    return outcome -> {
      SemanticVariable semanticVariable = new SemanticVariable(outcome.getSemanticOutcomeUri(), outcome.getSemanticOutcomeLabel());
      try {
        Outcome newOutcome = outcomeRepository.create(user, newProject.getId(), outcome.getName(), outcome.getDirection(), outcome.getMotivation(), semanticVariable);
        oldToNewOutcomeId.put(outcome.getId(), newOutcome.getId());
      } catch (Exception e) {
        e.printStackTrace();
      }
    };
  }

  private void createCovariates(Integer sourceProjectId, String trialverseDatasetUuid, URI headVersion, Project newProject) throws ReadValueException {
    Collection<Covariate> sourceCovariates = covariateRepository.findByProject(sourceProjectId);
    List<SemanticVariable> semanticCovariates = triplestoreService.getPopulationCharacteristics(trialverseDatasetUuid, headVersion);

    sourceCovariates.stream()
            .filter(sourceCovariate -> sourceCovariate.getType().equals(CovariateOptionType.STUDY_CHARACTERISTIC) ||
                    semanticCovariates.stream()
                            .anyMatch(semanticCovariate -> semanticCovariate.getUri().toString().equals(sourceCovariate.getDefinitionKey())))
            .forEach(covariateCreator(newProject, new HashMap<>()));
  }

  private Consumer<Covariate> covariateCreator(Project newProject, Map<Integer, Integer> oldIdToNewCovariateId) {
    return covariate -> {
      Covariate newCovariate = covariateRepository.createForProject(newProject.getId(), covariate.getDefinitionKey(),
              covariate.getName(), covariate.getMotivation(), covariate.getType());
      oldIdToNewCovariateId.put(covariate.getId(), newCovariate.getId());
    };
  }

  private void createInterventions(Account user, Integer sourceProjectId, String trialverseDatasetUuid, URI headVersion, Project newProject) {
    Set<AbstractIntervention> sourceInterventions = interventionRepository.query(sourceProjectId);
    List<SemanticInterventionUriAndName> semanticInterventions = triplestoreService.getInterventions(trialverseDatasetUuid, headVersion);
    List<URI> unitConcepts = triplestoreService.getUnitUris(trialverseDatasetUuid, headVersion);

    Map<Integer, Integer> oldIdToNewInterventionId = new HashMap<>();

    sourceInterventions.stream()
            .filter(intervention -> intervention instanceof SingleIntervention)
            .map(intervention -> (SingleIntervention) intervention)
            .filter(sourceIntervention -> semanticInterventions.stream().anyMatch(semanticIntervention -> semanticIntervention.getUri().equals(sourceIntervention.getSemanticInterventionUri())))
            .filter(intervention -> checkInterventionUnits(intervention, unitConcepts))
            .forEach(singleInterventionCreator(user, newProject, oldIdToNewInterventionId));

    //combination interventions before intervention sets because intervention sets may contain combination interventions
    sourceInterventions.stream()
            .filter(intervention -> (intervention instanceof CombinationIntervention))
            .map(intervention -> (CombinationIntervention) intervention)
            .filter(intervention -> intervention.getInterventionIds().stream().allMatch(id -> oldIdToNewInterventionId.get(id) != null))
            .forEach(combinationInterventionCreator(user, newProject, oldIdToNewInterventionId));
    // intervention sets
    sourceInterventions.stream()
            .filter(intervention -> (intervention instanceof InterventionSet))
            .map(intervention -> (InterventionSet) intervention)
            .filter(intervention -> intervention.getInterventionIds().stream().allMatch(id -> oldIdToNewInterventionId.get(id) != null))
            .forEach(interventionSetCreator(user, newProject, oldIdToNewInterventionId));
  }

  private Consumer<CombinationIntervention> combinationInterventionCreator(Account user, Project newProject, Map<Integer, Integer> oldIdToNewInterventionId) {
    return intervention -> {
      Set<Integer> updatedInterventionIds = intervention.getInterventionIds().stream()
              .map(oldIdToNewInterventionId::get)
              .collect(Collectors.toSet());
      AbstractInterventionCommand combinationCommand = new CombinationInterventionCommand(newProject.getId(),
              intervention.getName(), intervention.getMotivation(), updatedInterventionIds);
      try {
        AbstractIntervention newIntervention = interventionRepository.create(user, combinationCommand);
        oldIdToNewInterventionId.put(intervention.getId(), newIntervention.getId());
      } catch (MethodNotAllowedException | ResourceDoesNotExistException | InvalidConstraintException e) {
        e.printStackTrace();
      }
    };
  }

  private Consumer<SingleIntervention> singleInterventionCreator(Account user, Project newProject, Map<Integer, Integer> oldIdToNewInterventionId) {
    return intervention -> {
      try {
        AbstractInterventionCommand newInterventionCommand =
                InterventionService.buildSingleInterventionCommand(newProject.getId(), intervention);
        assert newInterventionCommand != null;
        AbstractIntervention newIntervention = interventionRepository.create(user, newInterventionCommand);
        oldIdToNewInterventionId.put(intervention.getId(), newIntervention.getId());
      } catch (InvalidConstraintException | MethodNotAllowedException | ResourceDoesNotExistException e) {
        e.printStackTrace();
      }
    };
  }

  private Consumer<InterventionSet> interventionSetCreator(Account user, Project newProject, Map<Integer, Integer> oldIdToNewInterventionId) {
    return intervention -> {
      Set<Integer> updatedInterventionIds = intervention.getInterventionIds().stream()
              .map(oldIdToNewInterventionId::get)
              .collect(Collectors.toSet());
      AbstractInterventionCommand setCommand = new InterventionSetCommand(newProject.getId(),
              intervention.getName(), intervention.getMotivation(), updatedInterventionIds);
      try {
        interventionRepository.create(user, setCommand);
      } catch (MethodNotAllowedException | ResourceDoesNotExistException | InvalidConstraintException e) {
        e.printStackTrace();
      }
    };
  }

  private Boolean checkInterventionUnits(SingleIntervention intervention, List<URI> unitConcepts) {
    if (intervention instanceof SimpleIntervention) {
      return true; // simple interventions don't have units -> always ok
    }
    if (intervention instanceof FixedDoseIntervention) {
      FixedDoseIntervention cast = (FixedDoseIntervention) intervention;
      return areConstraintUnitsKnown(cast.getConstraint(), unitConcepts);
    }
    if (intervention instanceof TitratedDoseIntervention) {
      TitratedDoseIntervention cast = (TitratedDoseIntervention) intervention;
      return areConstraintUnitsKnown(cast.getMinConstraint(), unitConcepts) && areConstraintUnitsKnown(cast.getMaxConstraint(), unitConcepts);
    }
    if (intervention instanceof BothDoseTypesIntervention) {
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
}
