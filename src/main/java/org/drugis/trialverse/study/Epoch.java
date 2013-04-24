package org.drugis.trialverse.study;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data public class Epoch {
	@EmbeddedId @JsonIgnore EpochPK epochPK;

	@Embeddable
	@Data private static class EpochPK implements Serializable {
		static final long serialVersionUID = 5403920072955290731L;
		private String name;
		private Long studyId;
 	}

	public String getName() {
		return epochPK.getName();
	}
}
