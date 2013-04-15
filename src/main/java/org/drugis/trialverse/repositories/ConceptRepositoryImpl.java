package org.drugis.trialverse.repositories;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.drugis.trialverse.model.Concept;

public class ConceptRepositoryImpl implements ConceptRepositoryCustom {
	@PersistenceContext private EntityManager d_em;
	
	public List<Concept> findTreatments(UUID id) {
		return d_em.createQuery("SELECT DISTINCT concept.* FROM studies, treatments " +
				"WHERE studies.indication_concept = :indication " +
				"AND studies.id = treatments.study_id " +
				"AND concepts.id = treatments.drug_concept", Concept.class).getResultList();
	}
}
