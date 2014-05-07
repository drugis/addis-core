package org.drugis.trialverse.concept;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data public class CodeSystem {
	@Id @Column(name="code_system") private String oid;
	@Column(name="code_system_name") private String name;
}