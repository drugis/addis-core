package org.drugis.addis.analyses.service.impl;

import org.drugis.addis.analyses.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
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


}
