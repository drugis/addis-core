package org.drugis.trialverse.study;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import lombok.Data;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

@Entity
@Data public class Measurement {
	@EmbeddedId @JsonUnwrapped MeasurementPK measurementPK;

	@Column Integer integerValue;
	@Column Double realValue;

	@Data static class MeasurementPK implements Serializable {
		private static final long serialVersionUID = 5206825154379784745L;
		Long studyId;
		@Type(type="pg-uuid") UUID variableConcept;
		String measurementMomentName;
		String armName;
		String attribute;
	}
}
