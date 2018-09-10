package org.drugis.addis.projects.service.impl;

import net.minidev.json.JSONObject;
import org.drugis.addis.analyses.model.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.BenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.OperationNotPermittedException;
import org.drugis.addis.exception.ProblemCreationException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.controller.command.AbstractInterventionCommand;
import org.drugis.addis.interventions.controller.command.CombinationInterventionCommand;
import org.drugis.addis.interventions.controller.command.InterventionSetCommand;
import org.drugis.addis.interventions.model.*;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.problems.model.AbstractNetworkMetaAnalysisProblemEntry;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.ProjectCommand;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.scaledUnits.repository.ScaledUnitRepository;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.subProblems.SubProblem;
import org.drugis.addis.subProblems.repository.SubProblemRepository;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  final static Logger logger = LoggerFactory.getLogger(ProjectService.class);

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private AnalysisRepository analysisRepository;

  @Inject
  private AnalysisService analysisService;

  @Inject
  private BenefitRiskAnalysisRepository benefitRiskAnalysisRepository;

  @Inject
  private ProjectRepository projectRepository;

  @Inject
  private TriplestoreService triplestoreService;

  @Inject
  private OutcomeRepository outcomeRepository;

  @Inject
  private InterventionRepository interventionRepository;

  @Inject
  private ScaledUnitRepository scaledUnitRepository;

  @Inject
  private MappingService mappingService;

  @Inject
  private CovariateRepository covariateRepository;

  @Inject
  private VersionMappingRepository versionMappingRepository;

  @Inject
  private ModelRepository modelRepository;

  @Inject
  private SubProblemRepository subProblemRepository;

  @Inject
  private ScenarioRepository scenarioRepository;

  @Inject
  private NetworkMetaAnalysisRepository networkMetaAnalysisRepository;

  @Inject
  private ProblemService problemService;

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
  public List<TrialDataStudy> queryMatchedStudies(Integer projectId) throws ResourceDoesNotExistException, ReadValueException, URISyntaxException, IOException {
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
  public Integer copy(Account user, Integer sourceProjectId, String newTitle) throws ResourceDoesNotExistException, SQLException {
    Project sourceProject = projectRepository.get(sourceProjectId);
    ProjectCommand command = sourceProject.getCommand();
    command.setDatasetVersion(sourceProject.getDatasetVersion());
    command.setName(newTitle);
    Project newProject = projectRepository.create(user, command);

    //outcomes
    Map<Integer, Integer> oldToNewOutcomeId = new HashMap<>();
    Collection<Outcome> sourceOutcomes = outcomeRepository.query(sourceProjectId);
    sourceOutcomes.forEach(outcomeCreator(user, newProject, oldToNewOutcomeId));

    //covariates
    Map<Integer, Integer> oldToNewCovariateId = new HashMap<>();
    Collection<Covariate> sourceCovariates = covariateRepository.findByProject(sourceProjectId);
    sourceCovariates.forEach(covariateCreator(newProject, oldToNewCovariateId));

    //units
    scaledUnitRepository.query(sourceProjectId).forEach(oldUnit -> scaledUnitRepository
            .create(newProject.getId(), oldUnit.getConceptUri(), oldUnit.getMultiplier(), oldUnit.getName()));

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
            .filter(analysis -> analysis instanceof NetworkMetaAnalysis)
            .map(analysis -> (NetworkMetaAnalysis) analysis)
            .forEach(netWorkMetaAnalysisCreator(user, newProject,
                    oldToNewAnalysisId,
                    oldToNewOutcomeId,
                    oldToNewInterventionId,
                    oldToNewCovariateId));


    //models
    Map<Integer, Integer> oldToNewModelId = new HashMap<>();
    Collection<Model> sourceModels = modelRepository.findModelsByProject(sourceProjectId);
    sourceModels.forEach(modelCreator(oldToNewAnalysisId, oldToNewModelId, oldToNewInterventionId));

    //update primary models
    analysisRepository.query(newProject.getId()).stream()
            .filter(analysis -> analysis instanceof NetworkMetaAnalysis)
            .map(analysis -> (NetworkMetaAnalysis) analysis)
            .forEach(nma -> {
              if (nma.getPrimaryModel() != null) {
                nma.setPrimaryModel(oldToNewModelId.get(nma.getPrimaryModel()));
              }
            });

    //mbr analyses
    sourceAnalyses.stream()
            .filter(analysis -> analysis instanceof BenefitRiskAnalysis)
            .map(analysis -> (BenefitRiskAnalysis) analysis)
            .forEach(benefitRiskCreator(user, newProject, oldToNewOutcomeId, oldToNewInterventionId, oldToNewAnalysisId, oldToNewModelId));

    //subProblems
    HashMap<Integer, Integer> oldToNewSubProblemId = new HashMap<>();
    Collection<SubProblem> subProblems = subProblemRepository.queryByProject(sourceProjectId);
    subProblems.forEach(subProblem -> {
      SubProblem newSubProblem = subProblemRepository.create(oldToNewAnalysisId.get(subProblem.getWorkspaceId()),
              subProblem.getDefinition(), subProblem.getTitle());
        oldToNewSubProblemId.put(subProblem.getId(), newSubProblem.getId());
    });

    //scenario's
    Collection<Scenario> sourceScenarios = scenarioRepository.queryByProject(sourceProjectId);
    sourceScenarios.forEach(scenario -> scenarioRepository.create(oldToNewAnalysisId.get(scenario.getWorkspace()),
            oldToNewSubProblemId.get(scenario.getSubProblemId()), scenario.getTitle(), scenario.getState()));

    return newProject.getId();
  }

  private Consumer<BenefitRiskAnalysis> benefitRiskCreator(Account user, Project newProject, Map<Integer, Integer> oldToNewOutcomeId, Map<Integer, Integer> oldToNewInterventionId, Map<Integer, Integer> oldToNewAnalysisId, Map<Integer, Integer> oldToNewModelId) {
    return oldAnalysis -> {
      AnalysisCommand analysisCommand = new AnalysisCommand(newProject.getId(), oldAnalysis.getTitle(),
              AnalysisType.BENEFIT_RISK_ANALYSIS_LABEL);
      try {
        BenefitRiskAnalysis newAnalysis = benefitRiskAnalysisRepository.create(user, analysisCommand);
        em.flush(); // needed to unbuffer the interventioninclusion additions from the constructor
        oldToNewAnalysisId.put(oldAnalysis.getId(), newAnalysis.getId());
        newAnalysis.setProblem(oldAnalysis.getProblem());
        newAnalysis.setFinalized(oldAnalysis.isFinalized());
        updateIncludedInterventions(oldAnalysis, newAnalysis, oldToNewInterventionId);

        List<BenefitRiskNMAOutcomeInclusion> updatedBenefitRiskNMAOutcomeInclusions = oldAnalysis.getBenefitRiskNMAOutcomeInclusions().stream()
                .map(inclusion -> {
                  BenefitRiskNMAOutcomeInclusion newInclusion = new BenefitRiskNMAOutcomeInclusion(newAnalysis.getId(),
                          oldToNewOutcomeId.get(inclusion.getOutcomeId()),
                          oldToNewAnalysisId.get(inclusion.getNetworkMetaAnalysisId()),
                          oldToNewModelId.get(inclusion.getModelId()));
                  newInclusion.setBaseline(inclusion.getBaseline());
                  return newInclusion;
                })
                .collect(Collectors.toList());
        newAnalysis.setBenefitRiskNMAOutcomeInclusions(updatedBenefitRiskNMAOutcomeInclusions);

        List<BenefitRiskStudyOutcomeInclusion> updatedBenefitRiskStudyOutcomeInclusions = oldAnalysis.getBenefitRiskStudyOutcomeInclusions().stream()
                .map(inclusion -> new BenefitRiskStudyOutcomeInclusion(newAnalysis.getId(),
                        oldToNewOutcomeId.get(inclusion.getOutcomeId()),
                        inclusion.getStudyGraphUri()
                ))
                .collect(Collectors.toList());
        newAnalysis.setBenefitRiskStudyOutcomeInclusions(updatedBenefitRiskStudyOutcomeInclusions);
        em.merge(newAnalysis);
      } catch (ResourceDoesNotExistException | MethodNotAllowedException | IOException | SQLException e) {
        e.printStackTrace();
      }
    };
  }

  private Consumer<Model> modelCreator(Map<Integer, Integer> oldIdToNewAnalysisId, Map<Integer, Integer> oldToNewModelId, Map<Integer, Integer> oldToNewInterventionId) {
    return oldModel -> {
      try {
        Model newModel = new Model(oldModel);
        newModel.setAnalysisId(oldIdToNewAnalysisId.get(oldModel.getAnalysisId()));
        newModel = modelRepository.persist(newModel);
        oldToNewModelId.put(oldModel.getId(), newModel.getId());

        JSONObject regressor = newModel.getRegressor();
        if (regressor != null) {
          Integer oldId = Integer.parseInt(regressor.get("control").toString());
          regressor.remove("control");
          regressor.put("control", oldToNewInterventionId.get(oldId).toString());
          newModel.setRegressor(regressor);
        }

        Model.ModelType modelType = newModel.getModelType();
        if (modelType.getType().equals("pairwise") || modelType.getType().equals("node-split")){
          Model.TypeDetails details = modelType.getDetails();
          Model.DetailNode from = details.getFrom();
          Model.DetailNode to = details.getTo();
          newModel.updateTypeDetails(oldToNewInterventionId.get(from.getId()), oldToNewInterventionId.get(to.getId()));
        }
      } catch (InvalidModelException | OperationNotPermittedException e) {
        e.printStackTrace();
      }
    };
  }

  private void updateIncludedInterventions(AbstractAnalysis oldAnalysis, AbstractAnalysis newAnalysis, Map<Integer, Integer> oldIdToNewInterventionId) {
    Set<InterventionInclusion> interventionInclusions = oldAnalysis.getInterventionInclusions().stream()
            .map(inclusion -> new InterventionInclusion(newAnalysis.getId(),
                    oldIdToNewInterventionId.get(inclusion.getInterventionId())))
            .collect(Collectors.toSet());
    newAnalysis.updateIncludedInterventions(interventionInclusions);
  }

  private Consumer<? super NetworkMetaAnalysis> netWorkMetaAnalysisCreator(
          Account user, Project newProject,
          Map<Integer, Integer> oldToNewAnalysisId,
          Map<Integer, Integer> oldToNewOutcomeId,
          Map<Integer, Integer> oldToNewInterventionId,
          Map<Integer, Integer> oldToNewCovariateId) {
    return oldAnalysis -> {
      AnalysisCommand command = new AnalysisCommand(newProject.getId(), oldAnalysis.getTitle(),
              AnalysisType.EVIDENCE_SYNTHESIS);
      try {
        final NetworkMetaAnalysis newAnalysis = analysisService.createNetworkMetaAnalysis(user, command);
        em.flush(); // needed to unbuffer the interventioninclusion additions from the constructor
        updateIncludedInterventions(oldAnalysis, newAnalysis, oldToNewInterventionId);
        if (oldAnalysis.getOutcome() != null) {
          Outcome updatedOutcome = outcomeRepository.get(oldToNewOutcomeId.get(oldAnalysis.getOutcome().getId()));
          newAnalysis.setOutcome(updatedOutcome);
        }
        newAnalysis.setPrimaryModel(oldAnalysis.getPrimaryModel());
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
      ReadValueException, URISyntaxException, SQLException, IOException {
    Project sourceProject = projectRepository.get(sourceProjectId);
    ProjectCommand command = sourceProject.getCommand();
    URI datasetUri = URI.create(Namespaces.DATASET_NAMESPACE + sourceProject.getNamespaceUid());
    VersionMapping mapping = versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri);
    String trialverseDatasetUuid = mapping.getVersionedDatasetUri().toString().split("/datasets/")[1];
    URI headVersion = URI.create(triplestoreService.getHeadVersion(mapping.getVersionedDatasetUri()));
    command.setDatasetVersion(headVersion);
    Project newProject = projectRepository.create(user, command);

    //Outcomes
    Map<Integer, Integer> oldToNewOutcomeId = new HashMap<>();
    createOutcomes(user, sourceProjectId, trialverseDatasetUuid, headVersion, newProject, oldToNewOutcomeId);


    //Covariates
    Map<Integer, Integer> oldToNewCovariateId = new HashMap<>();
    createCovariates(sourceProjectId, trialverseDatasetUuid, headVersion, newProject, oldToNewCovariateId);

    //units
    scaledUnitRepository.query(sourceProjectId).forEach(oldUnit -> scaledUnitRepository
            .create(newProject.getId(), oldUnit.getConceptUri(), oldUnit.getMultiplier(), oldUnit.getName()));

    //Interventions
    Map<Integer, Integer> oldToNewInterventionId = new HashMap<>();
    createInterventions(user, sourceProjectId, trialverseDatasetUuid, headVersion, newProject, oldToNewInterventionId);

    //Analyses
    Map<Integer, Integer> oldToNewAnalysisId = new HashMap<>();
    Collection<AbstractAnalysis> sourceAnalyses = analysisRepository.query(sourceProjectId);
    sourceAnalyses.stream()
            .filter(analysis -> analysis instanceof NetworkMetaAnalysis)
            .filter(analysis -> !analysis.getArchived())
            .map(analysis -> (NetworkMetaAnalysis) analysis)
            .filter(analysis -> checkNetworkMetaAnalysisDependencies(analysis,
                    oldToNewOutcomeId,
                    oldToNewCovariateId,
                    oldToNewInterventionId))
            .forEach(netWorkMetaAnalysisUpdateCreator(user, newProject,
                    oldToNewAnalysisId,
                    oldToNewOutcomeId,
                    oldToNewInterventionId,
                    oldToNewCovariateId));

    //models
    Map<Integer, Integer> oldToNewModelId = new HashMap<>();
    Collection<Model> sourceModels = modelRepository.findModelsByProject(sourceProjectId);
    sourceModels.stream()
            .filter(model -> !model.getArchived())
            .filter(model -> {
              try {
                return checkModelDependencies(model, newProject, sourceProject, oldToNewAnalysisId, oldToNewInterventionId);
              } catch (ResourceDoesNotExistException | UnexpectedNumberOfResultsException | URISyntaxException | SQLException | ReadValueException | IOException | InvalidTypeForDoseCheckException | ProblemCreationException e) {
                e.printStackTrace();
                return false;
              }
            })
            .forEach(modelCreator(oldToNewAnalysisId, oldToNewModelId, oldToNewInterventionId));

    //update primary models
    analysisRepository.query(newProject.getId()).stream()
            .filter(analysis -> analysis instanceof NetworkMetaAnalysis)
            .map(analysis -> (NetworkMetaAnalysis) analysis)
            .forEach(nma -> {
              if (nma.getPrimaryModel() != null) {
                nma.setPrimaryModel(oldToNewModelId.get(nma.getPrimaryModel()));
              }
            });

    return newProject.getId();
  }

  private boolean checkModelDependencies(Model model, Project newProject, Project sourceProject, Map<Integer, Integer> oldToNewAnalysisId, Map<Integer, Integer> oldToNewInterventionId) throws ResourceDoesNotExistException, ReadValueException, URISyntaxException, SQLException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException, IOException, ProblemCreationException {
    NetworkMetaAnalysis newAnalysis;
    NetworkMetaAnalysis oldAnalysis;
    NetworkMetaAnalysisProblem oldProblem;
    NetworkMetaAnalysisProblem newProblem;
    if (oldToNewAnalysisId.get(model.getAnalysisId()) == null) {
      return false;
    } else {
      newAnalysis = (NetworkMetaAnalysis) analysisRepository.get(oldToNewAnalysisId.get(model.getAnalysisId()));
      oldAnalysis = (NetworkMetaAnalysis) analysisRepository.get(model.getAnalysisId());
      // we already know covariates and interventions are alright, otherwise the analysis would not have been copied
    }

    newProblem = (NetworkMetaAnalysisProblem) problemService.getProblem(newProject.getId(), newAnalysis.getId());
    oldProblem = (NetworkMetaAnalysisProblem) problemService.getProblem(sourceProject.getId(), oldAnalysis.getId());

    return areEntriesIdenticalEnough(oldProblem.getEntries(), newProblem.getEntries(), oldToNewInterventionId);
  }

  private boolean isSameEntry(AbstractNetworkMetaAnalysisProblemEntry oldEntry,
                              AbstractNetworkMetaAnalysisProblemEntry newEntry,
                              Map<Integer, Integer> oldToNewInterventionId) {
    return oldToNewInterventionId.get(oldEntry.getTreatment()).equals(newEntry.getTreatment())
            && oldEntry.getStudy().equals(newEntry.getStudy()) && !newEntry.hasMissingValues();
  }

  private boolean areEntriesIdenticalEnough(List<AbstractNetworkMetaAnalysisProblemEntry> oldEntries,
                                            List<AbstractNetworkMetaAnalysisProblemEntry> newEntries,
                                            Map<Integer, Integer> oldToNewInterventionId) {
    // Number of entries must be the same
    if (oldEntries.size() != newEntries.size()) {
      return false;
    }
    // For every old entry a new one must exist.
    for (AbstractNetworkMetaAnalysisProblemEntry oldEntry : oldEntries) {
      if (newEntries.stream().noneMatch(newEntry -> isSameEntry(oldEntry, newEntry, oldToNewInterventionId))) {
        return false;
      }
    }
    return true;
  }

  private Consumer<? super NetworkMetaAnalysis> netWorkMetaAnalysisUpdateCreator(
          Account user, Project newProject, Map<Integer, Integer> oldToNewAnalysisId,
          Map<Integer, Integer> oldToNewOutcomeId, Map<Integer, Integer> oldToNewInterventionId,
          Map<Integer, Integer> oldToNewCovariateId) {
    // can probably use the networkMetaAnalysisCreator once updating of models is implemented
    return oldAnalysis -> {
      AnalysisCommand command = new AnalysisCommand(newProject.getId(), oldAnalysis.getTitle(),
              AnalysisType.EVIDENCE_SYNTHESIS);
      try {
        final NetworkMetaAnalysis newAnalysis = analysisService.createNetworkMetaAnalysis(user, command);
        newAnalysis.setPrimaryModel(oldAnalysis.getPrimaryModel());

        // update interventions
        em.flush(); // needed to unbuffer the intervention inclusion additions from the constructor
        updateIncludedInterventions(oldAnalysis, newAnalysis, oldToNewInterventionId);

        // update outcome
        if (oldAnalysis.getOutcome() != null) {
          Outcome updatedOutcome = outcomeRepository.get(oldToNewOutcomeId.get(oldAnalysis.getOutcome().getId()));
          newAnalysis.setOutcome(updatedOutcome);
        }

        // update covariates
        List<CovariateInclusion> updatedCovariateInclusions = oldAnalysis.getCovariateInclusions().stream()
                .map(inclusion -> new CovariateInclusion(newAnalysis.getId(), oldToNewCovariateId.get(inclusion.getCovariateId())))
                .collect(Collectors.toList());
        newAnalysis.updateCovariateInclusions(updatedCovariateInclusions);

        // update measurement moments
        List<TrialDataStudy> studiesForNewProject = analysisService.buildEvidenceTable(newProject.getId(), oldAnalysis.getId());
        Set<MeasurementMomentInclusion> updatedMMInclusions = oldAnalysis.getIncludedMeasurementMoments().stream()
                .filter(inclusion -> studiesForNewProject.stream()
                        .anyMatch(study -> study.getMeasurementMoments().stream()
                                .anyMatch(measurementMoment -> measurementMoment.getUri().equals(inclusion.getMeasurementMoment()))))
                .map(inclusion -> new MeasurementMomentInclusion(newAnalysis.getId(), inclusion.getStudy(), inclusion.getMeasurementMoment()))
                .collect(Collectors.toSet());
        newAnalysis.updateMeasurementMomentInclusions(updatedMMInclusions);

        // update arm exclusions
        Set<ArmExclusion> updatedArmExclusions = oldAnalysis.getExcludedArms().stream()
                .filter(exclusion -> studiesForNewProject.stream()
                        .anyMatch(study -> study.getTrialDataArms().stream()
                                .anyMatch(trialDataArm -> trialDataArm.getUri().equals(exclusion.getTrialverseUid()))))
                .map(exclusion -> new ArmExclusion(newAnalysis.getId(), exclusion.getTrialverseUid()))
                .collect(Collectors.toSet());
        newAnalysis.updateArmExclusions(updatedArmExclusions);

        oldToNewAnalysisId.put(oldAnalysis.getId(), newAnalysis.getId());
      } catch (ResourceDoesNotExistException | MethodNotAllowedException | ReadValueException | URISyntaxException | IOException e) {
        e.printStackTrace();
      }
    };
  }

  private boolean checkNetworkMetaAnalysisDependencies(NetworkMetaAnalysis analysis,
                                                       Map<Integer, Integer> oldToNewOutcomeId,
                                                       Map<Integer, Integer> oldToNewCovariateId,
                                                       Map<Integer, Integer> oldToNewInterventionId) {
    if (analysis.getOutcome() != null && oldToNewOutcomeId.get(analysis.getOutcome().getId()) == null) {
      return false;
    }
    for (CovariateInclusion covariateInclusion : analysis.getCovariateInclusions()) {
      if (oldToNewCovariateId.get(covariateInclusion.getCovariateId()) == null) {
        return false;
      }
    }
    for (InterventionInclusion interventionInclusion : analysis.getInterventionInclusions()) {
      if (oldToNewInterventionId.get(interventionInclusion.getInterventionId()) == null) {
        return false;
      }
    }
    return true;
  }

  private void createOutcomes(Account user, Integer sourceProjectId, String trialverseDatasetUuid, URI headVersion, Project newProject, Map<Integer, Integer> oldIdToNewCovariateId) throws ReadValueException, IOException {
    Collection<Outcome> sourceOutcomes = outcomeRepository.query(sourceProjectId);
    // List of target outcomes
    List<SemanticVariable> semanticOutcomes = triplestoreService.getOutcomes(trialverseDatasetUuid, headVersion);

    sourceOutcomes.stream()
            .filter(sourceOutcome ->
                    semanticOutcomes.stream().anyMatch(semanticOutcome -> semanticOutcome.getUri().equals(sourceOutcome.getSemanticOutcomeUri())))
            .forEach(outcomeCreator(user, newProject, oldIdToNewCovariateId));
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

  private void createCovariates(Integer sourceProjectId, String trialverseDatasetUuid, URI headVersion, Project newProject, Map<Integer, Integer> oldToNewCovariateId) throws ReadValueException, IOException {
    Collection<Covariate> sourceCovariates = covariateRepository.findByProject(sourceProjectId);
    List<SemanticVariable> semanticCovariates = triplestoreService.getPopulationCharacteristics(trialverseDatasetUuid, headVersion);

    sourceCovariates.stream()
            .filter(sourceCovariate -> sourceCovariate.getType().equals(CovariateOptionType.STUDY_CHARACTERISTIC) ||
                    semanticCovariates.stream()
                            .anyMatch(semanticCovariate -> semanticCovariate.getUri().toString().equals(sourceCovariate.getDefinitionKey())))
            .forEach(covariateCreator(newProject, oldToNewCovariateId));
  }

  private Consumer<Covariate> covariateCreator(Project newProject, Map<Integer, Integer> oldIdToNewCovariateId) {
    return covariate -> {
      Covariate newCovariate = covariateRepository.createForProject(newProject.getId(), covariate.getDefinitionKey(),
              covariate.getName(), covariate.getMotivation(), covariate.getType());
      oldIdToNewCovariateId.put(covariate.getId(), newCovariate.getId());
    };
  }

  private void createInterventions(Account user, Integer sourceProjectId, String trialverseDatasetUuid, URI headVersion, Project newProject, Map<Integer, Integer> oldToNewInterventionId) throws IOException {
    Set<AbstractIntervention> sourceInterventions = interventionRepository.query(sourceProjectId);
    List<SemanticInterventionUriAndName> semanticInterventions = triplestoreService.getInterventions(trialverseDatasetUuid, headVersion);
    List<URI> unitConcepts = triplestoreService.getUnitUris(trialverseDatasetUuid, headVersion);

    sourceInterventions.stream()
            .filter(intervention -> intervention instanceof SingleIntervention)
            .map(intervention -> (SingleIntervention) intervention)
            .filter(sourceIntervention -> semanticInterventions.stream().anyMatch(semanticIntervention -> semanticIntervention.getUri().equals(sourceIntervention.getSemanticInterventionUri())))
            .filter(intervention -> checkInterventionUnits(intervention, unitConcepts))
            .forEach(singleInterventionCreator(user, newProject, oldToNewInterventionId));

    //combination interventions before intervention sets because intervention sets may contain combination interventions
    sourceInterventions.stream()
            .filter(intervention -> (intervention instanceof CombinationIntervention))
            .map(intervention -> (CombinationIntervention) intervention)
            .filter(intervention -> intervention.getInterventionIds().stream().allMatch(id -> oldToNewInterventionId.get(id) != null))
            .forEach(combinationInterventionCreator(user, newProject, oldToNewInterventionId));
    // intervention sets
    sourceInterventions.stream()
            .filter(intervention -> (intervention instanceof InterventionSet))
            .map(intervention -> (InterventionSet) intervention)
            .filter(intervention -> intervention.getInterventionIds().stream().allMatch(id -> oldToNewInterventionId.get(id) != null))
            .forEach(interventionSetCreator(user, newProject, oldToNewInterventionId));
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
        AbstractIntervention newIntervention = interventionRepository.create(user, setCommand);
        oldIdToNewInterventionId.put(intervention.getId(), newIntervention.getId());
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
