package org.drugis.trialverse.study.web;

import org.drugis.trialverse.study.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class StudyResourceProcessor implements ResourceProcessor<Resource<Study>> {

	private final StudyLinks links;

	@Autowired
	public StudyResourceProcessor(final StudyLinks links) {
		Assert.notNull(links, "StudyLinks must not be null!");
		this.links = links;
	}

	@Override
	public Resource<Study> process(final Resource<Study> resource) {
		final Study study = resource.getContent();
		resource.add(this.links.getMeasurementLink(study));
		return resource;
	}

}
