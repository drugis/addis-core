package org.drugis.addis.analyses.repository;

import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.MetaBenefitRiskAnalysis;

import java.util.Collection;

/**
 * Created by daan on 25-2-16.
 */
public interface MetaBenefitRiskAnalysisRepository {
  Collection<MetaBenefitRiskAnalysis> queryByProject(Integer projectId);

  MetaBenefitRiskAnalysis create(AnalysisCommand analysisCommand);
}
