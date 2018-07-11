package org.drugis.addis.problems.service.impl;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.model.ArmExclusion;
import org.drugis.addis.analyses.model.MeasurementMomentInclusion;
import org.drugis.addis.analyses.model.NetworkMetaAnalysis;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.NetworkMetaAnalysisService;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
public class NetworkMetaAnalysisServiceImpl implements NetworkMetaAnalysisService {

  @Inject
  private AnalysisService analysisService;

  @Inject
  private CovariateRepository covariateRepository;

  @Inject
  private NetworkMetaAnalysisEntryBuilder networkMetaAnalysisEntryBuilder;

  @Override
  public List<TreatmentEntry> getTreatments(NetworkMetaAnalysis analysis) throws ResourceDoesNotExistException {
    Set<AbstractIntervention> includedInterventions = analysisService.getIncludedInterventions(analysis);

    return includedInterventions.stream()
            .map(intervention -> new TreatmentEntry(intervention.getId(), intervention.getName()))
            .collect(toList());
  }

  @Override
  public List<AbstractNetworkMetaAnalysisProblemEntry> buildPerformanceEntries(NetworkMetaAnalysis analysis, List<TrialDataStudy> trialDataStudies) {
    // create map of (non-default) measurement moment inclusions for the analysis
    final Map<URI, URI> selectedMeasurementMomentsByStudy = analysis.getIncludedMeasurementMoments()
            .stream().collect(toMap(MeasurementMomentInclusion::getStudy, MeasurementMomentInclusion::getMeasurementMoment));

    List<URI> armExclusionTrialverseIds = analysis.getExcludedArms().stream()
            .map(ArmExclusion::getTrialverseUid).collect(toList());

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = trialDataStudies.stream()
            .map(trialDataStudy -> armsToEntries(selectedMeasurementMomentsByStudy, armExclusionTrialverseIds, trialDataStudy))
            .reduce(new ArrayList<>(), ListUtils::union);

    entries = changeToStdErrEntriesIfNeeded(entries);
    return entries;
  }

  private List<AbstractNetworkMetaAnalysisProblemEntry> armsToEntries(Map<URI, URI> selectedMeasurementMomentsByStudy, List<URI> armExclusionTrialverseIds, TrialDataStudy trialDataStudy) {
    final URI selectedMeasurementMoment = getSelectedMeasurementMoment(selectedMeasurementMomentsByStudy, trialDataStudy);

    List<TrialDataArm> filteredArms = trialDataStudy.getTrialDataArms()
            .stream()
            .filter(arm -> arm.getMatchedProjectInterventionIds().size() == 1)
            .filter(arm -> !armExclusionTrialverseIds.contains(arm.getUri()))
            .filter(arm -> arm.getMeasurementsForMoment(selectedMeasurementMoment) != null)
            .collect(toList());

    // do not include studies with fewer than two included and matched arms
    if (filteredArms.size() > 1) {
      return filteredArms.stream()
              .map(arm -> {
                Set<Measurement> measurements = arm.getMeasurementsForMoment(selectedMeasurementMoment);
                return networkMetaAnalysisEntryBuilder.build(trialDataStudy.getName(),
                        arm.getMatchedProjectInterventionIds().iterator().next(), // safe because we filter unmatched arms
                        measurements.iterator().next()); // nma has exactly one measurement
              })
              .collect(toList());
    }
    return new ArrayList<>();
  }

  private URI getSelectedMeasurementMoment(Map<URI, URI> selectedMeasurementMomentsByStudy, TrialDataStudy trialDataStudy) {
    return selectedMeasurementMomentsByStudy.get(trialDataStudy.getStudyUri()) == null
            ? trialDataStudy.getDefaultMeasurementMoment()
            : selectedMeasurementMomentsByStudy.get(trialDataStudy.getStudyUri());
  }

  private List<AbstractNetworkMetaAnalysisProblemEntry> changeToStdErrEntriesIfNeeded(List<AbstractNetworkMetaAnalysisProblemEntry> entries) {
    // if there's an entry with missing standard deviation or sample size, move everything to standard error
    Boolean isStdErrEntry = entries.stream().anyMatch(entry -> entry instanceof ContinuousStdErrEntry);
    if (isStdErrEntry) {
      entries = entries.stream().map(entry -> {
        if (entry instanceof ContinuousStdErrEntry) {
          return entry;
        }
        ContinuousNetworkMetaAnalysisProblemEntry tmpEntry = (ContinuousNetworkMetaAnalysisProblemEntry) entry;
        Double stdErr = tmpEntry.getStdDev() / Math.sqrt(tmpEntry.getSampleSize());
        return new ContinuousStdErrEntry(tmpEntry.getStudy(), tmpEntry.getTreatment(), tmpEntry.getMean(), stdErr);
      }).collect(toList());
    }
    return entries;
  }

  @Override
  public List<TrialDataStudy> getStudiesWithEntries(List<TrialDataStudy> trialDataStudies, List<AbstractNetworkMetaAnalysisProblemEntry> entries) {
    Set<String> studyNames = entries.stream()
            .map(AbstractNetworkMetaAnalysisProblemEntry::getStudy)
            .collect(Collectors.toSet());
    return trialDataStudies.stream()
            .filter(study -> studyNames.contains(study.getName()))
            .collect(toList());  }

  @Override
  public Map<String, Map<String, Double>> getStudyLevelCovariates(Project project, NetworkMetaAnalysis analysis, List<TrialDataStudy> studiesWithEntries) {
    List<String> includedCovariateKeys = getIncludedCovariateKeys(project, analysis);
    if (includedCovariateKeys.size() == 0) {
      return null;
    }

    Map<String, Covariate> covariatesByKey = covariateRepository.findByProject(project.getId())
            .stream()
            .collect(toMap(Covariate::getDefinitionKey, identity()));
    return studiesWithEntries.stream()
            .map(trialDataStudy -> getCovariateNodes(trialDataStudy, covariatesByKey))
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
  }

  private Pair<String, Map<String, Double>> getCovariateNodes(TrialDataStudy trialDataStudy, Map<String, Covariate> covariatesByKey) {
    Map<String, Double> covariateNodes = trialDataStudy.getCovariateValues().stream()
            .map(covariateStudyValue -> {
              String covariateName = covariatesByKey.get(covariateStudyValue.getCovariateKey()).getName();
              return Pair.of(covariateName, covariateStudyValue.getValue());
            })
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    return Pair.of(trialDataStudy.getName(), covariateNodes);
  }

  private List<String> getIncludedCovariateKeys(Project project, NetworkMetaAnalysis analysis) {
    Map<Integer, Covariate> projectCovariatesById = covariateRepository.findByProject(project.getId())
            .stream()
            .collect(toMap(Covariate::getId, identity()));
    return analysis.getCovariateInclusions().stream()
            .map(covariateInclusion -> projectCovariatesById.get(covariateInclusion.getCovariateId()).getDefinitionKey())
            .sorted()
            .collect(toList());
  }
}
