package org.drugis.trialverse.study.web;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.drugis.trialverse.CachedQueryTemplateFactory.QueryTemplate;
import org.drugis.trialverse.QueryTemplateFactory;
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
@RequestMapping("/studies")
@ExposesResourceFor(Study.class)
public class StudiesController {

	private final StudyRepository d_studies;
	private QueryTemplateFactory d_queryTemplateFactory;

	
	@Autowired
	public StudiesController(StudyRepository studies, QueryTemplateFactory tmpl) {
		Assert.notNull(studies, "StudyRepository must not be null!");
		d_studies = studies;
		d_queryTemplateFactory = tmpl;

	}
	
	@RequestMapping("/findByConcepts")
	@ResponseBody
	public List<Study> getStudiesForConcepts(@RequestParam UUID indication) { 
		QueryTemplate query = d_queryTemplateFactory.buildQueryTemplate("/studiesQuery.template.sql");
		return d_studies.findStudies(
				query, 
				indication, 
				Collections.<UUID>emptyList(), 
				Collections.<UUID>emptyList());
	}
	
}
