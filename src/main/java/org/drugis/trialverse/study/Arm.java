package org.drugis.trialverse.study;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@JsonAutoDetect(
		fieldVisibility = JsonAutoDetect.Visibility.ANY,
		isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@Data public class Arm {
	@EmbeddedId @JsonIgnore ArmPK key;
	@Column Integer armSize;

	@Embeddable
	@Data static class ArmPK implements Serializable {
		static final long serialVersionUID = 5403920072955290731L;
		private String name;
		private Long studyId;
 	}

	public String getName() {
		return key.getName();
	}
}
