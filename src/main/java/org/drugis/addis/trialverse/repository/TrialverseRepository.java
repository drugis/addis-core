package org.drugis.addis.trialverse.repository;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.trialverse.model.*;

import java.util.Collection;
import java.util.List;

/**
 * Created by connor on 2/26/14.
 */
public interface TrialverseRepository {
  public Collection<Namespace> query();

  public Namespace get(Long trialverseId) throws ResourceDoesNotExistException;

  public List<Study> queryStudies(Long namespaceId);

  public List<Arm> getArmsByDrugIds(Integer studyId, Collection<Long> drugIds);

  public List<Variable> getVariablesByOutcomeIds(Collection<Long> outcomeIds);

  List<Measurement> getOrderedMeasurements(Integer studyId, Collection<Long> outcomeIds, Collection<Long> arms);
}
