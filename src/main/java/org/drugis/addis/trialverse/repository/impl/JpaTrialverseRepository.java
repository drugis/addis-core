package org.drugis.addis.trialverse.repository.impl;


import org.drugis.addis.trialverse.Trialverse;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.springframework.stereotype.Repository;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;

/**
 * Created by connor on 2/26/14.
 */
@Repository
public class JpaTrialverseRepository implements TrialverseRepository {

  @PersistenceContext
  EntityManager em;

  @Override
  public Collection<Trialverse> query() {
    return em.createQuery("from Trialverse").getResultList();
  }

  @Override
  public Trialverse get(Integer trialverseId) {
    throw new NotImplementedException();
  }
}
