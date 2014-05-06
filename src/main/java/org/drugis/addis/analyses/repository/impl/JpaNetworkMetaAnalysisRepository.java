package org.drugis.addis.analyses.repository.impl;

import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.Account;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by connor on 6-5-14.
 */
@Repository
public class JpaNetworkMetaAnalysisRepository implements NetworkMetaAnalysisRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Inject
  AnalysisRepositoryUtils analysisRepositoryUtils;

  @Override
  public NetworkMetaAnalysis create(Account user, AnalysisCommand analysisCommand) throws MethodNotAllowedException, ResourceDoesNotExistException {
    NetworkMetaAnalysis networkMetaAnalysis = new NetworkMetaAnalysis(analysisCommand.getProjectId(), analysisCommand.getName());
    analysisRepositoryUtils.checkProjectExistsAndModifiable(user, analysisCommand, em);
    em.persist(networkMetaAnalysis);
    return networkMetaAnalysis;
  }
}
