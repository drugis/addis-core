package org.drugis.addis.trialverse.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.drugis.addis.trialverse.model.Variable;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by connor on 25-3-14.
 */
@Service
public class TrialverseServiceImpl implements TrialverseService {

  @Inject
  TrialverseRepository trialverseRepository;

  @Override
  public List<ObjectNode> getVariablesByOutcomeIds(List<Long> outcomeIds) {
    List<Variable> variableList = trialverseRepository.getVariablesByOutcomeIds(outcomeIds);
    final ObjectMapper mapper = new ObjectMapper();
    Collection<ObjectNode> JSONVariables = Collections2.transform(variableList, new Function<Variable, ObjectNode>() {
      @Override
      public ObjectNode apply(Variable variable) {
        return (ObjectNode) mapper.valueToTree(variable);
      }
    });
    return new ArrayList<>(JSONVariables);
  }

  @Override
  public List<String> getArmNamesByDrugIds(Integer studyId, List<Long> drugIds) {
    return trialverseRepository.getArmNamesByDrugIds(studyId, drugIds);
  }
}
