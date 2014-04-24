package org.drugis.trialverse.study;

import java.net.URI;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Data;

import org.drugis.trialverse.concept.Concept;
import org.drugis.trialverse.core.EntityLinkResolver;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.joda.time.Period;
import org.springframework.hateoas.Identifiable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@TypeDef(name="interval", typeClass = org.drugis.trialverse.jpa.types.Interval.class)
@JsonAutoDetect(
		fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE,
		isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
@Data public class Treatment  implements Identifiable<Long> {

	@JsonIgnore @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private  Long id;
	@JsonIgnore @Column private  Long studyId;
	@JsonIgnore @Column private String activityName;
	@ManyToOne private Concept drugConcept;

	@JoinColumn(name="treatmentId")
	@OneToMany private List<TreatmentDosing> treatmentDosing;

	@Column
	@Type(type = "interval")
	private Period periodicity;

	public URI getDrugConcept() {
		return EntityLinkResolver.getInstance().getLinkForEntity(Concept.class, drugConcept.getId());
	}
}
