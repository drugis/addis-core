package org.drugis.addis.covariates;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by connor on 12/1/15.
 */
@Controller
public class CovariateOptionsController extends AbstractAddisCoreController {
  @RequestMapping(value = "/covariate-options", method = RequestMethod.GET)
  @ResponseBody
  public List<CovariateViewAdepter> getCovariateOptions() {
    CovariateOption[] studyCovarates = CovariateOption.values();
    return Arrays.stream(studyCovarates).map(c -> new CovariateViewAdepter(c)).collect(Collectors.toList());
  }
}
