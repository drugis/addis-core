package org.drugis.addis.trialverse.repository;

import org.drugis.addis.trialverse.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by connor on 2/26/14.
 */
public interface TrialverseRepository {

  public List<Study> queryStudies(String namespaceUid);

  public List<Arm> getArmsByDrugIds(String studyId, Collection<String> drugIds);

  public List<Variable> getVariablesByOutcomeIds(Set<String> outcomeIds);

  public List<Measurement> getOrderedMeasurements(List<String> outcomeUds, List<String> armUids);

  public List<Study> getStudiesByIds(String namespaceUid, List<String> studyUids);

  public List<TrialDataArm> getArmsForStudies(Long namespaceId, List<Long> studyIds, List<Variable> variables);

  public List<Measurement> getStudyMeasurementsForOutcomes(Collection<Long> studyId, Collection<Long> outcomeIds, Collection<Long> arms);
}
