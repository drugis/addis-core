package org.drugis.addis.outcomes.service.impl;

import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.outcomes.service.OutcomeService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by connor on 10-6-16.
 */
@Service
public class OutcomeServiceImpl implements OutcomeService {

  @Inject
  OutcomeRepository outcomeRepository;

  @Override
  public Outcome updateNameAndMotivation(Integer projectId, Integer outcomeId, String name, String motivation) throws Exception {
    if(outcomeRepository.isExistingOutcomeName(outcomeId, name)){
      throw new Exception("Can not update outcome, outcome name must be unique");
    }
    Outcome outcome = outcomeRepository.get(projectId, outcomeId);
    outcome.setName(name);
    outcome.setMotivation(motivation);
    return outcome;
  }
}
