package org.drugis.trialverse.study.web;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.drugis.trialverse.study.Measurement;
import org.drugis.trialverse.study.MeasurementDao;
import org.drugis.trialverse.study.Study;
import org.drugis.trialverse.study.StudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/studies")
@ExposesResourceFor(Study.class)
public class StudiesController {

	private final StudyRepository d_studies;
	private final EntityLinks d_entityLinks;
	private final MeasurementDao d_measurements;

	@Autowired
	public StudiesController(
			final StudyRepository studies,
			final MeasurementDao measurements,
			final EntityLinks entityLinks) {
		Assert.notNull(measurements, "MeasurementDao must not be null!");
		Assert.notNull(studies, "StudyRepository must not be null!");
		Assert.notNull(entityLinks, "EntityLinks must not be null!");
		d_studies = studies;
		d_entityLinks = entityLinks;
		d_measurements = measurements;
	}

	@RequestMapping("{id}/measurements")
	@ResponseBody
	public List<Measurement>
		getMeasurements(@PathVariable final Long id,
				@RequestParam(required=false) final UUID variable,
				@RequestParam(required=false) final String measurementMoment) {
		if (variable != null && measurementMoment != null) {
			System.err.println(variable);
			System.err.println(measurementMoment);
			return d_measurements.findByStudyIdAndVariableAndMeasurementMoment(id, variable, measurementMoment);
		} else if (variable == null && measurementMoment == null) {
			return d_measurements.findByStudyId(id);
		} else {
			throw new RuntimeException();
		}
	}

	@RequestMapping("/findByConcepts")
	@ResponseBody
	public ResponseEntity<List<Resource<Study>>> getStudiesForConcepts(
			@RequestParam final UUID indication,
			@RequestParam final List<UUID> variables,
			@RequestParam final List<UUID> treatments) {

		final List<Study> studies = d_studies.findStudies(indication, variables, treatments);
		final List<Resource<Study>> result = new ArrayList<>();
		for(final Study study : studies) {
			final Resource<Study> resource = new Resource<Study>(study);
			result.add(resource);
			resource.add(d_entityLinks.linkForSingleResource(study).withSelfRel());
		}
		return new ResponseEntity<List<Resource<Study>>>(result, HttpStatus.OK);
	}

}
