package org.drugis.trialverse.study;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import lombok.Data;

import org.drugis.common.hibernate.PostgresEnumConverter;
import org.drugis.trialverse.concept.Concept;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

@Entity
@TypeDefs({
	@TypeDef(name="measurementType", typeClass=PostgresEnumConverter.class,
			parameters = {@Parameter(name="enumClassName", value="org.drugis.trialverse.study.MeasurementType")}),
	@TypeDef(name="variableType", typeClass=PostgresEnumConverter.class,
		parameters = {@Parameter(name="enumClassName", value="org.drugis.trialverse.study.VariableType")})
})
@Data public class StudyVariable {
	@JsonUnwrapped @EmbeddedId StudyVariablePK studyVariablePK;
	@Column Boolean isPrimary;
	@ManyToOne Concept unitConcept;
	@Column @Type(type="measurementType") MeasurementType measurementType;
	@Column @Type(type="variableType") VariableType variableType;

	@Embeddable
	@Data private static class StudyVariablePK implements Serializable {
		private static final long serialVersionUID = -4052311975385835101L;

		@JsonIgnore Long studyId;
		@ManyToOne Concept variableConcept;
	}
}
