package org.drugis.trialverse.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Data;

import org.drugis.common.hibernate.PostgresEnumConverter;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@TypeDef(name="conceptType", typeClass=PostgresEnumConverter.class,
		 parameters = {@Parameter(name="enumClassName", value="org.drugis.trialverse.model.ConceptType")})

public @Data class Concept {
	@Id @Type(type="pg-uuid") @GeneratedValue(strategy=GenerationType.IDENTITY) private UUID id;
	@Column private String name;
	@Column private String description;
	@Column @Type(type="conceptType") private ConceptType type;
	@Column private String code;
	@ManyToOne private CodeSystem codeSystem;
	@Column private String owner;
}