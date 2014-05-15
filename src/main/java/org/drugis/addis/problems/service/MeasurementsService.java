package org.drugis.addis.problems.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.AlternativeEntry;
import org.drugis.addis.problems.model.Measurement;
import org.drugis.addis.problems.service.impl.PerformanceTableBuilder;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by connor on 28/03/14.
 */
@Service
public class MeasurementsService {

  @Inject
  private TriplestoreService triplestoreService;

  @Inject
  private TrialverseService trialverseService;

  @Inject
  PerformanceTableBuilder performanceTableBuilder;

  private ObjectMapper mapper = new ObjectMapper();


  /**
   * Retrieve a list of measurements from the project namespace, based on selected outcomes.
   */
  public List<Measurement> createMeasurements(Project project, SingleStudyBenefitRiskAnalysis analysis, Map<Long, AlternativeEntry> alternativesCache) {
    Map<String, Outcome> outcomesByUri = new HashMap<>();

    for (Outcome outcome : analysis.getSelectedOutcomes()) {
      outcomesByUri.put(outcome.getSemanticOutcomeUri(), outcome);
    }
    Map<Long, String> trialverseVariables = triplestoreService.getTrialverseVariables(project.getTrialverseId().longValue(), analysis.getStudyId().longValue(), outcomesByUri.keySet());
    List<ObjectNode> jsonMeasurements = trialverseService.getOrderedMeasurements(trialverseVariables.keySet(), alternativesCache.keySet());
    List<Measurement> measurements = new ArrayList<>(jsonMeasurements.size());
    for (ObjectNode measurementJSONNode : jsonMeasurements) {
      Measurement measurement = mapper.convertValue(measurementJSONNode, Measurement.class);
      measurements.add(measurement);
    }

    return measurements;
  }
}
