package org.drugis.addis.ordering.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.ordering.Ordering;
import org.drugis.addis.ordering.repository.OrderingRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
public class OrderingController extends AbstractAddisCoreController{
  @Inject
  private OrderingRepository orderingRepository;

  @RequestMapping(value="/projects/{projectId}/analyses/{analysisId}/ordering", method = RequestMethod.GET)
  @ResponseBody
  public String get(@PathVariable Integer analysisId) {
    return orderingRepository.get(analysisId);
  }
}
