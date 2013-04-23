package org.drugis.trialverse.concept;

import java.util.List;
import java.util.UUID;


public interface ConceptRepositoryCustom {
	public List<Concept> findTreatmentsByIndication(UUID id, String name);
	
	public List<Concept> findVariablesByIndication(UUID id, String name);
}