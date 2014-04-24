package org.drugis.trialverse.study;

import java.io.Serializable;
import java.net.URI;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import lombok.Data;

import org.drugis.trialverse.concept.Concept;
import org.drugis.trialverse.core.EntityLinkResolver;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

@Entity
@Data public class Measurement {
	@EmbeddedId @JsonUnwrapped private  MeasurementPK key;

	@Column private Integer integerValue;
	@Column private Double realValue;

	@Data static class MeasurementPK implements Serializable {
		private static final long serialVersionUID = 5206825154379784745L;
		@ManyToOne Concept variableConcept;
		Long studyId;
		String measurementMomentName;
		String armName;
		String attribute;
	}

	public URI getVariableConcept() {
		return EntityLinkResolver.getInstance().getLinkForEntity(Concept.class, key.variableConcept.getId());
	}
}
