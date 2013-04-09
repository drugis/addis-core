package org.drugis.trialverse.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

@Entity
public @Data class CodeSystem {
	@Id private String codeSystem;
	@Column private String codeSystemName;
}