package org.drugis.addis.analyses.service;

import org.drugis.addis.analyses.MetaBenefitRiskAnalysis;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.service.impl.ReadValueException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

/**
 * Created by connor on 9-3-16.
 */
public interface MetaBenefitRiskAnalysisService {
  MetaBenefitRiskAnalysis update(Account user, Integer projectId, MetaBenefitRiskAnalysis analysis) throws URISyntaxException, SQLException, IOException, ResourceDoesNotExistException, MethodNotAllowedException, ReadValueException;
}
