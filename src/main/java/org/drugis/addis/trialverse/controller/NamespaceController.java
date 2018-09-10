package org.drugis.addis.trialverse.controller;

import net.minidev.json.JSONArray;
import net.minidev.json.parser.ParseException;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.model.emun.StudyDataSection;
import org.drugis.addis.trialverse.model.mapping.TriplestoreUuidAndOwner;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

/**
 * Created by connor on 2/12/14.
 */

@Controller
@RequestMapping("/namespaces")
public class NamespaceController {

  final static Logger logger = LoggerFactory.getLogger(NamespaceController.class);

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private TriplestoreService triplestoreService;

  @Inject
  private MappingService mappingService;

  @RequestMapping(value = "", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Namespace> query() throws MethodNotAllowedException, ParseException {
    return triplestoreService.queryNameSpaces();
  }

  @RequestMapping(value = "/{namespaceUid}", method = RequestMethod.GET)
  @ResponseBody
  public Namespace get(@PathVariable String namespaceUid, @RequestParam(required = false) URI version) throws ResourceDoesNotExistException, URISyntaxException, IOException {
    if (version != null) {
      TriplestoreUuidAndOwner triplestoreUuidAndOwner = mappingService.getVersionedUuidAndOwner(namespaceUid);
      return triplestoreService.getNamespaceVersioned(triplestoreUuidAndOwner, version);
    } else {
      return triplestoreService.getNamespaceHead(mappingService.getVersionedUuidAndOwner(namespaceUid));
    }
  }

  @RequestMapping(value = "/{namespaceUid}/outcomes", method = RequestMethod.GET)
  @ResponseBody
  public Collection<SemanticVariable> queryOutcomes(@PathVariable String namespaceUid, @RequestParam String version) throws ResourceDoesNotExistException, URISyntaxException, ReadValueException, IOException {
    return triplestoreService.getOutcomes(mappingService.getVersionedUuid(namespaceUid), URI.create(version));
  }

  @RequestMapping(value = "/{namespaceUid}/interventions", method = RequestMethod.GET)
  @ResponseBody
  public Collection<SemanticInterventionUriAndName> queryInterventions(@PathVariable String namespaceUid, @RequestParam String version) throws ResourceDoesNotExistException, URISyntaxException, IOException {
    return triplestoreService.getInterventions(mappingService.getVersionedUuid(namespaceUid), URI.create(version));
  }

  @RequestMapping(value = "/{namespaceUid}/studies", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Study> queryStudies(@PathVariable String namespaceUid, @RequestParam String version) throws URISyntaxException, IOException {
    return triplestoreService.queryStudies(mappingService.getVersionedUuid(namespaceUid), URI.create(version));
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(ResourceDoesNotExistException.class)
  public String handleResourceDoesNotExist(HttpServletRequest request) {
    logger.error("Access to non-existent resource.\n{}", request.getRequestURL());
    return "redirect:/error/404";
  }
}
