package org.drugis.trialverse.study;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

@Repository
public class MeasurementDao {
	@PersistenceContext private EntityManager d_em;

	public List<Measurement> findByStudyId(final Long id) {
		final String sql = "FROM Measurement WHERE measurementPK.studyId = :id";
		final TypedQuery<Measurement> query = d_em.createQuery(sql, Measurement.class);
		query.setParameter("id", id);
		return query.getResultList();
	}

	public List<Measurement> findByStudyIdAndVariableAndMeasurementMoment(final Long id,
			final UUID variable, final String measurementMoment) {
		final String sql = "FROM Measurement WHERE measurementPK.studyId = :id AND " +
				"measurementPK.variableConcept = :var AND " +
				"measurementPK.measurementMomentName = 'P0D before epoch end Main phase'";
		final TypedQuery<Measurement> query = d_em.createQuery(sql, Measurement.class);
		query.setParameter("id", id);
		query.setParameter("var", variable);
//		query.setParameter("mm", measurementMoment);
		return query.getResultList();
	}
}
