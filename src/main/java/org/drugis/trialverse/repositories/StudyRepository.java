package org.drugis.trialverse.repositories;

import org.drugis.trialverse.model.Study;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.repository.annotation.RestResource;

@RestResource(path = "studies", rel = "study")
public interface StudyRepository extends CrudRepository<Study, Long> {
}	