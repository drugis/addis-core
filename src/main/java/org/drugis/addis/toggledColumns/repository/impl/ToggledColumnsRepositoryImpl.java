package org.drugis.addis.toggledColumns.repository.impl;

import org.drugis.addis.ordering.Ordering;
import org.drugis.addis.ordering.repository.OrderingRepository;
import org.drugis.addis.toggledColumns.ToggledColumns;
import org.drugis.addis.toggledColumns.repository.ToggledColumnsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
@Transactional
public class ToggledColumnsRepositoryImpl implements ToggledColumnsRepository{
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  private EntityManager em;

  @Override
  public ToggledColumns get(Integer analysisId) {
    return em.find(ToggledColumns.class, analysisId);
  }

  @Override
  public void put(Integer analysisId, String toggledColumnsString) {
    ToggledColumns oldToggledColumns = get(analysisId);
    ToggledColumns toggledColumns = new ToggledColumns(analysisId, toggledColumnsString);
    if(oldToggledColumns == null){
      em.persist(toggledColumns);
    } else {
      em.merge(toggledColumns);
    }
  }

}
