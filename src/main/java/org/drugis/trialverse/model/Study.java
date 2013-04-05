package org.drugis.trialverse.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
public @Data class Study {
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
	@Column private String name;
	@Column private String title;
}