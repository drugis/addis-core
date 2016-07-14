package org.drugis.addis.problems.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.models.Model;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.problems.model.AbstractProblem;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;
import org.drugis.addis.trialverse.service.impl.ReadValueException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

/**
 * Created by daan on 3/21/14.
 */

public interface ProblemService {
  AbstractProblem getProblem(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException, URISyntaxException, SQLException, IOException, ReadValueException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException;
  NetworkMetaAnalysisProblem applyModelSettings(NetworkMetaAnalysisProblem problem, Model model);
}
