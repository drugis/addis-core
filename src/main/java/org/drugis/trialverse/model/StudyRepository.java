package org.drugis.trialverse.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.repository.annotation.RestResource;

@RestResource(path = "studies", rel = "studies")
public interface StudyRepository extends CrudRepository<Study, Long> {

}
