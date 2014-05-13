package org.drugis.addis.trialverse.service.impl;

import org.drugis.addis.trialverse.model.Study;
import org.drugis.addis.trialverse.model.TrialData;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.drugis.addis.trialverse.service.TrialverseDataService;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
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
    return new TrialData(studies);
  }

  @Override
  public TrialData getTrialData(Long namespaceId) {
    List<Study> studies = trialverseRepository.queryStudies(Long.valueOf(namespaceId));
    return new TrialData(studies);
  }

  @Override
  public TrialData getTrialData(Long namespaceId, List<String> interventionUris) {
    //todo
    return null;
  }

  @Override
  public TrialData getTrialData(Long namespaceId, String outcomeUri, List<String> interventionUris) {
    List<Long> studysByOutcome = triplestoreService.findStudiesReferringToConcept(namespaceId, outcomeUri);
    Map<Long, List<Long>> studyInterventions = triplestoreService.findStudyInterventions(namespaceId, studysByOutcome, interventionUris);
    List<Study> studies = trialverseRepository.getStudiesByIds(namespaceId, new ArrayList<>(studyInterventions.keySet()));
    return new TrialData(studies);
  }
}
