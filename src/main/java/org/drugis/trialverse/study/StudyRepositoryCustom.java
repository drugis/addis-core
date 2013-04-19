package org.drugis.trialverse.study;

import java.util.List;
import java.util.UUID;

import org.drugis.trialverse.CachedQueryTemplateFactory.QueryTemplate;

public interface StudyRepositoryCustom {
	public List<Study> findStudies(QueryTemplate query, UUID indication, List<UUID> variables, List<UUID> treatments);
}
