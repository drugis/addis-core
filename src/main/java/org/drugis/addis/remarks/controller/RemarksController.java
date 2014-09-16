package org.drugis.addis.remarks.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.remarks.Remarks;
import org.drugis.addis.remarks.repository.RemarksRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;

/**
 * Created by daan on 16-9-14.
 */
@Controller
@Transactional("ptmAddisCore")
public class RemarksController extends AbstractAddisCoreController {

    @Inject
    RemarksRepository remarksRepository;

    @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/scenarios/{scenarioId}/remarks", method = RequestMethod.GET)
    public Remarks getRemarks(@PathVariable Integer scenarioId) {
        return remarksRepository.get(scenarioId);
    }


}
