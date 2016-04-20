package org.drugis.addis.analyses.service.impl;

import org.apache.commons.lang.NotImplementedException;
import org.drugis.addis.analyses.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.trialdata.AbstractSemanticIntervention;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by daan on 22-5-14.
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {

  @Inject
  AnalysisRepository analysisRepository;

  @Inject
  NetworkMetaAnalysisRepository networkMetaAnalysisRepository;

  @Inject
  SingleStudyBenefitRiskAnalysisRepository singleStudyBenefitRiskAnalysisRepository;

  @Inject
  ProjectService projectService;

  @Inject
  ModelRepository modelRepository;

  @Inject
  OutcomeRepository outcomeRepository;

  @Inject
  InterventionRepository interventionRepository;

  @Inject
  TriplestoreService triplestoreService;

  @Inject
  ProjectRepository projectRepository;

  @Inject
  MappingService mappingService;

  @Inject
  private CovariateRepository covariateRepository;

  @Inject
  private InterventionService interventionService;

  @Override
  public void checkCoordinates(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException {
    AbstractAnalysis analysis = analysisRepository.get(analysisId);
    if (!analysis.getProjectId().equals(projectId)) {
      throw new ResourceDoesNotExistException();
    }
  }

  @Override
  public NetworkMetaAnalysis updateNetworkMetaAnalysis(Account user, NetworkMetaAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException, SQLException {
    projectService.checkProjectExistsAndModifiable(user, analysis.getProjectId());
    checkProjectIdChange(analysis);

    if (!modelRepository.findByAnalysis(analysis.getId()).isEmpty()) {
      // can not update locked exception
      throw new MethodNotAllowedException();
    }

    // do not allow selection of outcome that is not in the project
    if (analysis.getOutcome() != null && !analysis.getOutcome().getProject().equals(analysis.getProjectId())) {
      throw new ResourceDoesNotExistException();
    }

    return networkMetaAnalysisRepository.update(analysis);
  }

  @Override
  public List<MbrOutcomeInclusion> buildInitialOutcomeInclusions(Integer projectId, Integer metabenefitRiskAnalysisId) throws SQLException {
    Collection<Outcome> outcomes = outcomeRepository.query(projectId);
    List<Integer> outcomeIds = outcomes.stream()
            .map(Outcome::getId)
            .collect(Collectors.toList());
    List<NetworkMetaAnalysis> networkMetaAnalyses = networkMetaAnalysisRepository.queryByOutcomes(projectId, outcomeIds);
    List<Model> models = modelRepository.findNetworkModelsByProject(projectId);
    return outcomes.stream()
            .filter(o -> findValidNetworkMetaAnalysis(networkMetaAnalyses, models, o).isPresent())
            .map(o -> {
              NetworkMetaAnalysis validNma = findValidNetworkMetaAnalysis(networkMetaAnalyses, models, o).get();
              return new MbrOutcomeInclusion(metabenefitRiskAnalysisId, o.getId(), validNma.getId(), selectModelId(validNma, models));
            })
            .collect(Collectors.toList());
  }

  private Optional<NetworkMetaAnalysis> findValidNetworkMetaAnalysis(List<NetworkMetaAnalysis> networkMetaAnalyses, List<Model> models, Outcome o) {
    return networkMetaAnalyses
            .stream()
            .filter(nma -> nma.getOutcome() != null && Objects.equals(nma.getOutcome().getId(), o.getId()))
            .filter(nma -> analysisHasModel(models, nma))
            .findFirst();
  }

  private Integer selectModelId(NetworkMetaAnalysis networkMetaAnalysis, List<Model> consistencyModels) {

    List<Model> analysisModels = new ArrayList<>();
    for(Model model  : consistencyModels) {
      if(model.getAnalysisId().equals(networkMetaAnalysis.getId())) {
        analysisModels.add(model);
      }
    }
    if (networkMetaAnalysis.getPrimaryModel() != null) {
      Optional<Model> primaryModel = analysisModels.stream()
              .filter(m -> m.getId().equals(networkMetaAnalysis.getPrimaryModel()))
              .findFirst();
      return primaryModel.get().getId();
    } else {
      return analysisModels.stream()
              .sorted((object1, object2) -> object1.getTitle().compareTo(object2.getTitle()))
              .findFirst().get().getId();
    }
  }

  private boolean analysisHasModel(List<Model> models, NetworkMetaAnalysis nma) {
    return models
            .stream()
            .filter(m -> m.getAnalysisId().equals(nma.getId()))
            .findFirst()
            .isPresent();
  }

  @Override
  public NetworkMetaAnalysis createNetworkMetaAnalysis(Account user, AnalysisCommand analysisCommand) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkProjectExistsAndModifiable(user, analysisCommand.getProjectId());
    return networkMetaAnalysisRepository.create(analysisCommand);
  }

  @Override
  public SingleStudyBenefitRiskAnalysis createSingleStudyBenefitRiskAnalysis(Account user, AnalysisCommand analysisCommand) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkProjectExistsAndModifiable(user, analysisCommand.getProjectId());
    return singleStudyBenefitRiskAnalysisRepository.create(analysisCommand);
  }

  @Override
  public void checkProjectIdChange(AbstractAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException {
    // do not allow changing of project ID
    AbstractAnalysis oldAnalysis = analysisRepository.get(analysis.getId());
    if (!oldAnalysis.getProjectId().equals(analysis.getProjectId())) {
      throw new ResourceDoesNotExistException();
    }
  }

  @Override
  public Map<URI, TrialDataStudy> matchInterventions(Map<URI, TrialDataStudy> studyData, List<AbstractSemanticIntervention> interventions) {
    return null;
  }

  @Override
  public List<AbstractIntervention> getIncludedInterventions(AbstractAnalysis analysis) throws ResourceDoesNotExistException {
    List<Integer> interventionInclusionsIds = analysis.getInterventionInclusions().stream()
            .map(InterventionInclusion::getInterventionId)
            .collect(Collectors.toList());
    return interventionRepository.query(analysis.getProjectId()).stream()
              .filter(i -> interventionInclusionsIds.contains(i.getId()))
              .collect(Collectors.toList());
  }

  private List<Covariate> getIncludedCovariates(NetworkMetaAnalysis analysis) throws ResourceDoesNotExistException {
    List<Integer> includedCovariates = analysis.getCovariateInclusions().stream()
            .map(CovariateInclusion::getCovariateId)
            .collect(Collectors.toList());
    return covariateRepository.findByProject(analysis.getProjectId()).stream()
            .filter(i -> includedCovariates.contains(i.getId()))
            .collect(Collectors.toList());
  }

  @Override
  public List<TrialDataStudy> buildEvidenceTable(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException, ReadValueException, URISyntaxException {

    Project project = projectRepository.get(projectId);
    AbstractAnalysis analysis = analysisRepository.get(analysisId);
    List<AbstractIntervention> includedInterventions = getIncludedInterventions(analysis);
    List<URI> includedInterventionUris = includedInterventions.stream()
            .map(AbstractIntervention::getSemanticInterventionUri)
            .collect(Collectors.toList());

    List<TrialDataStudy> trialData = Collections.emptyList();

    String namespaceUid = mappingService.getVersionedUuid(project.getNamespaceUid());
    String datasetVersion = project.getDatasetVersion();
    if(analysis instanceof NetworkMetaAnalysis) {
      NetworkMetaAnalysis networkMetaAnalysis = (NetworkMetaAnalysis) analysis;
      if(networkMetaAnalysis.getOutcome() == null) {
        // no outcome set, therefore no need to build a evidence table
        return trialData;
      }

      List<String>  includedCovariates = getIncludedCovariates(networkMetaAnalysis).stream()
              .map(Covariate::getDefinitionKey)
              .collect(Collectors.toList());

      trialData = triplestoreService.getTrialData(namespaceUid, datasetVersion,
              networkMetaAnalysis.getOutcome().getSemanticOutcomeUri(), includedInterventionUris, includedCovariates);


    } else if(analysis instanceof SingleStudyBenefitRiskAnalysis) {
      SingleStudyBenefitRiskAnalysis singleStudyBenefitRiskAnalysis = (SingleStudyBenefitRiskAnalysis) analysis;

      List<URI> outcomeUris = singleStudyBenefitRiskAnalysis.getSelectedOutcomes().stream().map(Outcome::getSemanticOutcomeUri).collect(Collectors.toList());

      trialData = triplestoreService.getSingleStudyMeasurements(namespaceUid,
              singleStudyBenefitRiskAnalysis.getStudyGraphUri(), datasetVersion, outcomeUris
               , includedInterventionUris);

    } else {
      throw new NotImplementedException("not yet implemented for other analysis types");
    }

    // add matching data;
    for(TrialDataStudy study: trialData) {
      for(TrialDataArm arm: study.getTrialDataArms()) {
        Optional<AbstractIntervention> matchingIntervention = findMatchingIncludedIntervention(includedInterventions, arm);

        if(matchingIntervention.isPresent()){
          arm.setMatchedProjectInterventionId(matchingIntervention.get().getId());
        }

      }
    }

    return trialData;
  }

  public Optional<AbstractIntervention> findMatchingIncludedIntervention(List<AbstractIntervention> includedInterventions, TrialDataArm arm) {
    return includedInterventions.stream().filter(i -> {
            try {
              return interventionService.isMatched(i, arm.getSemanticIntervention());
            } catch (InvalidTypeForDoseCheckException e) {
              e.printStackTrace();
            }
            return false;
          }).findFirst();
  }
}
