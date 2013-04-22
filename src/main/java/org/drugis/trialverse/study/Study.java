package org.drugis.trialverse.study;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Data;

import org.drugis.trialverse.concept.Concept;
import org.springframework.hateoas.Identifiable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public @Data class Study implements Identifiable<Long> {
	@Id @JsonIgnore @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
	@Column private String name;
	@Column private String title;
	@ManyToOne @JoinColumn(name = "indication_concept") private Concept indication;
	@Column private String objective;	
}