package org.drugis.trialverse.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@TypeDef(name="conceptTypeEnum", typeClass=ConceptTypeEnumConverter.class,
		 parameters = {@Parameter(name="enumClassName", value="org.drugis.trialverse.model.ConceptType")})
public @Data class Concept {
	@Id @Type(type="pg-uuid") @GeneratedValue(strategy=GenerationType.AUTO) private UUID id;
	@Column private String name;
	@Column private String description;
	@Column @Type(type="conceptTypeEnum") private ConceptType type;
	@Column private String owner; 
}