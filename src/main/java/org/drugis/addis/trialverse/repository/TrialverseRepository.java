package org.drugis.addis.trialverse.repository;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.trialverse.model.*;

import java.util.Collection;
import java.util.List;

/**
 * Created by connor on 2/26/14.
 */
public interface TrialverseRepository {

  public List<Study> queryStudies(Long namespaceId);

  public List<Arm> getArmsByDrugIds(Integer studyId, Collection<Long> drugIds);

  public List<Variable> getVariablesByOutcomeIds(Collection<Long> outcomeIds);

  public List<Measurement> getOrderedMeasurements(Collection<Long> outcomeIds, Collection<Long> arms);

  public List<Study> getStudiesByIds(String namespaceUid, List<String> studyUids);

  public List<TrialDataArm> getArmsForStudies(Long namespaceId, List<Long> studyIds, List<Variable> variables);

  public List<Measurement> getStudyMeasurementsForOutcomes(Collection<Long> studyId, Collection<Long> outcomeIds, Collection<Long> arms);
}
