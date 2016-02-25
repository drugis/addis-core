package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

/**
 * Created by connor on 6-5-14.
 */
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "analysisType")
@JsonSubTypes({@Type(value = SingleStudyBenefitRiskAnalysis.class, name = AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL),
        @Type(value = NetworkMetaAnalysis.class, name = AnalysisType.NETWORK_META_ANALYSIS_LABEL),
        @Type(value = MetaBenefitRiskAnalysis.class, name = AnalysisType.META_BENEFIT_RISK_ANALYSIS_LABEL)})
public abstract class AbstractAnalysis {
  public abstract Integer getId();
  public abstract Integer getProjectId();
}
