package org.drugis.trialverse.study;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.drugis.trialverse.QueryTemplate;
import org.springframework.stereotype.Component;

@Component
public class StudyRepositoryImpl implements StudyRepositoryCustom {
	@PersistenceContext private EntityManager d_em;
	
	@Inject @Named("studiesQuery") private QueryTemplate d_studiesQuery;

	@Override
	@SuppressWarnings("unchecked")
	public List<Study> findStudies(UUID indication, List<UUID> variables, List<UUID> treatments) {
		if (d_studiesQuery == null) {
			// FIXME: why doesn't injection work?
			try {
				d_studiesQuery = new QueryTemplate("/studiesQuery.template.sql");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		List<Study> results = d_em.createNativeQuery(
				d_studiesQuery.getTemplate(),
				Study.class)
				.setParameter("indication", indication.toString())
				.getResultList();
		return results;
	}
}
