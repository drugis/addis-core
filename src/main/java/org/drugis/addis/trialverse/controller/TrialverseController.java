package org.drugis.addis.trialverse.controller;

import net.minidev.json.JSONArray;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
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
public class TrialverseController {

  final static Logger logger = LoggerFactory.getLogger(TrialverseController.class);

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private TrialverseRepository trialverseRepository;

  @Inject
  private TriplestoreService triplestoreService;

  @RequestMapping(value = "/namespaces", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Namespace> query() throws MethodNotAllowedException {
    return triplestoreService.queryNameSpaces();
  }

  @RequestMapping(value = "/namespaces/{namespaceUid}", method = RequestMethod.GET)
  @ResponseBody
  public Namespace get(@PathVariable String namespaceUid) throws ResourceDoesNotExistException {
    return triplestoreService.getNamespace(namespaceUid);
  }

  @RequestMapping(value = "/namespaces/{namespaceUid}/outcomes", method = RequestMethod.GET)
  @ResponseBody
  public Collection<SemanticOutcome> queryOutcomes(@PathVariable String namespaceUid) throws ResourceDoesNotExistException {
    return triplestoreService.getOutcomes(namespaceUid);
  }

  @RequestMapping(value = "/namespaces/{namespaceUid}/interventions", method = RequestMethod.GET)
  @ResponseBody
  public Collection<SemanticIntervention> queryInterventions(Principal currentUser, @PathVariable String namespaceUid) throws ResourceDoesNotExistException {
    return triplestoreService.getInterventions(namespaceUid);
  }

  @RequestMapping(value = "/namespaces/{namespaceUid}/studies", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Study> queryStudies(Principal currentUser, @PathVariable String namespaceUid) {
    return triplestoreService.queryStudies(namespaceUid);
  }

  @RequestMapping(value = "/namespaces/{namespaceUid}/studiesWithDetail", method = RequestMethod.GET)
  @ResponseBody
  public Collection<StudyWithDetails> queryStudiesWithDetails(@PathVariable String namespaceUid) {
    return triplestoreService.queryStudydetails(namespaceUid);
  }

  @RequestMapping(value = "/namespaces/{namespaceUid}/studiesWithDetail/{studyUid}", method = RequestMethod.GET)
  @ResponseBody
  public StudyWithDetails getStudysWithDetails(@PathVariable String namespaceUid, @PathVariable String studyUid) throws ResourceDoesNotExistException {
    return triplestoreService.getStudydetails(namespaceUid, studyUid);
  }

  @RequestMapping(value = "/namespaces/{namespaceUid}/studiesWithDetail/{studyUid}/studyDesign", method = RequestMethod.GET)
  @ResponseBody
  public List<TreatmentActivity> getStudyDesign(@PathVariable String namespaceUid, @PathVariable String studyUid) throws ResourceDoesNotExistException {
    return triplestoreService.getStudyDesign(namespaceUid, studyUid);
  }

  @RequestMapping(value = "/namespaces/{namespaceUid}/studiesWithDetail/{studyUid}/arms", method = RequestMethod.GET)
  @ResponseBody
  public JSONArray getStudyArms(@PathVariable String namespaceUid, @PathVariable String studyUid) throws ResourceDoesNotExistException {
    logger.debug("getStudyArms");
    return triplestoreService.getStudyArms(namespaceUid, studyUid);
  }

  @RequestMapping(value = "/namespaces/{namespaceUid}/trialData", method = RequestMethod.GET)
  @ResponseBody
  public TrialData getTrialData(@PathVariable String namespaceUid, @RequestParam(required = true) String outcomeUri,
                                @RequestParam(required = false) List<String> interventionUris) {
    if (interventionUris == null) {
      interventionUris = Collections.emptyList();
    }
    return new TrialData(triplestoreService.getTrialData(namespaceUid, outcomeUri, interventionUris));
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(ResourceDoesNotExistException.class)
  public String handleResourceDoesNotExist(HttpServletRequest request) {
    logger.error("Access to non-existent resource.\n{}", request.getRequestURL());
    return "redirect:/error/404";
  }
}
