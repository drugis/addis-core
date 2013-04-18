package org.drugis.trialverse.study.web;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.drugis.trialverse.study.Study;
import org.drugis.trialverse.study.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/studies/{id}")
@ExposesResourceFor(Study.class)
public class StudiesController {

	private final StudyRepository d_studies;
	private final EntityLinks d_entityLinks; 
		
	@Autowired
	public StudiesController(StudyRepository studies, EntityLinks entityLinks) {
		Assert.notNull(entityLinks, "EntityLinks must not be null!");
		Assert.notNull(studies, "StudyRepository must not be null!");
		d_studies = studies;
		d_entityLinks = entityLinks;
	}
	
	@RequestMapping("/foo")
	@ResponseBody
	public List<Study> getStudiesOrSomething(@RequestParam UUID indication) { 
		return d_studies.findStudies(indication, Collections.<UUID>emptyList(), Collections.<UUID>emptyList());
	}
	
}
