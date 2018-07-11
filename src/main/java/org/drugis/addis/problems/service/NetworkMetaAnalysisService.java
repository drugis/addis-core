package org.drugis.addis.problems.service;

import org.drugis.addis.analyses.model.NetworkMetaAnalysis;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.problems.model.AbstractNetworkMetaAnalysisProblemEntry;
import org.drugis.addis.problems.model.TreatmentEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;

import java.util.List;
import java.util.Map;

public interface NetworkMetaAnalysisService {
  List<TreatmentEntry> getTreatments(NetworkMetaAnalysis analysis) throws ResourceDoesNotExistException;

  List<AbstractNetworkMetaAnalysisProblemEntry> buildPerformanceEntries(NetworkMetaAnalysis analysis, List<TrialDataStudy> trialDataStudies);

  List<TrialDataStudy> getStudiesWithEntries(List<TrialDataStudy> trialDataStudies, List<AbstractNetworkMetaAnalysisProblemEntry> entries);

  Map<String,Map<String,Double>> getStudyLevelCovariates(Project project, NetworkMetaAnalysis analysis, List<TrialDataStudy> studiesWithEntries);
}
