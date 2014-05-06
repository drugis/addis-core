package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by connor on 6-5-14.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "analysisType")
@JsonSubTypes({@JsonSubTypes.Type(value = SingleStudyBenefitRiskAnalysis.class)})
public class AbstractAnalysis {
}
