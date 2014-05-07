package org.drugis.trialverse.concept;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
@Component
public class ConceptRepositoryImpl implements ConceptRepositoryCustom {
	@PersistenceContext private EntityManager d_em;

	@SuppressWarnings("unchecked")
	private List<Concept> retrieveConcepts(
			final UUID concept,
			final String name,
			final StringBuilder sql) {
		if(name != null && !name.isEmpty()) {
			sql.append(" AND concepts.name ~ :name");
		}
		final Query query = d_em.createNativeQuery(sql.toString(), Concept.class);
		if(name != null && !name.isEmpty()) {
			query.setParameter("name", name);
		}
		query.setParameter("indication", concept.toString());
		return query.getResultList();
	}

	@Override
	public List<Concept> findTreatmentsByIndication(
			final UUID indicationConcept,
			final String name) {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT concepts.* FROM studies, treatments, concepts ");
		sql.append("WHERE studies.indication_concept = CAST(:indication AS uuid) ");
		sql.append("AND studies.id = treatments.study_id ");
		sql.append("AND concepts.id = treatments.drug_concept");
		return retrieveConcepts(indicationConcept, name, sql);
	}

	@Override
	public List<Concept> findVariablesByIndication(
			final UUID indicationConcept,
			final String name) {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT concepts.* ");
		sql.append("FROM studies, variables, concepts AS study_concepts, concept_map, concepts ");
		sql.append("WHERE studies.indication_concept = CAST(:indication AS uuid) ");
		sql.append("AND studies.id = variables.study_id ");
		sql.append("AND study_concepts.id = variables.variable_concept ");
		sql.append("AND concept_map.sub = study_concepts.id ");
		sql.append("AND concept_map.super = concepts.id");
		return retrieveConcepts(indicationConcept, name, sql);
	}
}
