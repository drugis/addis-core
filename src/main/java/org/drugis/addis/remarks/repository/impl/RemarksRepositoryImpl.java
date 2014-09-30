package org.drugis.addis.remarks.repository.impl;

import org.drugis.addis.remarks.Remarks;
import org.drugis.addis.remarks.repository.RemarksRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by daan on 16-9-14.
 */
@Repository
public class RemarksRepositoryImpl implements RemarksRepository {
    @Qualifier("emAddisCore")
    @PersistenceContext(unitName = "addisCore")
    EntityManager em;

    @Override
    public Remarks find(Integer analysisId) {
        TypedQuery<Remarks> query = em.createQuery(
                "FROM Remarks r " +
                "WHERE r.analysisId= :analysisId", Remarks.class);
        query.setParameter("analysisId", analysisId);
        List<Remarks> resultList = query.getResultList();
        return resultList.size() == 0 ? null : resultList.get(0);
    }

  @Override
  public Remarks update(Remarks remarks) {
    Remarks oldRemark = em.find(Remarks.class, remarks.getAnalysisId());
    oldRemark.setRemarks(remarks.getRemarks());
    return oldRemark;
  }

  @Override
  public Remarks create(Integer analysisId, String remarks) {
    Remarks newRemarks = new Remarks(analysisId, remarks);
    em.persist(newRemarks);
    return newRemarks;
  }
}
