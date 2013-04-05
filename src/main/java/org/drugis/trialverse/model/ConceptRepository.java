package org.drugis.trialverse.model;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.repository.annotation.RestResource;

@RestResource(path = "concepts", rel = "concepts")
public interface ConceptRepository extends JpaRepository<Concept, UUID> {
	
}