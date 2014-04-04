package org.drugis.addis.scenarios.repository.impl;

import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by connor on 3-4-14.
 */
@Repository
public class JpaScenarioRepository implements ScenarioRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Override
  public Scenario get(Integer id) {
    return em.find(Scenario.class, id);
  }
}
