package org.drugis.addis.ordering.repository.impl;

import org.drugis.addis.ordering.Ordering;
import org.drugis.addis.ordering.repository.OrderingRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
@Transactional
public class OrderingRepositoryImpl implements OrderingRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  private EntityManager em;

  @Override
  public Ordering get(Integer analysisId) {
    return em.find(Ordering.class, analysisId);
  }

  @Override
  public void put(Integer analysisId, String orderingString) {
    Ordering oldOrder = get(analysisId);
    Ordering ordering = new Ordering(analysisId, orderingString);
    if (oldOrder == null) {
      em.persist(ordering);
    } else {
      em.merge(ordering);
    }
  }

}
