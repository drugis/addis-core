package org.drugis.addis.trialverse.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by connor on 25-3-14.
 */
@Service
public class TrialverseServiceImpl implements TrialverseService {

  @Inject
  TrialverseRepository trialverseRepository;

  @Inject
  TriplestoreService triplestoreService;

  final ObjectMapper mapper = new ObjectMapper();

  @Override
  public List<ObjectNode> getVariablesByIds(Set<String> outcomeIds) {
    List<Variable> variableList = trialverseRepository.getVariablesByOutcomeIds(outcomeIds);
    return objectsToNodes(variableList);
  }

  @Override
  public List<ObjectNode> getArmsByDrugIds(String studyUid, Collection<String> drugUids) {
    List<Arm> arms = trialverseRepository.getArmsByDrugIds(studyUid, drugUids);
    return objectsToNodes(arms);
  }

  @Override
  public List<ObjectNode> getOrderedMeasurements(List<String> outcomeIds, List<String> armIds) {
    List<Measurement> measurements = trialverseRepository.getOrderedMeasurements(outcomeIds, armIds);
    return objectsToNodes(measurements);
  }

  @Override
  public List<ObjectNode> getStudiesByIds(String namespaceId, List<String> studyIds) {
    List<Study> studies = trialverseRepository.getStudiesByIds(namespaceId, studyIds);
    return objectsToNodes(studies);
  }

  @Override
  public List<ObjectNode> getTrialData(String namespaceUId, String semanticOutcomeUri, List<String> alternativeUris) {
    List<TrialDataStudy> trialData = triplestoreService.getTrialData(namespaceUId, semanticOutcomeUri, alternativeUris);
    return objectsToNodes(trialData);
  }

  private <T> List<ObjectNode> objectsToNodes(List<T> objectList) {
    Collection<ObjectNode> JSONVariables = Collections2.transform(objectList, new Function<T, ObjectNode>() {
      @Override
      public ObjectNode apply(T t) {
        return (ObjectNode) mapper.valueToTree(t);
      }
    });
    return new ArrayList<>(JSONVariables);
  }
}
