package org.drugis.trialverse.study.web;

import org.drugis.trialverse.study.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class StudyLinks {
	static final String MEASUREMENTS = "measurements";
	static final String MEASUREMENTS_REL = "measurements";


	private final EntityLinks entityLinks;

	@Autowired
	public StudyLinks(final EntityLinks entityLinks) {
		Assert.notNull(entityLinks, "EntityLinks must not be null!");
		this.entityLinks = entityLinks;
	}


	Link getMeasurementLink(final Study study) {
		return entityLinks.linkForSingleResource(study).slash(MEASUREMENTS).withRel(MEASUREMENTS_REL);
	}
}
