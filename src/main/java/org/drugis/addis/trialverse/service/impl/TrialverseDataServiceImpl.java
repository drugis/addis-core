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
  public TrialData getTrialData(Long namespaceId, String outcomeUri) {
    List<Long> studyIds = triplestoreService.findStudiesReferringToConcept(namespaceId, outcomeUri);
    List<Study> studies = trialverseRepository.getStudiesByIds(namespaceId, studyIds);
    return new TrialData(TrialDataStudy.toTrailDataStudy(studies));
  }

  @Override
  public TrialData getTrialData(Long namespaceId) {
    List<Study> studies = trialverseRepository.queryStudies(Long.valueOf(namespaceId));
    return new TrialData(TrialDataStudy.toTrailDataStudy(studies));
  }

  @Override
  public TrialData getTrialData(Long namespaceId, List<String> interventionUris) {
    //todo
    return null;
  }

  @Override
  public TrialData getTrialData(Long namespaceId, String outcomeUri, List<String> interventionUris) {
    System.out.println(" !!!!!!!!!!!!!!!!!!!TrialData getTrialData !!!!!!!!!!!!!!!!!!!");
    List<Long> studyIdsByOutcome = triplestoreService.findStudiesReferringToConcept(namespaceId, outcomeUri);

    Map<Long, List<Pair<Long, String>>> studyInterventionKeys = triplestoreService.findStudyInterventions(namespaceId, studyIdsByOutcome, interventionUris);

    List<Study> studies = trialverseRepository.getStudiesByIds(namespaceId, new ArrayList<>(studyInterventionKeys.keySet()));
    List<TrialDataStudy> trialDataStudies = TrialDataStudy.toTrailDataStudy(studies);

    List<Pair<Long, Long>> studyOutcomeVariableIds = triplestoreService.getOutComeVariableIdsByStudyForSingleOutcome(namespaceId, studyIdsByOutcome, outcomeUri);

    List<TrialDataArm> trailDataArms = trialverseRepository.getArmsForStudies(namespaceId, studyIdsByOutcome);
//    System.out.println(" !!!!!!!!!!!!!!!!!!! ARMS !!!!!!!!!!!!!!!!!!!");
//    for(TrialDataArm a : trailDataArms) {
//      System.out.println(" name: " + a.getName() + "study: " + a.getStudy());
//    }

    List<Measurement> measurements = trialverseRepository.getStudyMeasurementsForOutcomes(studyIdsByOutcome, getRightSideOfPairList(studyOutcomeVariableIds), buildIdIndexedMap(trailDataArms).keySet());
//    System.out.println(" !!!!!!!!!!!!!!!!!!! MEASUREMENTS !!!!!!!!!!!!!!!!!!!");
//    for(Measurement m : measurements) {
//      System.out.println(" attr: " + m.getMeasurementAttribute() + "  int_val: " + m.getIntegerValue() + "  key.study: " + m.getMeasurementKey().getStudyId() + " key.varID: " + m.getMeasurementKey().getVariableId());
//    }

    Map<Long, List<Measurement>> measurementsByArm = sortMeasurementsByArm(measurements);

    for (TrialDataArm trialDataArm : trailDataArms) {
      trialDataArm.setMeasurements(measurementsByArm.get(trialDataArm.getId()));
    }

    Map<Long, List<TrialDataArm>> studyIdToTrialDataArmMap = buildMapOfStudyIdToTrialDataArm(trailDataArms);

    Map<TrialDataStudy, List<Pair<Long, String>>> studyInterventions = new HashMap<>();
    for (TrialDataStudy trialDataStudy : trialDataStudies) {
      trialDataStudy.setTrialDataArms(studyIdToTrialDataArmMap.get(trialDataStudy.getStudyId()));
      studyInterventions.put(trialDataStudy, studyInterventionKeys.get(trialDataStudy.getStudyId()));
    }

    TrialData trialData = new TrialData(studyInterventions);
    return trialData;
  }

  private Map<Long, List<TrialDataArm>> buildMapOfStudyIdToTrialDataArm(List<TrialDataArm> trialDataArms) {
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
