package org.drugis.trialverse.concept.web;

import org.drugis.trialverse.concept.Concept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class ConceptResourceProcessor implements ResourceProcessor<Resource<Concept>> {

	private final ConceptLinks links;
	
	@Autowired
	public ConceptResourceProcessor(ConceptLinks links) {
		Assert.notNull(links, "ConceptLinks must not be null!");
		this.links = links;
	}

	@Override
	public Resource<Concept> process(Resource<Concept> resource) {
		resource.add(this.links.getTreatmentLink(resource.getContent()));
		return resource;
	}

}
