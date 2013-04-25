package org.drugis.trialverse.study;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.drugis.trialverse.jpa.QueryTemplateFactory;
import org.drugis.trialverse.jpa.CachedQueryTemplateFactory.QueryTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyRepositoryImpl implements StudyRepositoryCustom {
	@PersistenceContext private EntityManager d_em;
	
	@Autowired private QueryTemplateFactory queryTemplateFactory;
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Study> findStudies(
			UUID indication,
			List<UUID> variables,
			List<UUID> treatments) {
		QueryTemplate template = queryTemplateFactory.buildQueryTemplate("queries/studiesQuery.template.sql");
		List<Study> results = d_em.createNativeQuery(
				template.getTemplate(),
				Study.class)
				.setParameter("indication", UUIDsToString(Collections.singletonList(indication)))
				.setParameter("treatments", UUIDsToString(treatments))
				.setParameter("variables", UUIDsToString(variables))
				.getResultList();
		return results;
	}
	
	private List<Object> UUIDsToString(List<UUID> uuids) { 
		List<Object> result = new ArrayList<>();
		for(UUID uuid : uuids) { 
			result.add(uuid.toString());
		}
		return result;
	}
}
