package org.drugis.addis.effectsTables.impl;

import org.drugis.addis.effectsTables.EffectsTableExclusion;
import org.drugis.addis.effectsTables.EffectsTableRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by joris on 4-4-17.
 */
@Repository
class EffectsTableRepositoryImpl implements EffectsTableRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  private EntityManager em;

  @Override
  public void setEffectsTableExclusion(Integer analysisId, Integer alternativeId) {
    TypedQuery<EffectsTableExclusion> query = em.createQuery(
            "FROM EffectsTableExclusion WHERE analysisId = :analysisId AND alternativeId = :alternativeId",
            EffectsTableExclusion.class);
    query.setParameter("analysisId", analysisId);
    query.setParameter("alternativeId",alternativeId);
    List<EffectsTableExclusion> resultList = query.getResultList();
    EffectsTableExclusion effectsTableExclusion = null;
    if(!resultList.isEmpty()){
      effectsTableExclusion = resultList.get(0);
    }

    if (effectsTableExclusion == null) {
      effectsTableExclusion = new EffectsTableExclusion(analysisId, alternativeId);
      em.persist(effectsTableExclusion);
    } else {
      em.remove(effectsTableExclusion);
    }
  }

  @Override
  public List<EffectsTableExclusion> getEffectsTableExclusions(Integer analysisId) {
    TypedQuery<EffectsTableExclusion> query = em.createQuery("FROM EffectsTableExclusion WHERE analysisId = :analysisId", EffectsTableExclusion.class);
    query.setParameter("analysisId", analysisId);
    return query.getResultList();
  }
}
