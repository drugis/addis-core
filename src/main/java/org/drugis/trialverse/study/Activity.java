package org.drugis.trialverse.study;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data public class Activity {
	@EmbeddedId @JsonIgnore ActivityPK activityPK;

	@Embeddable
	@Data private static class ActivityPK implements Serializable {
		static final long serialVersionUID = 5403920072955290731L;
		private Long studyId;
		private String name;
 	}

	public String getName() {
		return activityPK.getName();
	}
}
