package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import javax.persistence.*;

/**
 * Created by connor on 6-5-14.
 */
@JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "analysisType")
@JsonSubTypes({@Type(value = SingleStudyBenefitRiskAnalysis.class, name = AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL),
        @Type(value = NetworkMetaAnalysis.class, name = AnalysisType.NETWORK_META_ANALYSIS_LABEL),
        @Type(value = MetaBenefitRiskAnalysis.class, name = AnalysisType.META_BENEFIT_RISK_ANALYSIS_LABEL)})
@Inheritance(strategy = InheritanceType.JOINED)
@Entity
public abstract class AbstractAnalysis {
  @Id
  @SequenceGenerator(name = "analysis_sequence", sequenceName = "shared_analysis_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "analysis_sequence")
  protected Integer id;
  protected Integer projectId;
  protected String title;

  public Integer getId() {
    return id;
  }

  public Integer getProjectId() {
    return projectId;
  }

  public String getTitle() {
    return title;
  }
}
