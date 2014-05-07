package org.drugis.trialverse.study;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.drugis.trialverse.concept.Concept;
import org.springframework.stereotype.Repository;

@Repository
public class MeasurementDao {
	@PersistenceContext private EntityManager d_em;

	public List<Measurement> find(final Long id) {
		final String sql = "FROM Measurement WHERE key.studyId = :id";
		final TypedQuery<Measurement> query = d_em.createQuery(sql, Measurement.class);
		query.setParameter("id", id);
		return query.getResultList();
	}

	public List<Measurement> find(
			final Long id,
			final Concept variable,
			final String measurementMoment) {
		final String sql = "FROM Measurement WHERE key.studyId = :id AND " +
				"key.variableConcept = :var AND " +
				"key.measurementMomentName = :mm";
		final TypedQuery<Measurement> query = d_em.createQuery(sql, Measurement.class);
		query.setParameter("id", id);
		query.setParameter("var", variable);
		query.setParameter("mm", measurementMoment);
		return query.getResultList();
	}
}
