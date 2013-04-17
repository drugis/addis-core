package org.drugis.trialverse.concept;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.repository.annotation.RestResource;

@RestResource(path = "concepts", rel = "concept")
public interface ConceptRepository extends JpaRepository<Concept, UUID>, ConceptRepositoryCustom {
	
	@RestResource(path="type", rel="types")
	public Page<Concept> findByType(@Param("type") ConceptType type, Pageable pageable);
	
	@RestResource(path="typeAndName")
	public Page<Concept> findByTypeAndNameContaining(@Param("type") ConceptType type, @Param("q") String name, Pageable pageable);
}