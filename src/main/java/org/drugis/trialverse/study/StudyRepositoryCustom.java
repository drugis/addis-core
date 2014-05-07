package org.drugis.trialverse.study;

import java.util.List;
import java.util.UUID;

import org.drugis.trialverse.study.StudyRepositoryImpl.StudyConceptPair;

public interface StudyRepositoryCustom {
	public List<Study> findStudies(final UUID indication, final List<UUID> variables, final List<UUID> treatments);

	public List<StudyConceptPair> findVariableMappings(final List<Long> studies, final UUID variable);
}
