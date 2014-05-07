package org.drugis.trialverse.study;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import lombok.Data;

import org.drugis.trialverse.concept.Concept;
import org.drugis.trialverse.core.EntityLinkResolver;
import org.drugis.trialverse.jpa.CachedQueryTemplateFactory.QueryTemplate;
import org.drugis.trialverse.jpa.QueryTemplateFactory;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

@Component
public class StudyRepositoryImpl implements StudyRepositoryCustom {
	@PersistenceContext private EntityManager d_em;

	@Autowired private QueryTemplateFactory queryTemplateFactory;

	@Override
	@SuppressWarnings("unchecked")
	public List<Study> findStudies(
			final UUID indication,
			final List<UUID> variables,
			final List<UUID> treatments) {
		final QueryTemplate template = queryTemplateFactory.buildQueryTemplate("queries/studiesQuery.template.sql");
		final List<Study> results = d_em.createNativeQuery(
				template.getTemplate(),
				Study.class)
				.setParameter("indication", indication.toString())
				.setParameter("treatments", uuidToString(treatments))
				.setParameter("variables", uuidToString(variables))
				.getResultList();
		return results;
	}

	private static List<String> uuidToString(final List<UUID> uuids) {
		final List<String> result = new ArrayList<>();
		for(final UUID uuid : uuids) {
			result.add(uuid.toString());
		}
		return result;
	}

	@Entity
	@Data static class StudyConceptPair {
		@EmbeddedId @JsonUnwrapped private StudyConceptPairId key;

		@Embeddable
		@Data private static class StudyConceptPairId implements Serializable {
			private static final long serialVersionUID = 708984061275955124L;
			private Long studyId;
			@JsonIgnore private @Type(type="pg-uuid") UUID conceptId;
		}

		public URI getConcept() {
			return EntityLinkResolver.getInstance().getLinkForEntity(Concept.class, key.conceptId);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<StudyConceptPair> findVariableMappings(final List<Long> studies, final UUID variable) {
		final QueryTemplate template = queryTemplateFactory.buildQueryTemplate("queries/variableMappingQuery.template.sql");
		final List<StudyConceptPair> results = d_em.createNativeQuery(
				template.getTemplate(), StudyConceptPair.class)
				.setParameter("studies", studies)
				.setParameter("variable", variable.toString())
				.getResultList();
		return results;
	}
}
