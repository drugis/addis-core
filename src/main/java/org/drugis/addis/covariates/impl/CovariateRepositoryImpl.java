package org.drugis.addis.covariates.impl;

import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateOption;
import org.drugis.addis.covariates.CovariateRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collection;

/**
 * Created by connor on 12/2/15.
 */
@Repository
public class CovariateRepositoryImpl implements CovariateRepository {

  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Override
  public Collection<Covariate> findByProject(Integer projectId) {
    TypedQuery<Covariate> query = em.createQuery("FROM Covariate c WHERE c.project = :projectId", Covariate.class);
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }

  @Override
  public Covariate createForProject(Integer projectId, CovariateOption definition, String name, String motivation) {
    Covariate newCovariate = new Covariate(projectId, name, motivation, definition.toString());
    em.persist(newCovariate);
    return newCovariate;
  }
}
