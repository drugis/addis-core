package org.drugis.addis.trialverse.repository.impl;


import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.trialverse.model.Trialverse;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;

/**
 * Created by connor on 2/26/14.
 */
@Repository
public class JpaTrialverseRepository implements TrialverseRepository {

  @PersistenceContext(unitName = "trialverse")
  @Qualifier("emTrialverse")
  EntityManager em;

  @Override
  public Collection<Trialverse> query() {
    return em.createQuery("from Trialverse").getResultList();
  }

  @Override
  public Trialverse get(Long trialverseId) throws ResourceDoesNotExistException {
    Trialverse trialverse = em.find(Trialverse.class, trialverseId);
    if (trialverse == null) {
      throw new ResourceDoesNotExistException();
    }
    return trialverse;
  }
}
