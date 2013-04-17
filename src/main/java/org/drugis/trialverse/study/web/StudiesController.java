package org.drugis.trialverse.study.web;

import org.drugis.trialverse.study.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/studies/{id}")
public class StudiesController {

	private final StudyRepository d_studies;
	private final EntityLinks d_entityLinks; 
		
	@Autowired
	public StudiesController(StudyRepository studies, EntityLinks entityLinks) {
		Assert.notNull(entityLinks, "EntityLinks must not be null!");
		Assert.notNull(studies, "ConceptRepository must not be null!");
		d_studies = studies;
		d_entityLinks = entityLinks;
	}
	
}
