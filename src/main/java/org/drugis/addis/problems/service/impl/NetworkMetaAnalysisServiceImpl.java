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
import org.drugis.addis.problems.model.problemEntry.AbsoluteContinuousProblemEntry;
import org.drugis.addis.problems.model.problemEntry.AbsoluteContinuousStdErrProblemEntry;
import org.drugis.addis.problems.model.problemEntry.AbstractProblemEntry;
import org.drugis.addis.problems.model.problemEntry.RelativeDataEntry;
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
  private NetworkMetaAnalysisEntryBuilder networkMetaAnalysisEntryBuilder;


  @Override
  public List<TreatmentEntry> getTreatments(NetworkMetaAnalysis analysis) throws ResourceDoesNotExistException {
    Set<AbstractIntervention> includedInterventions = analysisService.getIncludedInterventions(analysis);

    return includedInterventions.stream()
            .map(intervention -> new TreatmentEntry(intervention.getId(), intervention.getName()))
            .collect(toList());
  }

  @Override
  public List<AbstractProblemEntry> buildAbsolutePerformanceEntries(
          NetworkMetaAnalysis analysis,
          List<TrialDataStudy> studies
  ) {
    final Map<URI, URI> selectedMeasurementMomentsByStudy = getIncludedMeasurementMomentsByStudy(analysis);
    List<URI> excludedArmUris = getExcludedArmUris(analysis);
    Outcome outcome = analysis.getOutcome();
    List<AbstractProblemEntry> entries = getAbsoluteEntries(studies, selectedMeasurementMomentsByStudy, excludedArmUris, outcome);
    return changeToStdErrEntriesIfNeeded(entries);
  }

  private List<AbstractProblemEntry> getAbsoluteEntries(
          List<TrialDataStudy> studies,
          Map<URI, URI> selectedMeasurementMomentsByStudy,
          List<URI> excludedArmUris,
          Outcome outcome
  ) {
    return studies
            .stream()
            .map(study -> armsToAbsoluteEntries(selectedMeasurementMomentsByStudy, excludedArmUris, study, outcome))
            .reduce(new ArrayList<>(), this::listUnion);
  }

  private List<URI> getExcludedArmUris(NetworkMetaAnalysis analysis) {
    return analysis
            .getExcludedArms()
            .stream()
            .map(ArmExclusion::getTrialverseUid)
            .collect(toList());
  }

  private Map<URI, URI> getIncludedMeasurementMomentsByStudy(NetworkMetaAnalysis analysis) {
    return analysis.getIncludedMeasurementMoments()
            .stream()
            .collect(toMap(MeasurementMomentInclusion::getStudy, MeasurementMomentInclusion::getMeasurementMoment));
  }

  private List<AbstractProblemEntry> armsToAbsoluteEntries(
          Map<URI, URI> selectedMeasurementMomentsByStudy,
          List<URI> excludedArmUris,
          TrialDataStudy study,
          Outcome outcome
  ) {
    final URI selectedMeasurementMoment = getSelectedMeasurementMoment(
            selectedMeasurementMomentsByStudy, study);

    List<TrialDataArm> filteredArms = filterAbsoluteArms(excludedArmUris, study, selectedMeasurementMoment, outcome);

    // do not include studies with fewer than two included and matched arms
    if (filteredArms.size() > 1) {
      return filteredArms.stream()
              .map(arm -> {
                Set<Measurement> measurements = arm.getMeasurementsForMoment(selectedMeasurementMoment);
                return networkMetaAnalysisEntryBuilder.buildAbsoluteEntry(study.getName(),
                        getProjectInterventionId(arm), // safe because we filter unmatched arms
                        measurements.iterator().next()); // nma has exactly one measurement
              })
              .collect(toList());
    }
    return new ArrayList<>();
  }

  @Override
  public RelativeEffectData buildRelativeEffectData(NetworkMetaAnalysis analysis, List<TrialDataStudy> studies) {
    Map<URI, RelativeDataEntry> data = buildRelativeDataEntries(studies, analysis);
    return new RelativeEffectData(data);
  }

  private Map<URI, RelativeDataEntry> buildRelativeDataEntries(
          List<TrialDataStudy> studies,
          NetworkMetaAnalysis analysis

  ) {
    Map<URI, RelativeDataEntry> entries = new HashMap<>();
    studies.forEach(study -> {
      RelativeDataEntry entry = buildRelativeDataEntry(
              study, analysis);
      if (entry != null) {
        entries.put(study.getStudyUri(), entry);
      }
    });
    return entries;
  }

  private RelativeDataEntry buildRelativeDataEntry(
          TrialDataStudy study,
          NetworkMetaAnalysis analysis
  ) {
    final Map<URI, URI> momentsByStudy = getIncludedMeasurementMomentsByStudy(analysis);
    List<URI> excludedArms = getExcludedArmUris(analysis);
    Outcome outcome = analysis.getOutcome();
    final URI selectedMoment = getSelectedMeasurementMoment(momentsByStudy, study);
    List<TrialDataArm> relativeArms = filterRelativeArms(study, excludedArms, selectedMoment, outcome);
    if (relativeArms.size() == 0) {
      return null;
    }
    URI referenceArmUri = getReferenceArmUri(relativeArms, selectedMoment, outcome);
    TrialDataArm referenceArm = getReferenceArm(referenceArmUri, study);

    List<AbstractProblemEntry> otherArms = buildOtherArms(relativeArms, study, selectedMoment);
    Double referenceArmStdErr = getReferenceStdErr(outcome, selectedMoment, relativeArms);
    Integer referenceArmId = getProjectInterventionId(referenceArm);
    return new RelativeDataEntry(referenceArmId, referenceArmStdErr, otherArms);
  }

  private Double getReferenceStdErr(Outcome outcome, URI selectedMoment, List<TrialDataArm> relativeArms) {
    Double referenceArmStdErr = 0.0;
    Set<Measurement> measurements = relativeArms.get(0).getMeasurementsForMoment(selectedMoment);
    for (Measurement measurement : measurements) {
      if (isMeasurementForOutcome(outcome, measurement)) {
        referenceArmStdErr = measurement.getReferenceStdErr();
        break;
      }
    }
    return referenceArmStdErr;
  }

  private boolean isMeasurementForOutcome(Outcome outcome, Measurement measurement) {
    return measurement.getVariableConceptUri().equals(outcome.getSemanticOutcomeUri());
  }

  private Integer getProjectInterventionId(TrialDataArm referenceArm) {
    return referenceArm.getMatchedProjectInterventionIds().iterator().next();
  }

  private URI getReferenceArmUri(List<TrialDataArm> arms, URI defaultMoment, Outcome outcome) {
    Set<Measurement> measurements = arms.get(0).getMeasurementsForMoment(defaultMoment);
    if (measurements.isEmpty()) {
      measurements = arms.get(1).getMeasurementsForMoment(defaultMoment);
    }
    for (Measurement measurement : measurements) {
      if (isMeasurementForOutcome(outcome, measurement)) {
        return measurement.getReferenceArm();
      }
    }
    return null;
  }

  private TrialDataArm getReferenceArm(URI referenceArm, TrialDataStudy study) {
    return study.getArms()
            .stream()
            .filter(arm -> arm.getUri().equals(referenceArm))
            .collect(Collectors.toList())
            .get(0);
  }

  private List<AbstractProblemEntry> buildOtherArms(List<TrialDataArm> filteredArms, TrialDataStudy study, URI selectMoment) {
    if (filteredArms.size() > 0) {
      return filteredArms.stream()
              .map(arm -> {
                Set<Measurement> measurements = arm.getMeasurementsForMoment(selectMoment);
                return networkMetaAnalysisEntryBuilder.buildContrastEntry(
                        study.getName(),
                        getProjectInterventionId(arm), // safe because we filter unmatched arms
                        measurements.iterator().next()
                ); // nma has exactly one measurement
              })
              .collect(toList());
    }
    return new ArrayList<>();
  }

  private List<TrialDataArm> filterRelativeArms(
          TrialDataStudy trialDataStudy,
          List<URI> excludedArmUris,
          URI selectedMoment,
          Outcome outcome
  ) {
    List<TrialDataArm> relativeArms = trialDataStudy.getArms()
            .stream()
            .filter(arm -> !isAbsoluteEffectArm(arm, outcome, selectedMoment))
            .filter(arm -> isNonReferenceArm(arm, selectedMoment))
            .collect(toList());
    return filterArms(excludedArmUris, selectedMoment, relativeArms);
  }

  private List<TrialDataArm> filterArms(
          List<URI> excludedArmUris, URI selectedMoment, List<TrialDataArm> relativeArms) {
    return relativeArms.stream()
            .filter(arm -> hasMeasurements(selectedMoment, arm))
            .filter(arm -> isArmExcluded(excludedArmUris, arm))
            .filter(this::isArmUnambiguous)
            .collect(toList());
  }

  private boolean hasMeasurements(URI selectedMoment, TrialDataArm arm) {
    return arm.getMeasurementsForMoment(selectedMoment) != null;
  }

  private boolean isArmUnambiguous(TrialDataArm arm) {
    return arm.getMatchedProjectInterventionIds().size() == 1;
  }

  private boolean isArmExcluded(List<URI> excludedArmUris, TrialDataArm arm) {
    return !excludedArmUris.contains(arm.getUri());
  }

  private boolean isNonReferenceArm(TrialDataArm arm, URI selectedMoment) {
    return arm.getMeasurementsForMoment(selectedMoment) != null;
  }

  private <T> List<T> listUnion(List<T> list1, List<T> list2) {
    final ArrayList<T> result = new ArrayList<>(list1);
    result.addAll(list2);
    return result;
  }

  private List<TrialDataArm> filterAbsoluteArms(
          List<URI> excludedArmUris,
          TrialDataStudy study,
          URI selectedMoment,
          Outcome outcome) {
    List<TrialDataArm> absoluteArms = study.getArms()
            .stream()
            .filter(arm -> isAbsoluteEffectArm(arm, outcome, selectedMoment))
            .collect(toList());
    return filterArms(excludedArmUris, selectedMoment, absoluteArms);
  }

  private boolean isAbsoluteEffectArm(TrialDataArm arm, Outcome outcome, URI selectedMoment) {
    Set<Measurement> measurements = arm.getMeasurementsForMoment(selectedMoment);
    if (measurements == null) {
      return false;
    }
    for (Measurement measurement : measurements) {
      Boolean isContrastMeasurement = measurement.getReferenceArm() != null && !measurement.getArmUri().equals(measurement.getReferenceArm());
      if (isMeasurementForOutcome(outcome, measurement) && isContrastMeasurement) {
        return false;
      }
    }
    return true;
  }

  private URI getSelectedMeasurementMoment(Map<URI, URI> momentsByStudy, TrialDataStudy study) {
    return momentsByStudy.get(study.getStudyUri()) == null
            ? study.getDefaultMeasurementMoment()
            : momentsByStudy.get(study.getStudyUri());
  }

  private List<AbstractProblemEntry> changeToStdErrEntriesIfNeeded(List<AbstractProblemEntry> entries) {
    // if there's an entry with missing standard deviation or sample size, move everything to standard error
    boolean isStdErrEntry = entries.stream().anyMatch(entry -> entry instanceof AbsoluteContinuousStdErrProblemEntry);
    if (isStdErrEntry) {
      entries = entries.stream().map(entry -> {
        if (entry instanceof AbsoluteContinuousStdErrProblemEntry) {
          return entry;
        }
        AbsoluteContinuousProblemEntry tmpEntry = (AbsoluteContinuousProblemEntry) entry;
        Double stdErr = tmpEntry.getStdDev() / Math.sqrt(tmpEntry.getSampleSize());
        return new AbsoluteContinuousStdErrProblemEntry(tmpEntry.getStudy(), tmpEntry.getTreatment(), tmpEntry.getMean(), stdErr);
      }).collect(toList());
    }
    return entries;
  }

  @Override
  public List<TrialDataStudy> getStudiesWithEntries(List<TrialDataStudy> trialDataStudies, List<AbstractProblemEntry> entries) {
    Set<String> studyNames = entries.stream()
            .map(AbstractProblemEntry::getStudy)
            .collect(Collectors.toSet());
    return trialDataStudies.stream()
            .filter(study -> studyNames.contains(study.getName()))
            .collect(toList());
  }

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
    String modelTitle = inclusionWithResults.getModel().getTitle();
    if ("binom".equals(inclusionWithResults.getModel().getLikelihood())) {
      DataSourceEntry dataSource = new DataSourceEntry(
              uuidService.generate(),
              Arrays.asList(0d, 1d),
              modelTitle,
              modelURI);
      criterionEntry = new CriterionEntry(
              Collections.singletonList(dataSource),
              outcome.getName());
    } else {
      DataSourceEntry dataSource = new DataSourceEntry(uuidService.generate(), modelTitle, modelURI);
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
                    intervention -> new AlternativeEntry(intervention.getName())));
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
