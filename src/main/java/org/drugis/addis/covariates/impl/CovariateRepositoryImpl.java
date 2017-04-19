package org.drugis.addis.covariates.impl;

import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.trialverse.model.emun.CovariateOptionType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.List;

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
  public Covariate createForProject(Integer projectId, String covariateDefinitionKey, String name, String motivation, CovariateOptionType type) {
    Covariate newCovariate = new Covariate(projectId, name, motivation, covariateDefinitionKey, type);
    em.persist(newCovariate);
    return newCovariate;
  }

  @Override
  public List<Covariate> get(List<String> covariateKeys) {
    return null;
  }

  @Override
  public void delete(Integer covariateId) throws ResourceDoesNotExistException {
    Covariate covariate = em.find(Covariate.class, covariateId);
    if (covariate== null) {
      throw new ResourceDoesNotExistException("No covariate with id " + covariateId);
    }
    em.remove(covariate);
  }
}
