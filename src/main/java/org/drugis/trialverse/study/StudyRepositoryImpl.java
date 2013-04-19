package org.drugis.trialverse.study;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.drugis.trialverse.CachedQueryTemplateFactory.QueryTemplate;
import org.springframework.stereotype.Component;

@Component
public class StudyRepositoryImpl implements StudyRepositoryCustom {
	@PersistenceContext private EntityManager d_em;
	

	@Override
	@SuppressWarnings("unchecked")
	public List<Study> findStudies(
			QueryTemplate query, 
			UUID indication,
			List<UUID> variables,
			List<UUID> treatments) {
		List<Study> results = d_em.createNativeQuery(
				query.getTemplate(),
				Study.class)
				.setParameter("indication", indication.toString())
				.getResultList();
		return results;
	}
}
