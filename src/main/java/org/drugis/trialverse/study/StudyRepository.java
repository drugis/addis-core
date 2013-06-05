package org.drugis.trialverse.study;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.repository.annotation.RestResource;

@RestResource(path = "studies", rel = "study")
public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryCustom {
	
}	