package org.drugis.trialverse.study;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="designs")
@Data public class Design {
	@EmbeddedId @JsonIgnore DesignPK designPK;
	@Column String activityName;

	@Embeddable
	@Data private static class DesignPK implements Serializable {
		private static final long serialVersionUID = -2803631081517571717L;
		private String armName;
		private String epochName;
		private Long studyId;
 	}

	public String getArmName() {
		return designPK.getArmName();
	}

	public String getEpochName() {
		return designPK.getEpochName();
	}
}
