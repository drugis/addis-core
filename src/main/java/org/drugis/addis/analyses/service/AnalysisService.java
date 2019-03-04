package org.drugis.addis.analyses.service;

import org.drugis.addis.analyses.model.AbstractAnalysis;
import org.drugis.addis.analyses.model.AnalysisCommand;
import org.drugis.addis.analyses.model.BenefitRiskAnalysis;
import org.drugis.addis.analyses.model.NetworkMetaAnalysis;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.SingleIntervention;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.model.trialdata.AbstractSemanticIntervention;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.impl.ReadValueException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by daan on 22-5-14.
 */
public interface AnalysisService {
  void checkCoordinates(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException;

  void checkProjectIdChange(AbstractAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException;

  NetworkMetaAnalysis updateNetworkMetaAnalysis(Account user, NetworkMetaAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException, SQLException, IOException;

  NetworkMetaAnalysis createNetworkMetaAnalysis(Account user, AnalysisCommand analysisCommand) throws ResourceDoesNotExistException, MethodNotAllowedException;

  BenefitRiskAnalysis createBenefitRiskAnalysis(Account user, AnalysisCommand analysisCommand) throws MethodNotAllowedException, SQLException, IOException, ResourceDoesNotExistException;

  List<TrialDataStudy> buildEvidenceTable(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException, ReadValueException, URISyntaxException, IOException;

  Set<AbstractIntervention> getIncludedInterventions(AbstractAnalysis analysis) throws ResourceDoesNotExistException;

  Set<SingleIntervention> getSingleInterventions(Set<AbstractIntervention> includedInterventions) throws ResourceDoesNotExistException;
}
