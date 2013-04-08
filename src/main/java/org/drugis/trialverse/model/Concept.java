package org.drugis.trialverse.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

import org.hibernate.annotations.Type;

@Entity
public @Data class Concept {
	@Id @Type(type="pg-uuid") @GeneratedValue(strategy=GenerationType.AUTO) private UUID id;
	@Column private String name;
	@Column private String description;
	@Column private String type;
	@Column private String owner; 
	
	public String getType() {
		return this.type.toLowerCase();
	}
	
}	