package org.drugis.trialverse.study;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import lombok.Data;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.joda.time.Period;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@TypeDef(name="interval", typeClass = org.drugis.trialverse.jpa.types.Interval.class)
@JsonAutoDetect(
		fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE,
		isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
@Data public class Epoch {
	@EmbeddedId @JsonIgnore EpochPK epochPK;

	@Column
	@Type(type = "interval")
	@JsonProperty
	private Period duration;

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
