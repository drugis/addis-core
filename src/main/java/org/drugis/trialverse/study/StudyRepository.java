package org.drugis.trialverse.study;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.repository.annotation.RestResource;

@RestResource(path = "studies", rel = "study")
public interface StudyRepository extends PagingAndSortingRepository<Study, Long>, StudyRepositoryCustom {
}