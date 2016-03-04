package org.drugis.addis.problems.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.problems.model.AbstractProblem;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

/**
 * Created by daan on 3/21/14.
 */

public interface ProblemService {
  AbstractProblem getProblem(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException, URISyntaxException, SQLException, IOException;
}
