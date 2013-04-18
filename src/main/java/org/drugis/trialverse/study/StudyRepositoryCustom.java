package org.drugis.trialverse.study;

import java.util.List;
import java.util.UUID;

public interface StudyRepositoryCustom {
	public List<Study> findStudies(UUID indication, List<UUID> variables, List<UUID> treatments);
}
