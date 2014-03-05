package org.drugis.addis.outcomes.repository.impl;

import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;

/**
 * Created by daan on 3/5/14.
 */
@Repository
public class JpaOutcomeRepository implements OutcomeRepository {

  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Override
  public Collection<Outcome> query(Integer projectId) {
    return em.createQuery("from Outcome").getResultList();
  }
}
