package org.drugis.trialverse.rest;

import org.drugis.trialverse.model.Concept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;

@Component
public class ConceptResourceProcessor implements ResourceProcessor<Resource<Concept>> {

	@Autowired
	public ConceptResourceProcessor(ConceptLinks links) {
		Assert.notNull(links, "ConceptLinks must not be null!");
		this.links = links;
	}

	private final ConceptLinks links;
	
	@Override
	public Resource<Concept> process(Resource<Concept> resource) {
		resource.add(this.links.getTreatmentLink(resource.getContent()));
		return resource;
	}

}
