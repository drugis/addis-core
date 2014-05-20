package org.drugis.addis.trialverse.service.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.drugis.addis.trialverse.service.TrialverseDataService;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by connor on 9-5-14.
 */
@Service
public class TrialverseDataServiceImpl implements TrialverseDataService {

  @Inject
  private TrialverseService trialverseService;

  @Inject
  private TriplestoreService triplestoreService;

  @Inject
  private TrialverseRepository trialverseRepository;

  @Override
  public TrialData getTrialData(Long namespaceId, String outcomeUri, List<String> interventionUris) {
    List<Long> studyIdsByOutcome = triplestoreService.findStudiesReferringToConcept(namespaceId, outcomeUri);

    Map<Long, List<TrialDataIntervention>> studyInterventions = triplestoreService.findStudyInterventions(namespaceId, studyIdsByOutcome, interventionUris);
    List<Study> studies = trialverseRepository.getStudiesByIds(namespaceId, new ArrayList<>(studyInterventions.keySet()));
    List<Pair<Long, Long>> studyOutcomeVariableIds = triplestoreService.getOutcomeVariableIdsByStudyForSingleOutcome(namespaceId, studyIdsByOutcome, outcomeUri);
    List<Variable> variables = trialverseRepository.getVariablesByOutcomeIds(getRightSideOfPairList(studyOutcomeVariableIds));
    List<TrialDataArm> trailDataArms = trialverseRepository.getArmsForStudies(namespaceId, studyIdsByOutcome, variables);
    List<Measurement> measurements = trialverseRepository.getStudyMeasurementsForOutcomes(studyIdsByOutcome, getRightSideOfPairList(studyOutcomeVariableIds), buildIdIndexedMap(trailDataArms).keySet());

    Map<Long, List<Measurement>> measurementsByArm = sortMeasurementsByArm(measurements);

    for (TrialDataArm trialDataArm : trailDataArms) {
      trialDataArm.setMeasurements(measurementsByArm.get(trialDataArm.getId()));
    }

    Map<Long, List<TrialDataArm>> studyIdToTrialDataArmMap = buildStudyIdToTrialDataArmMap(trailDataArms);

    List<TrialDataStudy> trialDataStudies = new ArrayList<>(studies.size());
    for (Study study : studies) {
      Long studyId = study.getId();
      trialDataStudies.add(new TrialDataStudy(study.getId(), study.getName(), studyInterventions.get(studyId), studyIdToTrialDataArmMap.get(studyId)));
    }

    TrialData trialData = new TrialData(trialDataStudies);
    return trialData;
  }

  private Map<Long, List<TrialDataArm>> buildStudyIdToTrialDataArmMap(List<TrialDataArm> trialDataArms) {
    Map<Long, List<TrialDataArm>> map = new HashMap<>();
    for (TrialDataArm trialDataArm : trialDataArms) {
      List<TrialDataArm> arms = map.get(trialDataArm.getStudy());
      if (arms == null) {
        arms = new ArrayList<>();
        map.put(trialDataArm.getStudy(), arms);
      }
      arms.add(trialDataArm);
    }
    return map;
  }

  private Map<Long, TrialDataArm> buildIdIndexedMap(List<TrialDataArm> arms) {
    Map<Long, TrialDataArm> map = new HashMap<>();
    for (TrialDataArm arm : arms) {
      map.put(arm.getId(), arm);
    }
    return map;
  }

  private Map<Long, List<Measurement>> sortMeasurementsByArm(List<Measurement> measurements) {
    Map<Long, List<Measurement>> measurementsByArm = new HashMap<>();
    for (Measurement measurement : measurements) {
      List<Measurement> measurementsInArm = measurementsByArm.get(measurement.getArmId());
      if (measurementsInArm == null) {
        measurementsInArm = new ArrayList<>();
        measurementsByArm.put(measurement.getArmId(), measurementsInArm);
      }
      measurementsInArm.add(measurement);
    }
    return measurementsByArm;
  }

  private <T, O> List<T> getRightSideOfPairList(List<Pair<O, T>> pairList) {
    List<T> rightSizeList = new ArrayList<>(pairList.size());
    for (Pair<O, T> pair : pairList) {
      rightSizeList.add(pair.getRight());
    }
    return rightSizeList;
  }
}
