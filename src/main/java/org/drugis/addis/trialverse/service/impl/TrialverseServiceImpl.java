package org.drugis.addis.trialverse.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.drugis.addis.trialverse.model.TrialDataStudy;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by connor on 25-3-14.
 */
@Service
public class TrialverseServiceImpl implements TrialverseService {

  final ObjectMapper mapper = new ObjectMapper();
  @Inject
  TriplestoreService triplestoreService;

  @Override
  public List<ObjectNode> getTrialData(String namespaceUId, String version, String semanticOutcomeUri, List<URI> alternativeUris, List<String> covariateKeys) throws ReadValueException {
    List<TrialDataStudy> trialData = triplestoreService.getTrialData(namespaceUId, version, semanticOutcomeUri, alternativeUris, covariateKeys);
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
