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
  private EntityManager em;

  @Override
  public Scenario get(Integer id) {
    return em.find(Scenario.class, id);
  }

  @Override
  public Scenario create(Integer analysisId, Integer subProblemId, String title, String state) {
    Scenario scenario = new Scenario(analysisId, subProblemId, title, state);
    em.persist(scenario);
    return scenario;
  }

  @Override
  public Collection<Scenario> queryByProject(Integer projectId) {
    TypedQuery<Scenario> query = em.createQuery(
            "SELECT DISTINCT s FROM Scenario s\n" +
                    "where s.workspace in (\n" +
                    "SELECT id FROM AbstractAnalysis where projectid = :projectId)"
            , Scenario.class
    );
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }

  @Override
  public Collection<Scenario> queryByAnalysis(Integer projectId, Integer analysisId) {
    TypedQuery<Scenario> query = em.createQuery(
            "SELECT DISTINCT s FROM Scenario s\n" +
                    "  WHERE s.workspace = :analysisId \n" +
                    "  AND s.workspace in (\n" +
                    "    SELECT id FROM BenefitRiskAnalysis where id = :analysisId and projectId = :projectId\n" +
                    "  )"
            , Scenario.class
    );
    query.setParameter("analysisId", analysisId);
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }

  @Override
  public Collection<Scenario> queryBySubProblem(Integer projectId, Integer analysisId, Integer subProblemId) {
    TypedQuery<Scenario> query = em.createQuery(
            "SELECT DISTINCT s FROM Scenario s\n" +
                    "  WHERE s.workspace = :analysisId \n" +
                    "  AND s.subProblemId = :subProblemId \n" +
                    "  AND s.workspace in (\n" +
                    "    SELECT id FROM BenefitRiskAnalysis where id = :analysisId and projectid = :projectId\n" +
                    "  )"
            , Scenario.class
    );
    query.setParameter("analysisId", analysisId);
    query.setParameter("projectId", projectId);
    query.setParameter("subProblemId", subProblemId);
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
