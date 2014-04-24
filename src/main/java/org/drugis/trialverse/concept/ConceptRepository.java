package org.drugis.trialverse.concept;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.repository.annotation.RestResource;

@RestResource(path = "concepts", rel = "concept")
public interface ConceptRepository extends PagingAndSortingRepository<Concept, UUID>, ConceptRepositoryCustom {

	@RestResource(path="type", rel="types")
	public Page<Concept> findByType(@Param("type") final ConceptType type, final Pageable pageable);

	@RestResource(path="typeAndName")
	public Page<Concept> findByTypeAndNameContaining(@Param("type") final ConceptType type, @Param("q") final String name, final Pageable pageable);
}