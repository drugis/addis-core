package org.drugis.trialverse.study;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;

import lombok.Data;

import org.drugis.common.hibernate.PostgresEnumConverter;
import org.drugis.trialverse.study.types.ActivityType;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@JsonAutoDetect(
		fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE,
		isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
@TypeDef(name="activityType", typeClass=PostgresEnumConverter.class,
		 parameters = {@Parameter(name="enumClassName", value="org.drugis.trialverse.study.types.ActivityType")})
@Data public class Activity {
	@EmbeddedId @JsonIgnore private  ActivityPK key;
	@Column @Type(type="activityType") private  ActivityType type;

	@Embeddable
	@Data private static class ActivityPK implements Serializable {
		static final long serialVersionUID = 5403920072955290731L;
		private Long studyId;
		private String name;
 	}

	@JoinColumns({
		@JoinColumn(name="activityName"),
		@JoinColumn(name="studyId")
	})
	@OneToMany private List<Treatment> treatment;

	public String getName() {
		return key.getName();
	}
}
