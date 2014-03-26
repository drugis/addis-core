package org.drugis.addis.trialverse.service.impl;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.drugis.addis.trialverse.model.Variable;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.json.JSONObject;
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
  public List<JSONObject> getVariablesByOutcomeIds(List<Long> outcomeIds) {
    List<Variable> variableList = trialverseRepository.getVariablesByOutcomeIds(outcomeIds);
    Collection<JSONObject> JSONVariables = Collections2.transform(variableList, new Function<Variable, JSONObject>() {
      @Override
      public JSONObject apply(Variable variable) {
        return new JSONObject(variable);
      }
    });
    return new ArrayList<>(JSONVariables);
  }
}
