package org.drugis.trialverse.study;

import java.io.Serializable;
import java.net.URI;
import java.sql.Time;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import lombok.Data;

import org.drugis.trialverse.concept.Concept;
import org.drugis.trialverse.core.EntityLinkResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

@Entity
@Data class TreatmentDosing {
	@JsonUnwrapped @EmbeddedId private TreatmentDosingPK key;
	@Column private Double minDose;
	@Column private Double maxDose;
	@Column private String scaleModifier;
	@ManyToOne private Concept unitConcept;

	@Embeddable
	@Data private static class TreatmentDosingPK implements Serializable {
		private static final long serialVersionUID = 3201662940151856371L;
		@JsonIgnore Long treatmentId;
		Time plannedTime;
	}

	public URI getUnitConcept() {
		return EntityLinkResolver.getInstance().getLinkForEntity(Concept.class, unitConcept.getId());
	}
}