package org.drugis.addis.trialverse.repository.impl;


import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.trialverse.model.Namespace;
import org.drugis.addis.trialverse.model.Study;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.List;

/**
 * Created by connor on 2/26/14.
 */
@Repository
public class JpaTrialverseRepository implements TrialverseRepository {

  @PersistenceContext(unitName = "trialverse")
  @Qualifier("emTrialverse")
  EntityManager em;

  @Override
  public Collection<Namespace> query() {
    return em.createQuery("from Namespace").getResultList();
  }

  @Override
  public Namespace get(Long trialverseId) throws ResourceDoesNotExistException {
    Namespace namespace = em.find(Namespace.class, trialverseId);
    if (namespace == null) {
      throw new ResourceDoesNotExistException();
    }
    return namespace;
  }

  @Override
  public List<Study> queryStudies(Long namespaceId) {
    Query query = em.createNativeQuery("select" +
      " s.id, s.name, s.title " +
      " FROM" +
      " studies s," +
      " namespaces ns," +
      " namespace_studies nss" +
      " WHERE nss.namespace = ns.id " +
      " AND nss.study = s.id" +
      " AND nss.namespace = :namespaceId",
      Study.class);
    query.setParameter("namespaceId", namespaceId);
    return query.getResultList();
  }
}
