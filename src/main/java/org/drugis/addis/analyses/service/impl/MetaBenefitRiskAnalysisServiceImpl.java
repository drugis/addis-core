package org.drugis.addis.analyses.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.drugis.addis.analyses.InterventionInclusion;
import org.drugis.addis.analyses.MbrOutcomeInclusion;
import org.drugis.addis.analyses.MetaBenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.MetaBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.analyses.service.MetaBenefitRiskAnalysisService;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ProblemCreationException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.problems.model.AbstractProblem;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.subProblems.service.SubProblemService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * Created by connor on 9-3-16.
 */
@Service
public class MetaBenefitRiskAnalysisServiceImpl implements MetaBenefitRiskAnalysisService {

  @Inject
  private AnalysisService analysisService;

  @Inject
  @Lazy
  private MetaBenefitRiskAnalysisRepository metaBenefitRiskAnalysisRepository;

  @Inject
  private OutcomeRepository outcomeRepository;

  @Inject
  private ProblemService problemService;

  @Inject
  private SubProblemService subProblemService;

  @Inject
  private ProjectService projectService;

  @Inject
  private InterventionRepository interventionRepository;



  @Inject
  private ScenarioRepository scenarioRepository;
  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public MetaBenefitRiskAnalysis update(Account user, Integer projectId,
                                        MetaBenefitRiskAnalysis analysis, String scenarioState) throws URISyntaxException, SQLException, IOException, ResourceDoesNotExistException, MethodNotAllowedException, ReadValueException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException, ProblemCreationException {
    MetaBenefitRiskAnalysis storedAnalysis = metaBenefitRiskAnalysisRepository.find(analysis.getId());
    if(storedAnalysis.isFinalized()) {
      throw new MethodNotAllowedException();
    }
    if(analysis.isFinalized()) {
      AbstractProblem problem = problemService.getProblem(projectId, analysis.getId());
      String problemString = objectMapper.writeValueAsString(problem);
      analysis.setProblem(problemString);
      subProblemService.createMCDADefaults(projectId, analysis.getId(), scenarioState);
    }
    return metaBenefitRiskAnalysisRepository.update(user, analysis);
  }

  @Override
  public void checkMetaBenefitRiskAnalysis(Account user, MetaBenefitRiskAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkProjectExistsAndModifiable(user, analysis.getProjectId());
    analysisService.checkProjectIdChange(analysis);

    Set<AbstractIntervention> interventions = interventionRepository.query(analysis.getProjectId());
    Map<Integer, AbstractIntervention> interventionMap = interventions.stream()
            .collect(Collectors.toMap(AbstractIntervention::getId, Function.identity()));

    if (isNotEmpty(analysis.getInterventionInclusions())) {
      // do not allow selection of interventions that are not in the project
      for (InterventionInclusion interventionInclusion : analysis.getInterventionInclusions()) {
        if (!interventionMap.get(interventionInclusion.getInterventionId()).getProject().equals(analysis.getProjectId())) {
          throw new ResourceDoesNotExistException();
        }
      }
    }
    if (isNotEmpty(analysis.getMbrOutcomeInclusions())) {
      // do not allow selection of outcomes that are not in the project
      for (MbrOutcomeInclusion mbrOutcomeInclusion : analysis.getMbrOutcomeInclusions()) {
        Integer outcomeId = mbrOutcomeInclusion.getOutcomeId();
        Outcome outcome = outcomeRepository.get(outcomeId);
        if (!outcome.getProject().equals(analysis.getProjectId())) {
          throw new ResourceDoesNotExistException();
        }
      }
    }
  }

  @Override
  public List<MbrOutcomeInclusion> cleanInclusions(MetaBenefitRiskAnalysis analysis, MetaBenefitRiskAnalysis oldAnalysis) {
    HashSet newInterventionInclusions = new HashSet(analysis.getInterventionInclusions());
    HashSet oldInterventionInclusions = new HashSet(oldAnalysis.getInterventionInclusions());
    if (!newInterventionInclusions.equals(oldInterventionInclusions)) {
      Sets.SetView<InterventionInclusion> difference = Sets.symmetricDifference(Sets.newHashSet(oldAnalysis.getInterventionInclusions()), Sets.newHashSet(analysis.getInterventionInclusions()));
      Integer removedInterventionId = difference.iterator().next().getInterventionId();
      ObjectMapper om = new ObjectMapper();
      return analysis.getMbrOutcomeInclusions().stream().map(moi -> {
        if (moi.getBaseline() != null) {
          try {
            JsonNode baseline = om.readTree(moi.getBaseline());
            String baselineInterventionName = baseline.get("name").asText();
            AbstractIntervention intervention = interventionRepository.getByProjectIdAndName(analysis.getProjectId(), baselineInterventionName);
            if (intervention.getId().equals(removedInterventionId)) {
              moi.setBaseline(null);
            }
          } catch (IOException e) {
            throw new RuntimeException("Attempt to read baseline " + moi.getBaseline());
          }
        }
        return moi;
      }).collect(Collectors.toList());
    }
    return analysis.getMbrOutcomeInclusions();
  }
}
