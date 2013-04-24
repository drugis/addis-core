package org.drugis.trialverse.study;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data public class Activity {
	@EmbeddedId @JsonIgnore ActivityPK activityPK;

	@OneToMany
	@JoinColumns({
		@JoinColumn(name="activityName"),
		@JoinColumn(name="studyId")
	})
	List<ActivityUsedBy> usedBy;

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
