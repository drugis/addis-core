package org.drugis.addis.problems.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.model.ArmExclusion;
import org.drugis.addis.analyses.model.MeasurementMomentInclusion;
import org.drugis.addis.analyses.model.NetworkMetaAnalysis;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.models.Model;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.NetworkMetaAnalysisService;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.trialverse.util.service.UuidService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;
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
  private PataviTaskRepository pataviTaskRepository;

  @Inject
  private UuidService uuidService;

  @Inject
  private NetworkMetaAnalysisEntryBuilder networkPerformanceTableBuilder;


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
            .reduce(new ArrayList<>(), this::listUnion);

    return changeToStdErrEntriesIfNeeded(entries);
  }

  private <T> List<T> listUnion(List<T> list1, List<T> list2) {
    final ArrayList<T> result = new ArrayList<>(list1);
    result.addAll(list2);
    return result;
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
                return networkPerformanceTableBuilder.build(trialDataStudy.getName(),
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
    boolean isStdErrEntry = entries.stream().anyMatch(entry -> entry instanceof ContinuousStdErrEntry);
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
    if (analysis.getCovariateInclusions().size() == 0) {
      return null;
    }

    Collection<Covariate> projectCovariates = covariateRepository.findByProject(project.getId());
    Map<String, Covariate> includedCovariatesByKey = projectCovariates
            .stream()
            .filter(covariate -> isIncluded(analysis, covariate))
            .collect(toMap(Covariate::getDefinitionKey, identity()));

    return studiesWithEntries.stream()
            .map(trialDataStudy -> getStudyValuesByCovariate(trialDataStudy, includedCovariatesByKey))
            .collect(Collectors.toMap(StudyNameAndCovariateNodes::getName, StudyNameAndCovariateNodes::getNodes));
  }

  private boolean isIncluded(NetworkMetaAnalysis analysis, Covariate covariate) {
    return analysis.getCovariateInclusions().stream()
            .anyMatch(inclusion -> inclusion.getCovariateId().equals(covariate.getId()));
  }

  private StudyNameAndCovariateNodes getStudyValuesByCovariate(TrialDataStudy trialDataStudy, Map<String, Covariate> includedCovariatesByKey) {
    Map<String, Double> covariateNodes = trialDataStudy.getCovariateValues().stream()
            .filter(covariateStudyValue -> includedCovariatesByKey.containsKey(covariateStudyValue.getCovariateKey()))
            .map(covariateStudyValue -> {
              String covariateName = includedCovariatesByKey.get(covariateStudyValue.getCovariateKey()).getName();
              return Pair.of(covariateName, covariateStudyValue.getValue());
            })
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    return new StudyNameAndCovariateNodes(trialDataStudy.getName(), covariateNodes);
  }

  private class StudyNameAndCovariateNodes {
    final String name;
    final Map<String, Double> nodes;

    StudyNameAndCovariateNodes(String name, Map<String, Double> nodes) {
      this.name = name;
      this.nodes = nodes;
    }

    public String getName() {
      return name;
    }

    public Map<String, Double> getNodes() {
      return nodes;
    }
  }

  @Override
  public Map<URI, CriterionEntry> buildCriteriaForInclusion(NMAInclusionWithResults inclusionWithResults, URI modelURI) {
    final Map<URI, CriterionEntry> criteria = new HashMap<>();
    CriterionEntry criterionEntry;

    Outcome outcome = inclusionWithResults.getOutcome();
    if ("binom".equals(inclusionWithResults.getModel().getLikelihood())) {
      DataSourceEntry dataSource = new DataSourceEntry(uuidService.generate(), Arrays.asList(0d, 1d),
          /* pvf */ null, "meta analysis", modelURI);
      criterionEntry = new CriterionEntry(
          Collections.singletonList(dataSource),
          outcome.getName());
    } else {
      DataSourceEntry dataSource = new DataSourceEntry(uuidService.generate(), "meta analysis", modelURI);
      criterionEntry = new CriterionEntry(Collections.singletonList(dataSource), outcome.getName());
    }
    criteria.put(outcome.getSemanticOutcomeUri(), criterionEntry);
    return criteria;
  }

  @Override
  public Map<String, AlternativeEntry> buildAlternativesForInclusion(NMAInclusionWithResults inclusionWithResults) {
    return inclusionWithResults.getInterventions().stream()
        .collect(Collectors.toMap(
            intervention -> intervention.getId().toString(),
            intervention -> new AlternativeEntry(intervention.getId(), intervention.getName())));
  }

  @Override
  public Map<Integer, JsonNode> getPataviResultsByModelId(Collection<Model> models) throws IOException, SQLException, UnexpectedNumberOfResultsException, URISyntaxException {
    Map<URI, Model> modelsByTaskUrl = models.stream()
        .filter(model -> model.getTaskUrl() != null)
        .collect(Collectors.toMap(Model::getTaskUrl, identity()));
    List<PataviTask> pataviTasks = pataviTaskRepository.findByUrls(new ArrayList<>(modelsByTaskUrl.keySet()));
    Map<URI, JsonNode> resultsByUrl = pataviTaskRepository.getResults(pataviTasks);
    return resultsByUrl.entrySet().stream()
        .collect(Collectors.toMap(e -> modelsByTaskUrl.get(e.getKey()).getId(), Map.Entry::getValue));
  }
}
