package org.drugis.addis.ordering.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.ordering.Ordering;
import org.drugis.addis.ordering.repository.OrderingRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;

@Controller
public class OrderingController extends AbstractAddisCoreController {
  @Inject
  private OrderingRepository orderingRepository;

  @Inject
  private ProjectService projectService;

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/ordering", method = RequestMethod.GET)
  @ResponseBody
  public Ordering get(@PathVariable(value="analysisId") Integer analysisId) {
    return orderingRepository.get(analysisId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/ordering", method = RequestMethod.PUT)
  @ResponseBody
  public void put(Principal principal, @PathVariable(value="projectId") Integer projectId, @PathVariable(value="analysisId") Integer analysisId,
                  @RequestBody String ordering) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkOwnership(projectId, principal);
    orderingRepository.put(analysisId, ordering);
  }


}
