package org.drugis.addis.trialverse.service.impl;

import org.drugis.addis.trialverse.model.Study;
import org.drugis.addis.trialverse.model.TrialData;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.drugis.addis.trialverse.service.TrialverseDataService;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

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
    // Map<Study, List<Arm>> arms = trialverseRepository.getArms(namespaceId);

    TrialData trialData = new TrialData();
    for (Study study : studies) {

    }
//    trialverseService
    return null;
  }
}
