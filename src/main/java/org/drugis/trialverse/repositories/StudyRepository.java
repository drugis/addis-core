package org.drugis.trialverse.repositories;

import org.drugis.trialverse.model.Study;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.repository.annotation.RestResource;

@RestResource(path = "studies", rel = "studies")
public interface StudyRepository extends JpaRepository<Study, Long> {
}	