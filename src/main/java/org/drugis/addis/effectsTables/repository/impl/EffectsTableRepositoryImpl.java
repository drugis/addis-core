package org.drugis.addis.effectsTables.repository.impl;

import org.drugis.addis.effectsTables.EffectsTableAlternativeInclusion;
import org.drugis.addis.effectsTables.repository.EffectsTableRepository;
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
  public void setEffectsTableAlternativeInclusion(Integer analysisId, List<String> alternativeIds) {
    List<EffectsTableAlternativeInclusion> resultList = getEffectsTableAlternativeInclusions(analysisId);

    for (String alternativeId : alternativeIds) {
      boolean alreadyIncluded = false;
      for (EffectsTableAlternativeInclusion effectsTableAlternativeInclusion : resultList) {
        // Look if it is already in the db
        if (effectsTableAlternativeInclusion.getAlternativeId().equals(alternativeId)) {
          alreadyIncluded = true;
          resultList.remove(effectsTableAlternativeInclusion);
          break;
        }
      }
      if (!alreadyIncluded) {
        //do nothing is it is already in the db, otherwise add it
        em.persist(new EffectsTableAlternativeInclusion(analysisId, alternativeId));
      }
    }
    for(EffectsTableAlternativeInclusion effectsTableAlternativeInclusion : resultList){
      // remove everything from the db that is not on the new list
      em.remove(effectsTableAlternativeInclusion);
    }
  }

  @Override
  public List<EffectsTableAlternativeInclusion> getEffectsTableAlternativeInclusions(Integer analysisId) {
    TypedQuery<EffectsTableAlternativeInclusion> query = em.createQuery("FROM EffectsTableAlternativeInclusion WHERE analysisId = :analysisId", EffectsTableAlternativeInclusion.class);
    query.setParameter("analysisId", analysisId);
    return query.getResultList();
  }
}
