package org.drugis.addis.problems.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.drugis.addis.analyses.model.NetworkMetaAnalysis;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.model.problemEntry.AbstractProblemEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface NetworkMetaAnalysisService {
  List<TreatmentEntry> getTreatments(NetworkMetaAnalysis analysis) throws ResourceDoesNotExistException;

  List<AbstractProblemEntry> buildAbsolutePerformanceEntries(NetworkMetaAnalysis analysis, List<TrialDataStudy> trialDataStudies);

  RelativeEffectData buildRelativeEffectData(NetworkMetaAnalysis analysis, List<TrialDataStudy> studies);

  List<TrialDataStudy> getStudiesWithEntries(List<TrialDataStudy> trialDataStudies, List<AbstractProblemEntry> entries);

  Map<String, Map<String, Double>> getStudyLevelCovariates(Project project, NetworkMetaAnalysis analysis, List<TrialDataStudy> studiesWithEntries);

  Map<URI, CriterionEntry> buildCriteriaForInclusion(NMAInclusionWithResults inclusionWithResults, URI modelURI);

  Map<String, AlternativeEntry> buildAlternativesForInclusion(NMAInclusionWithResults inclusionWithResults);

  Map<Integer,JsonNode> getPataviResultsByModelId(Collection<Model> models) throws IOException, SQLException, UnexpectedNumberOfResultsException, URISyntaxException;

}
