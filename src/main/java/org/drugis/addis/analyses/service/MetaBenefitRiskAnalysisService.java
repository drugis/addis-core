package org.drugis.addis.analyses.service;

import org.drugis.addis.analyses.MbrOutcomeInclusion;
import org.drugis.addis.analyses.MetaBenefitRiskAnalysis;
import org.drugis.addis.exception.MethodNotAllowedException;
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
public interface MetaBenefitRiskAnalysisService {
  MetaBenefitRiskAnalysis update(Account user, Integer projectId, MetaBenefitRiskAnalysis analysis) throws URISyntaxException, SQLException, IOException, ResourceDoesNotExistException, MethodNotAllowedException, ReadValueException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException;

  void checkMetaBenefitRiskAnalysis(Account user, MetaBenefitRiskAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException;

  List<MbrOutcomeInclusion> cleanInclusions(MetaBenefitRiskAnalysis analysis, MetaBenefitRiskAnalysis oldAnalysis);
}
