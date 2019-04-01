package org.drugis.addis.importer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.importer.service.impl.ClinicalTrialsImportError;
import org.drugis.addis.importer.service.ClinicalTrialsImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/import")
public class ImportController extends AbstractAddisCoreController {

  private final static Logger logger = LoggerFactory.getLogger(ImportController.class);

  @Inject
  private ClinicalTrialsImportService clinicalTrialsImportService;

  @RequestMapping(value = "/{nctId}", method = RequestMethod.GET)
  @ResponseBody
  public JsonNode fetchInfo(HttpServletResponse response, @PathVariable String nctId) throws ClinicalTrialsImportError {
    logger.debug("fetch study info from clinicalTrials Importer for NTCID: " + nctId);
    return clinicalTrialsImportService.fetchInfo(nctId);
  }
 }
