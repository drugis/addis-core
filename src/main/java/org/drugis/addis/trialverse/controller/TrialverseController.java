package org.drugis.addis.trialverse.controller;

import net.minidev.json.JSONArray;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.model.emun.StudyDataSection;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by connor on 2/12/14.
 */

@Controller
@RequestMapping("/namespaces")
public class TrialverseController {

  final static Logger logger = LoggerFactory.getLogger(TrialverseController.class);

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private TriplestoreService triplestoreService;

  @RequestMapping(value = "", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Namespace> query() throws MethodNotAllowedException {
    return triplestoreService.queryNameSpaces();
  }

  @RequestMapping(value = "/{namespaceUid}", method = RequestMethod.GET)
  @ResponseBody
  public Namespace get(@PathVariable String namespaceUid, @RequestParam String version) throws ResourceDoesNotExistException {
    return triplestoreService.getNamespaceVersioned(namespaceUid, version);
  }

  @RequestMapping(value = "/{namespaceUid}/outcomes", method = RequestMethod.GET)
  @ResponseBody
  public Collection<SemanticOutcome> queryOutcomes(@PathVariable String namespaceUid, @RequestParam String version) throws ResourceDoesNotExistException {
    return triplestoreService.getOutcomes(namespaceUid, version);
  }

  @RequestMapping(value = "/{namespaceUid}/interventions", method = RequestMethod.GET)
  @ResponseBody
  public Collection<SemanticIntervention> queryInterventions(Principal currentUser, @PathVariable String namespaceUid, @RequestParam String version) throws ResourceDoesNotExistException {
    return triplestoreService.getInterventions(namespaceUid, version);
  }

  @RequestMapping(value = "/{namespaceUid}/studies", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Study> queryStudies(Principal currentUser, @PathVariable String namespaceUid, @RequestParam String version) {
    return triplestoreService.queryStudies(namespaceUid, version);
  }

  @RequestMapping(value = "/{namespaceUid}/studiesWithDetail", method = RequestMethod.GET)
  @ResponseBody
  public Collection<StudyWithDetails> queryStudiesWithDetails(@PathVariable String namespaceUid) {
    return triplestoreService.queryStudydetailsHead(namespaceUid);
  }

  @RequestMapping(value = "/{namespaceUid}/studiesWithDetail/{studyUid}", method = RequestMethod.GET)
  @ResponseBody
  public StudyWithDetails getStudyWithDetails(@PathVariable String namespaceUid, @PathVariable String studyUid) throws ResourceDoesNotExistException {
    return triplestoreService.getStudydetails(namespaceUid, studyUid);
  }

  @RequestMapping(value = "/{namespaceUid}/studiesWithDetail/{studyUid}/arms", method = RequestMethod.GET)
  @ResponseBody
  public JSONArray getStudyArms(@PathVariable String namespaceUid, @PathVariable String studyUid) throws ResourceDoesNotExistException {
    return triplestoreService.getStudyArms(namespaceUid, studyUid);
  }

  @RequestMapping(value = "/{namespaceUid}/studiesWithDetail/{studyUid}/epochs", method = RequestMethod.GET)
  @ResponseBody
  public JSONArray getStudyEpochs(@PathVariable String namespaceUid, @PathVariable String studyUid) throws ResourceDoesNotExistException {
    return triplestoreService.getStudyEpochs(namespaceUid, studyUid);
  }

  @RequestMapping(value = "/{namespaceUid}/studiesWithDetail/{studyUid}/treatmentActivities", method = RequestMethod.GET)
  @ResponseBody
  public List<TreatmentActivity> getStudyTreatmentActivities(@PathVariable String namespaceUid, @PathVariable String studyUid) throws ResourceDoesNotExistException {
    return triplestoreService.getStudyTreatmentActivities(namespaceUid, studyUid);
  }

  @RequestMapping(value = "/{namespaceUid}/studiesWithDetail/{studyUid}/studyData/populationCharacteristics", method = RequestMethod.GET)
  @ResponseBody
  public List<StudyData> getStudyPopulationCharacteristicsData(@PathVariable String namespaceUid, @PathVariable String studyUid) throws ResourceDoesNotExistException {
    return triplestoreService.getStudyData(namespaceUid, studyUid, StudyDataSection.BASE_LINE_CHARACTERISTICS);
  }

  @RequestMapping(value = "/{namespaceUid}/studiesWithDetail/{studyUid}/studyData/adverseEvents", method = RequestMethod.GET)
  @ResponseBody
  public List<StudyData> getStudyAdverseEventsData(@PathVariable String namespaceUid, @PathVariable String studyUid) throws ResourceDoesNotExistException {
    return triplestoreService.getStudyData(namespaceUid, studyUid, StudyDataSection.ADVERSE_EVENTS);
  }

  @RequestMapping(value = "/{namespaceUid}/studiesWithDetail/{studyUid}/studyData/endpoints", method = RequestMethod.GET)
  @ResponseBody
  public List<StudyData> getStudyEndpointsData(@PathVariable String namespaceUid, @PathVariable String studyUid) throws ResourceDoesNotExistException {
    return triplestoreService.getStudyData(namespaceUid, studyUid, StudyDataSection.ENDPOINTS);
  }

  @RequestMapping(value = "/{namespaceUid}/trialData", method = RequestMethod.GET)
  @ResponseBody
  public TrialData getTrialData(@PathVariable String namespaceUid,@RequestParam String version, @RequestParam String outcomeUri,
                                @RequestParam(required = false) List<String> interventionUris) {
    if (interventionUris == null) {
      interventionUris = Collections.emptyList();
    }
    return new TrialData(triplestoreService.getTrialData(namespaceUid, version, outcomeUri, interventionUris));
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(ResourceDoesNotExistException.class)
  public String handleResourceDoesNotExist(HttpServletRequest request) {
    logger.error("Access to non-existent resource.\n{}", request.getRequestURL());
    return "redirect:/error/404";
  }
}
