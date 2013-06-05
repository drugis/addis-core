package org.drugis.trialverse.study;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import lombok.Data;

import org.drugis.common.hibernate.PostgresEnumConverter;
import org.drugis.trialverse.concept.Concept;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.hateoas.Identifiable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@TypeDefs({
	@TypeDef(name="blindingType", typeClass=PostgresEnumConverter.class,
			parameters = {@Parameter(name="enumClassName", value="org.drugis.trialverse.study.BlindingType")}),
	@TypeDef(name="allocationType", typeClass=PostgresEnumConverter.class,
		parameters = {@Parameter(name="enumClassName", value="org.drugis.trialverse.study.AllocationType")})
})
@Data public class Study implements Identifiable<Long> {
	@Id @JsonIgnore @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
	@Column private String name;
	@Column private String title;
	@OneToOne @JoinColumn(name = "indication_concept") private Concept indication;
	@Column private String objective;
	@Column private Integer numberOfCenters;
	@Column private String inclusion;
	@Column private String exclusion;
	@Column private Date startDate;
	@Column private Date endDate;
	@Column @Type(type="blindingType")  private BlindingType blindingType;
	@Column @Type(type="allocationType")  private AllocationType allocationType;

	@OneToMany(mappedBy="armPK.studyId") private List<Arm> arms;
	@OneToMany(mappedBy="epochPK.studyId") private List<Epoch> epochs;
	@OneToMany(mappedBy="activityPK.studyId") private List<Activity> activities;
	@OneToMany(mappedBy="designPK.studyId")  private List<Design> designs;
	@OneToMany(mappedBy="studyVariablePK.studyId") private List<StudyVariable> variables;
	@OneToMany(mappedBy="measurementMomentPK.studyId") private List<MeasurementMoment> measurementMoments;
}