package org.drugis.addis.remarks.repository.impl;

import org.drugis.addis.remarks.Remarks;
import org.drugis.addis.remarks.repository.RemarksRepository;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Created by daan on 16-9-14.
 */
public class RemarksRepositoryImpl implements RemarksRepository {
    @Qualifier("emAddisCore")
    @PersistenceContext(unitName = "addisCore")
    EntityManager em;

    @Override
    public Remarks get(Integer scenarioId) {
        TypedQuery<Remarks> query = em.createQuery(
                "FROM Remarks r " +
                "WHERE r.scenarioID = :scenarioId", Remarks.class);
        query.setParameter("scenarioId", scenarioId);
        List<Remarks> resultList = query.getResultList();
        return resultList.size() == 0 ? new Remarks() : resultList.get(0);
    }
}
