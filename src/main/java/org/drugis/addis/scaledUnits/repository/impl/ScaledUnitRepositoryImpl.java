package org.drugis.addis.scaledUnits.repository.impl;

import org.drugis.addis.scaledUnits.ScaledUnit;
import org.drugis.addis.scaledUnits.repository.ScaledUnitRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.net.URI;
import java.util.List;

/**
 * Created by joris on 19-4-17.
 */
@Repository
public class ScaledUnitRepositoryImpl implements ScaledUnitRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  private EntityManager em;

  @Override
  public List<ScaledUnit> query(Integer projectId) {
    TypedQuery<ScaledUnit> query = em
            .createQuery("FROM ScaledUnit su WHERE su.projectId = :projectId", ScaledUnit.class);
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }

  @Override
  public ScaledUnit create(Integer projectId, URI conceptUri, Double multiplier, String name) {
    ScaledUnit newUnit = new ScaledUnit(projectId, conceptUri, multiplier, name);
    em.persist(newUnit);
    return newUnit;
  }
}
