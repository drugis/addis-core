package org.drugis.addis.analyses.repository.impl;

import org.drugis.addis.analyses.AbstractAnalysis;
import org.drugis.addis.analyses.MetaBenefitRiskAnalysis;
import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by daan on 7-5-14.
 */
@Repository
public class AnalysisRepositoryImpl implements AnalysisRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Inject
  NetworkMetaAnalysisRepository networkMetaAnalysisRepository;
  @Inject
  SingleStudyBenefitRiskAnalysisRepository singleStudyBenefitRiskAnalysisRepository;

  @Override
  public AbstractAnalysis get(Integer analysisId) throws ResourceDoesNotExistException {
    TypedQuery<SingleStudyBenefitRiskAnalysis> singleStudyQuery = em.createQuery(
      "FROM SingleStudyBenefitRiskAnalysis a WHERE a.id = :analysisId", SingleStudyBenefitRiskAnalysis.class);
    AbstractAnalysis analysis = findAnalysis(analysisId, singleStudyQuery);
    if (analysis != null) return analysis;

    TypedQuery<NetworkMetaAnalysis> networkMetaAnalysisQuery = em.createQuery(
      "FROM NetworkMetaAnalysis a WHERE a.id = :analysisId", NetworkMetaAnalysis.class);
    AbstractAnalysis networkMetaAnalysisResults = findAnalysis(analysisId, networkMetaAnalysisQuery);
    if (networkMetaAnalysisResults != null) return networkMetaAnalysisResults;

    TypedQuery<MetaBenefitRiskAnalysis> metaBenefitRiskQuery = em.createQuery(
            "FROM metBenefitRiskAnalysis a WHERE a.id = :analysisId", MetaBenefitRiskAnalysis.class);
    AbstractAnalysis metaBenefitRiskResults = findAnalysis(analysisId, metaBenefitRiskQuery);
    if (metaBenefitRiskResults != null) return metaBenefitRiskResults;

    throw new ResourceDoesNotExistException();
  }

  private <T extends AbstractAnalysis> T findAnalysis(Integer analysisId, TypedQuery<T> analysisQuery)  {
    analysisQuery.setParameter("analysisId", analysisId);
    List<T> networkMetaAnalysisResults = analysisQuery.getResultList();

    if(networkMetaAnalysisResults.size() == 1) {
      return networkMetaAnalysisResults.get(0);
    }
    return null;
  }

  @Override
  public List<AbstractAnalysis> query(Integer projectId) {
    Collection<SingleStudyBenefitRiskAnalysis> singleStudyBenefitRiskAnalyses = singleStudyBenefitRiskAnalysisRepository.query(projectId);
    Collection<NetworkMetaAnalysis> networkMetaAnalyses = networkMetaAnalysisRepository.query(projectId);
    List<AbstractAnalysis> analyses = new ArrayList<>();
    analyses.addAll(singleStudyBenefitRiskAnalyses);
    analyses.addAll(networkMetaAnalyses);
    return analyses;
  }

  @Override
  public void setPrimaryModel(Integer analysisId, Integer modelId) {
    networkMetaAnalysisRepository.setPrimaryModel(analysisId, modelId);
  }
}
