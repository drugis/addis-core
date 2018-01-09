package org.drugis.addis.analyses.service;

import org.drugis.addis.analyses.model.BenefitRiskNMAOutcomeInclusion;
import org.drugis.addis.analyses.model.BenefitRiskAnalysis;
import org.drugis.addis.analyses.model.BenefitRiskStudyOutcomeInclusion;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ProblemCreationException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.service.impl.ReadValueException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by connor on 9-3-16.
 */
public interface BenefitRiskAnalysisService {
  BenefitRiskAnalysis update(Account user, Integer projectId, BenefitRiskAnalysis analysis, String scenarioState, String path) throws URISyntaxException, SQLException, IOException, ResourceDoesNotExistException, MethodNotAllowedException, ReadValueException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException, ProblemCreationException;

  void updateBenefitRiskAnalysis(Account user, BenefitRiskAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException;

  List<BenefitRiskNMAOutcomeInclusion> removeBaselinesWithoutIntervention(BenefitRiskAnalysis analysis, BenefitRiskAnalysis oldAnalysis);

}
