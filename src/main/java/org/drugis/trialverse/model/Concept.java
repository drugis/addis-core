package org.drugis.trialverse.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.type.EnumType;

@Entity
@TypeDef(name="conceptTypeEnum", typeClass=ConceptTypeEnumConverter.class)
public @Data class Concept {
	@Id @Type(type="pg-uuid") @GeneratedValue(strategy=GenerationType.AUTO) private UUID id;
	@Column private String name;
	@Column private String description;
	@Column @Type(type="conceptTypeEnum") private ConceptType type;
	@Column private String owner; 
}