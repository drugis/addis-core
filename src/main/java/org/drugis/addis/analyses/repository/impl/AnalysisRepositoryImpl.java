package org.drugis.addis.analyses.repository.impl;

import org.drugis.addis.analyses.AbstractAnalysis;
import org.drugis.addis.analyses.MetaBenefitRiskAnalysis;
import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.MetaBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
  @Lazy
  NetworkMetaAnalysisRepository networkMetaAnalysisRepository;
  @Inject
  @Lazy
  SingleStudyBenefitRiskAnalysisRepository singleStudyBenefitRiskAnalysisRepository;
  @Inject
  @Lazy
  MetaBenefitRiskAnalysisRepository metaBenefitRiskAnalysisRepository;

  @Override
  public AbstractAnalysis get(Integer analysisId) throws ResourceDoesNotExistException {
    SingleStudyBenefitRiskAnalysis analysis = em.find(SingleStudyBenefitRiskAnalysis.class, analysisId);
    if (analysis != null) return analysis;

    NetworkMetaAnalysis networkMetaAnalysisResults = em.find(NetworkMetaAnalysis.class, analysisId);
    if (networkMetaAnalysisResults != null) return networkMetaAnalysisResults;

    MetaBenefitRiskAnalysis metaBenefitRiskanalysis = em.find(MetaBenefitRiskAnalysis.class, analysisId);
    if (metaBenefitRiskanalysis != null) return metaBenefitRiskanalysis;

    throw new ResourceDoesNotExistException();
  }

  @Override
  public List<AbstractAnalysis> query(Integer projectId) {
    Collection<SingleStudyBenefitRiskAnalysis> singleStudyBenefitRiskAnalyses = singleStudyBenefitRiskAnalysisRepository.query(projectId);
    Collection<NetworkMetaAnalysis> networkMetaAnalyses = networkMetaAnalysisRepository.query(projectId);
    Collection<MetaBenefitRiskAnalysis> metaBenefitRiskAnalyses = metaBenefitRiskAnalysisRepository.queryByProject(projectId);
    List<AbstractAnalysis> analyses = new ArrayList<>();
    analyses.addAll(singleStudyBenefitRiskAnalyses);
    analyses.addAll(networkMetaAnalyses);
    analyses.addAll(metaBenefitRiskAnalyses);
    return analyses;
  }

  @Override
  public void setPrimaryModel(Integer analysisId, Integer modelId) {
    networkMetaAnalysisRepository.setPrimaryModel(analysisId, modelId);
  }
}
