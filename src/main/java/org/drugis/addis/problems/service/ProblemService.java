package org.drugis.addis.problems.service;

import org.drugis.addis.exception.ProblemCreationException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.problems.model.AbstractProblem;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;

import java.net.URI;

/**
 * Created by daan on 3/21/14.
 */

public interface ProblemService {
  URI DICHOTOMOUS_TYPE_URI = URI.create("http://trials.drugis.org/ontology#dichotomous");
  URI CONTINUOUS_TYPE_URI = URI.create("http://trials.drugis.org/ontology#continuous");

  AbstractProblem getProblem(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException, ProblemCreationException;
  NetworkMetaAnalysisProblem applyModelSettings(NetworkMetaAnalysisProblem problem, Model model);
}
