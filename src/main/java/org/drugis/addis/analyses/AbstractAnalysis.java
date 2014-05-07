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
@JsonSubTypes({@Type(value = SingleStudyBenefitRiskAnalysis.class, name = "Single-study Benefit-Risk"),
               @Type(value = NetworkMetaAnalysis.class, name = "Network meta-analysis")})
public abstract class AbstractAnalysis {

}
