package org.drugis.trialverse.concept;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
@Component
public class ConceptRepositoryImpl implements ConceptRepositoryCustom {
	@PersistenceContext private EntityManager d_em;
	
	@SuppressWarnings("unchecked")
	public List<Concept> findTreatmentsByIndication(UUID indicationConcept) {
		List<Concept> results = d_em.createNativeQuery(
				"SELECT DISTINCT concepts.* FROM studies, treatments, concepts " +
				"WHERE studies.indication_concept = CAST(:indication AS uuid) " +
				"AND studies.id = treatments.study_id " +
				"AND concepts.id = treatments.drug_concept", Concept.class)
				.setParameter("indication", indicationConcept.toString())
				.getResultList();
		return results;
	}
}
