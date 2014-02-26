package org.drugis.addis.trialverse.repository;

import org.drugis.addis.trialverse.Trialverse;

import java.util.Collection;

/**
 * Created by connor on 2/26/14.
 */
public interface TrialverseRepository {
  public Collection<Trialverse> query();

  public Trialverse get(Integer trialverseId);
}
