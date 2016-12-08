package org.drugis.addis.outcomes.service.impl;

import org.drugis.addis.analyses.AbstractAnalysis;
import org.drugis.addis.analyses.MetaBenefitRiskAnalysis;
import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.outcomes.service.OutcomeService;
import org.springframework.social.OperationNotPermittedException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by connor on 10-6-16.
 */
@Service
public class OutcomeServiceImpl implements OutcomeService {

  @Inject
  OutcomeRepository outcomeRepository;

  @Inject
  AnalysisRepository analysisRepository;

  @Override
  public Outcome updateOutcome(Integer projectId, Integer outcomeId, String name, String motivation, Integer direction) throws Exception {
    if(outcomeRepository.isExistingOutcomeName(outcomeId, name)){
      throw new Exception("Can not update outcome, outcome name must be unique");
    }
    Outcome outcome = outcomeRepository.get(projectId, outcomeId);
    outcome.setName(name);
    outcome.setMotivation(motivation);

    outcome.setDirection(direction);
    return outcome;
  }

  @Override
  public void delete(Integer projectId, Integer outcomeId) throws ResourceDoesNotExistException {
    List<AbstractAnalysis> analyses = analysisRepository.query(projectId);
    Boolean isOutcomeUsed = analyses.stream()
            .anyMatch(analysis -> {
              if(analysis instanceof NetworkMetaAnalysis) {
                NetworkMetaAnalysis nma = (NetworkMetaAnalysis)analysis;
                return nma.getOutcome() != null && nma.getOutcome().getId().equals(outcomeId);
              } else if (analysis instanceof SingleStudyBenefitRiskAnalysis) {
                SingleStudyBenefitRiskAnalysis ssbr = (SingleStudyBenefitRiskAnalysis) analysis;
                return ssbr.getSelectedOutcomes().stream()
                        .anyMatch(selectedOutcome -> selectedOutcome.getId().equals(outcomeId));
              } else if (analysis instanceof MetaBenefitRiskAnalysis) {
                MetaBenefitRiskAnalysis metabr = (MetaBenefitRiskAnalysis) analysis;
                return metabr.getMbrOutcomeInclusions().stream()
                        .anyMatch(outcomeInclusion -> outcomeInclusion.getOutcomeId().equals(outcomeId));
              }
              return false;
            });
    if (isOutcomeUsed) {
      throw new OperationNotPermittedException("", "attempt to delete outcome that is in use");
    }
    outcomeRepository.delete(outcomeId);
  }
}
