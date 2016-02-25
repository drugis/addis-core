package org.drugis.addis.analyses.repository.impl;

import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.MetaBenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.MetaBenefitRiskAnalysisRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collection;

/**
 * Created by daan on 25-2-16.
 */
@Repository
public class MetaBenefitRiskAnalysisRepositoryImpl implements MetaBenefitRiskAnalysisRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Override
  public Collection<MetaBenefitRiskAnalysis> queryByProject(Integer projectId) {
    TypedQuery<MetaBenefitRiskAnalysis> query = em.createQuery("FROM MetaBenefitRiskAnalysis " +
            "a WHERE a.projectId = :projectId", MetaBenefitRiskAnalysis.class);
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }

  @Override
  public MetaBenefitRiskAnalysis create(AnalysisCommand analysisCommand) {
    MetaBenefitRiskAnalysis metaBenefitRiskAnalysis = new MetaBenefitRiskAnalysis(analysisCommand.getProjectId(), analysisCommand.getTitle());
    em.persist(metaBenefitRiskAnalysis);
    return metaBenefitRiskAnalysis;
  }
}
