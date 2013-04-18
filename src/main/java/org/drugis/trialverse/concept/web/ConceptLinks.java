package org.drugis.trialverse.concept.web;

import org.drugis.trialverse.concept.Concept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class ConceptLinks {
	static final String TREATMENTS = "treatments";
	static final String TREATMENTS_REL = "treatments";

	static final String VARIABLES = "variables";
	static final String VARIABLES_REL = "variables";
	
	private final EntityLinks entityLinks;

	@Autowired
	public ConceptLinks(EntityLinks entityLinks) {
		Assert.notNull(entityLinks, "EntityLinks must not be null!");
		this.entityLinks = entityLinks;
	}
	
	Link getVariableLink(Concept concept) {
		return entityLinks.linkForSingleResource(concept).slash(VARIABLES).withRel(VARIABLES_REL);
	}
	
	Link getTreatmentLink(Concept concept) {
		return entityLinks.linkForSingleResource(concept).slash(TREATMENTS).withRel(TREATMENTS_REL);
	}
}
