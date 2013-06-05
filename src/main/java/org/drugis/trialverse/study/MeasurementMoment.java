package org.drugis.trialverse.study;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

import lombok.Data;

import org.drugis.common.hibernate.PostgresEnumConverter;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.joda.time.Period;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

@Entity
@Data
@TypeDefs({
	@TypeDef(name="epochOffsetType", typeClass=PostgresEnumConverter.class,
			parameters = {@Parameter(name="enumClassName", value="org.drugis.trialverse.study.EpochOffsetType")}),
	@TypeDef(name="interval", typeClass = org.drugis.trialverse.jpa.types.Interval.class)
})
public class MeasurementMoment {
	@EmbeddedId @JsonUnwrapped MeasurementMomentPK measurementMomentPK;
	@Column String epochName;
	@Column Boolean isPrimary;
	@Column @Type(type="epochOffsetType") EpochOffsetType beforeEpoch;

	@Column
	@Type(type = "interval")
	@JsonProperty
	private Period offsetFromEpoch;

	@Data private static class MeasurementMomentPK implements Serializable {
		private static final long serialVersionUID = 189168965179522667L;

		@JsonIgnore Long studyId;
		String name;
	}
}
