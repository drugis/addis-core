package org.drugis.trialverse.study;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="designs")
@Data public class ActivityUsedBy {
	@EmbeddedId @JsonIgnore private ActivityPK activityPK;

	@Embeddable
	@Data private static class ActivityPK implements Serializable {
		static final long serialVersionUID = 5403920072955290731L;
		private Long studyId;
		private String activityName;
		private String armName;
		private String epochName;
 	}

	public String getArmName() {
		return activityPK.armName;
	}

	public String getEpochName() {
		return activityPK.epochName;
	}
}
