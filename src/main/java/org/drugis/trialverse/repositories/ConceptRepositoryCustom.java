package org.drugis.trialverse.repositories;

import java.util.List;
import java.util.UUID;

import org.drugis.trialverse.model.Concept;

public interface ConceptRepositoryCustom {
	public List<Concept> findTreatmentsByIndication(UUID id);
}