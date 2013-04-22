package org.drugis.trialverse.study;

import java.util.List;
import java.util.UUID;

import org.drugis.trialverse.queries.CachedQueryTemplateFactory.QueryTemplate;

public interface StudyRepositoryCustom {
	public List<Study> findStudies(UUID indication, List<UUID> variables, List<UUID> treatments);
}
