package org.drugis.trialverse.dataset.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.dataset.model.Dataset;
import org.drugis.trialverse.dataset.service.DatasetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by connor on 21-7-16.
 */
@Controller
public class FeaturedDatasetController extends AbstractAddisCoreController {

  private final static Logger logger = LoggerFactory.getLogger(FeaturedDatasetController.class);

  @Inject
  private DatasetService datasetService;

  @RequestMapping(value = "/featured", method = RequestMethod.GET, headers = WebConstants.ACCEPT_JSON_HEADER)
  @ResponseBody
  public List<Dataset> queryFeaturedDatasetsAsJson(HttpServletResponse httpServletResponse) throws IOException, URISyntaxException {
    logger.trace("retrieving featured datasets");
    List<Dataset> featured = datasetService.findFeatured();
    return featured;
  }
}
