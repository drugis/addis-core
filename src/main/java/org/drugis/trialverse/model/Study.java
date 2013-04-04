package org.drugis.trialverse.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Study {
	@Id Long id;
	@Column String name;
	@Column String title;
}