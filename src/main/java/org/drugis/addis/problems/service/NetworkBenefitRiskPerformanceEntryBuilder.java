package org.drugis.addis.problems.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.models.Model;
import org.drugis.addis.problems.model.AbstractBaselineDistribution;
import org.drugis.addis.problems.model.DataSourceEntry;
import org.drugis.addis.problems.model.MultiVariateDistribution;
import org.drugis.addis.problems.model.NMAInclusionWithResults;
import org.drugis.addis.problems.service.model.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NetworkBenefitRiskPerformanceEntryBuilder {
  private ObjectMapper objectMapper = new ObjectMapper();

  public AbstractMeasurementEntry build(NMAInclusionWithResults inclusion, DataSourceEntry dataSource) {
    RelativePerformance performance = getPerformance(inclusion);
    String criterion = inclusion.getOutcome().getSemanticOutcomeUri().toString();
    return new RelativePerformanceEntry(criterion, dataSource.getId(), performance);
  }

  private RelativePerformance getPerformance(NMAInclusionWithResults inclusion) {
    String baselineInterventionId = getBaselineIntervention(inclusion).getId().toString();

    MultiVariateDistribution baselineResults = getBaselineResults(inclusion, baselineInterventionId);
    Set<String> interventionIds = inclusion.getInterventions().stream()
            .map(AbstractIntervention::getId)
            .map(Object::toString)
            .collect(Collectors.toSet());
    Map<String, Double> mu = getMu(baselineInterventionId, baselineResults, interventionIds);

    List<String> interventionIdsWithBaselineFirst = new ArrayList<>(interventionIds);
    // place baseline at the front of the list
    Collections.swap(interventionIdsWithBaselineFirst, 0, interventionIdsWithBaselineFirst.indexOf(baselineInterventionId));

    final List<List<Double>> cov = getCov(interventionIdsWithBaselineFirst, baselineInterventionId, baselineResults);
    return getRelativePerformance(inclusion, mu, interventionIdsWithBaselineFirst, cov);
  }

  private AbstractIntervention getBaselineIntervention(NMAInclusionWithResults inclusion) {
    AbstractBaselineDistribution baselineDistribution = getBaselineDistribution(inclusion);
    return inclusion
            .getInterventions()
            .stream()
            .filter(intervention -> baselineDistribution.getName().equalsIgnoreCase(intervention.getName()))
            .findFirst()
            .orElse(null);
  }


  private AbstractBaselineDistribution getBaselineDistribution(NMAInclusionWithResults outcomeInclusion) {
    try {
      return objectMapper.readValue(outcomeInclusion.getBaseline(), AbstractBaselineDistribution.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private MultiVariateDistribution getBaselineResults(NMAInclusionWithResults outcomeInclusion, String baselineInterventionId) {
    JsonNode taskResults = outcomeInclusion.getPataviResults();

    try {
      Map<String, MultiVariateDistribution> distributionByInterventionId = objectMapper.readValue(
              taskResults.get("multivariateSummary").toString(),
              new TypeReference<Map<String, MultiVariateDistribution>>() {
              });
      if (distributionByInterventionId == null) {
        throw new RuntimeException("cannot read distribution for baseline " + baselineInterventionId);
      }
      return distributionByInterventionId.get(baselineInterventionId);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private Map<String, Double> getMu(String baselineInterventionId, MultiVariateDistribution distribution, Set<String> interventionIds) {
    Map<String, Double> mu = distribution.getMu().entrySet().stream()
            .collect(Collectors.toMap(getTargetIntervention(), Map.Entry::getValue));

    mu = mu.entrySet().stream().filter(m -> interventionIds.contains(m.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    //add baseline to mu
    mu.put(baselineInterventionId, 0.0);
    return mu;
  }

  private RelativePerformance getRelativePerformance(NMAInclusionWithResults outcomeInclusion,
                                                     Map<String, Double> mu, List<String> interventionIds,
                                                     List<List<Double>> data) {
    CovarianceMatrix cov = new CovarianceMatrix(interventionIds, interventionIds, data);
    Relative relative = new Relative("dmnorm", mu, cov);
    RelativePerformanceParameters parameters = new RelativePerformanceParameters(outcomeInclusion.getBaseline(), relative);
    Model outComeInclusionModel = outcomeInclusion.getModel();
    return new RelativePerformance(getModelPerformanceType(outComeInclusionModel.getLink(), outComeInclusionModel.getLikelihood()), parameters);
  }

  private Function<Map.Entry<String, Double>, String> getTargetIntervention() {
    return e -> {
      String key = e.getKey();
      return key.substring(key.lastIndexOf('.') + 1);
    };
  }

  private List<List<Double>> getCov(List<String> interventionIds, String baselineInterventionId, MultiVariateDistribution distribution) {
    Map<Pair<String, String>, Double> effectsByInterventionId = getEffectsByInterventionId(distribution, baselineInterventionId, interventionIds);
    List<List<Double>> cov = new ArrayList<>(interventionIds.size());

    // setup data structure and init with zeroes
    for (int i = 0; i < interventionIds.size(); ++i) {
      List<Double> row = new ArrayList<>(interventionIds.size());
      for (int j = 0; j < interventionIds.size(); ++j) {
        row.add(0.0);
      }
      cov.add(row);
    }

    interventionIds.forEach(rowName ->
            interventionIds
                    .stream()
                    .filter(colName -> !baselineInterventionId.equals(rowName) && !baselineInterventionId.equals(colName))
                    .forEach(colName -> cov
                            .get(interventionIds.indexOf(rowName))
                            .set(interventionIds.indexOf(colName), effectsByInterventionId.get(ImmutablePair.of(rowName, colName))))
    );
    return cov;
  }

  private Map<Pair<String, String>, Double> getEffectsByInterventionId(MultiVariateDistribution distribution, String baselineInterventionId, List<String> interventionIds) {
    Map<Pair<String, String>, Double> dataMap = new HashMap<>();

    final Map<String, Map<String, Double>> sigma = distribution.getSigma();
    for (String rowName : interventionIds) {
      interventionIds
              .stream()
              .filter(columnName ->
                      !columnName.equals(baselineInterventionId) && !rowName.equals(baselineInterventionId))
              .forEach(columnName -> {
                Double value = getSigmaValue(baselineInterventionId, sigma, rowName, columnName);
                dataMap.put(new ImmutablePair<>(columnName, rowName), value);
                dataMap.put(new ImmutablePair<>(rowName, columnName), value);
              });
    }
    return dataMap;
  }

  private Double getSigmaValue(String baselineInterventionId, Map<String, Map<String, Double>> sigma, String interventionY, String interventionX) {
    return sigma
            .get("d." + baselineInterventionId + '.' + interventionX)
            .get("d." + baselineInterventionId + '.' + interventionY);
  }


  private String getModelPerformanceType(String modelLinkType, String modelLikelyHoodType) {
    String modelPerformanceType;
    if (Model.LINK_IDENTITY.equals(modelLinkType)) {
      modelPerformanceType = "relative-normal";
    } else if (Model.LIKELIHOOD_POISSON.equals(modelLikelyHoodType) && Model.LINK_LOG.equals(modelLinkType)) {
      modelPerformanceType = "relative-survival";
    } else {
      modelPerformanceType = "relative-" + modelLinkType + "-normal";
    }
    return modelPerformanceType;
  }
}
