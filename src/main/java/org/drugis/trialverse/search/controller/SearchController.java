package org.drugis.trialverse.search.controller;

import org.drugis.trialverse.search.model.SearchResult;
import org.drugis.trialverse.search.service.SearchService;
import org.drugis.trialverse.util.controller.AbstractTrialverseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by connor on 01/09/15.
 */
@Controller
@RequestMapping
public class SearchController extends AbstractTrialverseController {

  @Inject
  SearchService searchService;

  @RequestMapping(value = "/search", method = RequestMethod.GET)
  @ResponseBody
  public List<SearchResult> searchStudy(@RequestParam(value = "searchTerm", required = false) String searchTerm) throws IOException, URISyntaxException {
    return searchService.searchStudy(searchTerm);
  }
}
