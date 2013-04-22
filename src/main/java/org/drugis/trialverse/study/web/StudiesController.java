package org.drugis.trialverse.study.web;

import java.util.List;
import java.util.UUID;

import org.drugis.trialverse.queries.CachedQueryTemplateFactory;
import org.drugis.trialverse.queries.CachedQueryTemplateFactory.QueryTemplate;
import org.drugis.trialverse.queries.QueryTemplateFactory;
import org.drugis.trialverse.study.Study;
import org.drugis.trialverse.study.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
	private final QueryTemplateFactory d_queryTemplateFactory;

	@Autowired
	public StudiesController(StudyRepository studies, CachedQueryTemplateFactory tmpl) {
		Assert.notNull(studies, "StudyRepository must not be null!");
		d_studies = studies;
		d_queryTemplateFactory = tmpl;
	}
	
	@RequestMapping("/findByConcepts")
	@ResponseBody
	public List<Study> getStudiesForConcepts(
			@RequestParam UUID indication,
			@RequestParam List<UUID> variables,
			@RequestParam List<UUID> treatments) { 
		QueryTemplate query = d_queryTemplateFactory.buildQueryTemplate("/queries/studiesQuery.template.sql");
		return d_studies.findStudies(query, indication, variables, treatments);
	}
	
}
