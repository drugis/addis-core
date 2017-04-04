package org.drugis.addis.EffectsTable.impl;

import org.apache.jena.atlas.lib.Pair;
import org.drugis.addis.EffectsTable.EffectsTable;
import org.drugis.addis.EffectsTable.EffectsTableRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by joris on 4-4-17.
 */
@Repository
class EffectsTableRepositoryImpl implements EffectsTableRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Override
  public void setEffectsTable(Integer analysisId, Pair<Integer, Boolean> pair) {
    EffectsTable et = em.find(EffectsTable.class, analysisId);
    if (et == null) {
      et = new EffectsTable(analysisId);
      em.persist(et);
    }
    et.setInclusion(pair.getLeft(), pair.getRight());
    em.merge(et);

  }

  @Override
  public EffectsTable getEffectsTable(Integer analysisId) {
    return em.find(EffectsTable.class, analysisId);
  }
}
