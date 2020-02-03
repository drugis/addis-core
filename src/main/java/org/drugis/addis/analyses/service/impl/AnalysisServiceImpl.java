package org.drugis.addis.analyses.service.impl;

import org.apache.commons.lang3.NotImplementedException;
import org.drugis.addis.analyses.model.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.BenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.CombinationIntervention;
import org.drugis.addis.interventions.model.InterventionSet;
import org.drugis.addis.interventions.model.SingleIntervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalysisServiceImpl implements AnalysisService {

  @Inject
  private AnalysisRepository analysisRepository;

  @Inject
  private NetworkMetaAnalysisRepository networkMetaAnalysisRepository;

  @Inject
  private BenefitRiskAnalysisRepository benefitRiskAnalysisRepository;

  @Inject
  private ProjectService projectService;

  @Inject
  private ModelService modelService;

  @Inject
  private OutcomeRepository outcomeRepository;

  @Inject
  private InterventionRepository interventionRepository;

  @Inject
  private TriplestoreService triplestoreService;

  @Inject
  private ProjectRepository projectRepository;

  @Inject
  private MappingService mappingService;

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
  public NetworkMetaAnalysis updateNetworkMetaAnalysis(Account user, NetworkMetaAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException, SQLException, IOException {
    projectService.checkProjectExistsAndModifiable(user, analysis.getProjectId());
    checkProjectIdChange(analysis);

    if (!modelService.findByAnalysis(analysis.getId()).isEmpty()) {
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
  public NetworkMetaAnalysis createNetworkMetaAnalysis(Account user, AnalysisCommand analysisCommand) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkProjectExistsAndModifiable(user, analysisCommand.getProjectId());
    return networkMetaAnalysisRepository.create(analysisCommand);
  }

  @Override
  public BenefitRiskAnalysis createBenefitRiskAnalysis(Account user, AnalysisCommand analysisCommand) throws MethodNotAllowedException, SQLException, IOException, ResourceDoesNotExistException {
    projectService.checkProjectExistsAndModifiable(user, analysisCommand.getProjectId());
    return benefitRiskAnalysisRepository.create(user, analysisCommand);
  }

  @Override
  public void checkProjectIdChange(AbstractAnalysis analysis) throws ResourceDoesNotExistException {
    // do not allow changing of project ID
    AbstractAnalysis oldAnalysis = analysisRepository.get(analysis.getId());
    if (!oldAnalysis.getProjectId().equals(analysis.getProjectId())) {
      throw new ResourceDoesNotExistException();
    }
  }

  @Override
  public Set<AbstractIntervention> getIncludedInterventions(AbstractAnalysis analysis) {
    Set<Integer> interventionInclusionsIds = analysis.getInterventionInclusions().stream()
            .map(InterventionInclusion::getInterventionId)
            .collect(Collectors.toSet());
    return interventionRepository.query(analysis.getProjectId()).stream()
            .filter(i -> interventionInclusionsIds.contains(i.getId()))
            .collect(Collectors.toSet());
  }

  private List<Covariate> getIncludedCovariates(NetworkMetaAnalysis analysis) {
    List<Integer> includedCovariates = analysis.getCovariateInclusions().stream()
            .map(CovariateInclusion::getCovariateId)
            .collect(Collectors.toList());
    return covariateRepository.findByProject(analysis.getProjectId()).stream()
            .filter(i -> includedCovariates.contains(i.getId()))
            .collect(Collectors.toList());
  }

  @Override
  public List<TrialDataStudy> buildEvidenceTable(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException, ReadValueException, IOException {
    Project project = projectRepository.get(projectId);
    AbstractAnalysis analysis = analysisRepository.get(analysisId);
    if (!(analysis instanceof NetworkMetaAnalysis)) {
      throw new NotImplementedException("not implemented for other analysis types");
    }
    NetworkMetaAnalysis networkMetaAnalysis = (NetworkMetaAnalysis) analysis;
    if (networkMetaAnalysis.getOutcome() == null) {
      // no outcome set, therefore no need to build a evidence table
      return Collections.emptyList();
    }

    String namespaceUid = mappingService.getVersionedUuid(project.getNamespaceUid());
    URI datasetVersion = project.getDatasetVersion();

    return getTrialData(networkMetaAnalysis, namespaceUid, datasetVersion);
  }

  private List<TrialDataStudy> getTrialData(NetworkMetaAnalysis networkMetaAnalysis, String namespaceUid, URI datasetVersion) throws ReadValueException, IOException, ResourceDoesNotExistException {
    Set<AbstractIntervention> includedInterventions = getIncludedInterventions(networkMetaAnalysis);
    Set<SingleIntervention> singleInterventions = getSingleInterventions(includedInterventions);
    Set<URI> includedInterventionUris = singleInterventions.stream()
            .map(SingleIntervention::getSemanticInterventionUri)
            .collect(Collectors.toSet());

    Set<String> includedCovariates = getIncludedCovariates(networkMetaAnalysis).stream()
            .map(Covariate::getDefinitionKey)
            .collect(Collectors.toSet());

    List<TrialDataStudy> studies = triplestoreService.getNetworkData(
            namespaceUid,
            datasetVersion,
            networkMetaAnalysis.getOutcome().getSemanticOutcomeUri(),
            includedInterventionUris,
            includedCovariates);

    studies = triplestoreService.addMatchingInformation(includedInterventions, studies);
    return filterUnmatchedArms(studies);
  }

  private List<TrialDataStudy> filterUnmatchedArms(List<TrialDataStudy> studies) {
    return studies.stream().filter(study -> {
      int nMatchedInterventions = study.getArms().stream()
              .mapToInt(arm -> arm.getMatchedProjectInterventionIds().size())
              .sum();
      return nMatchedInterventions > 0;
    }).collect(Collectors.toList());
  }

  @Override
  public Set<SingleIntervention> getSingleInterventions(Set<AbstractIntervention> includedInterventions) throws ResourceDoesNotExistException {
    Set<SingleIntervention> singleInterventions = includedInterventions.stream()
            .filter(abstractIntervention -> abstractIntervention instanceof SingleIntervention)
            .map(abstractIntervention -> (SingleIntervention) abstractIntervention)
            .collect(Collectors.toSet());

    List<CombinationIntervention> combinationInterventions = includedInterventions.stream()
            .filter(abstractIntervention -> abstractIntervention instanceof CombinationIntervention)
            .map(abstractIntervention -> (CombinationIntervention) abstractIntervention)
            .collect(Collectors.toList());

    List<InterventionSet> interventionSets = includedInterventions.stream()
            .filter(abstractIntervention -> abstractIntervention instanceof InterventionSet)
            .map(abstractIntervention -> (InterventionSet) abstractIntervention)
            .collect(Collectors.toList());

    singleInterventions.addAll(interventionService.resolveCombinations(combinationInterventions));
    singleInterventions.addAll(interventionService.resolveInterventionSets(interventionSets));
    return singleInterventions;
  }
}
