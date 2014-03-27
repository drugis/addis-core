package org.drugis.addis.trialverse.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.drugis.addis.trialverse.model.Arm;
import org.drugis.addis.trialverse.model.Measurement;
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
    return objectsToNodes(variableList);
  }

  @Override
  public List<ObjectNode> getArmsByDrugIds(Integer studyId, List<Long> drugIds) {
    List<Arm> arms = trialverseRepository.getArmsByDrugIds(studyId, drugIds);
    return objectsToNodes(arms);
  }

  @Override
  public List<ObjectNode> getOrderedMeasurements(Integer studyId, List<Long> outcomeIds, List<Long> armIds) {
    List<Measurement> measurements = trialverseRepository.getOrderedMeasurements(studyId, outcomeIds, armIds);
    return objectsToNodes(measurements);
  }

  private <T> List<ObjectNode> objectsToNodes(List<T> objectList) {
    final ObjectMapper mapper = new ObjectMapper();
    Collection<ObjectNode> JSONVariables = Collections2.transform(objectList, new Function<T, ObjectNode>() {
      @Override
      public ObjectNode apply(T t) {
        return (ObjectNode) mapper.valueToTree(t);
      }
    });
    return new ArrayList<>(JSONVariables);
  }
}
