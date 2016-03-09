package org.drugis.addis.scenarios.repository.impl;

import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collection;

/**
 * Created by connor on 3-4-14.
 */
@Repository
public class ScenarioRepositoryImpl implements ScenarioRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Override
  public Scenario get(Integer id) {
    return em.find(Scenario.class, id);
  }

  @Override
  public Scenario create(Integer analysisId, String title, String state) {
    Scenario scenario = new Scenario(analysisId, title, state);
    em.persist(scenario);
    return scenario;
  }

  @Override
  public Collection<Scenario> query(Integer projectId, Integer analysisId) {
    TypedQuery<Scenario> query = em.createQuery(
            "SELECT DISTINCT s FROM Scenario s, SingleStudyBenefitRiskAnalysis ssbra, MetaBenefitRiskAnalysis metabra " +
                    "WHERE s.workspace = :analysisId " +
                    "AND ((ssbra.id = :analysisId AND ssbra.projectId = :projectId) OR " +
                    "(metabra.id = :analysisId AND metabra.projectId = :projectId))"
                    , Scenario.class
    );
    query.setParameter("analysisId", analysisId);
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }

  @Override
  public Scenario update(Integer id, String title, String state) {
    Scenario scenario = em.find(Scenario.class, id);
    scenario.setTitle(title);
    scenario.setState(state);
    return scenario;
  }

}
