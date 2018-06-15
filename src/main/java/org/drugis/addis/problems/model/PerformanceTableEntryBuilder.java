package org.drugis.addis.problems.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.model.BenefitRiskNMAOutcomeInclusion;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.models.Model;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.problems.service.model.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PerformanceTableEntryBuilder {
  private final Map<Integer, Model> modelsById;
  private final Map<Integer, Outcome> outcomesById;
  private final Map<String, DataSourceEntry> dataSourcesByOutcomeId;
  private final Map<Integer, PataviTask> tasksByModelId;
  private final Map<URI, JsonNode> resultsByTaskUrl;
  private final Map<String, AbstractIntervention> includedInterventionsByName;
  private ObjectMapper objectMapper = new ObjectMapper();

  public PerformanceTableEntryBuilder(Map<Integer, Model> modelsById, Map<Integer, Outcome> outcomesById, Map<String, DataSourceEntry> dataSourcesByOutcomeId, Map<Integer, PataviTask> tasksByModelId, Map<URI, JsonNode> resultsByTaskUrl, Map<String, AbstractIntervention> includedInterventionsByName) {
    this.modelsById = modelsById;
    this.outcomesById = outcomesById;
    this.dataSourcesByOutcomeId = dataSourcesByOutcomeId;
    this.tasksByModelId = tasksByModelId;
    this.resultsByTaskUrl = resultsByTaskUrl;
    this.includedInterventionsByName = includedInterventionsByName;
  }

  public AbstractMeasurementEntry build(BenefitRiskNMAOutcomeInclusion outcomeInclusion) {
    String baselineInterventionId = getBaselineInterventionId(outcomeInclusion);

    MultiVariateDistribution baselineResults = getBaselineResults(outcomeInclusion, baselineInterventionId);
    Map<String, Double> mu = getMu(baselineInterventionId, baselineResults);

    List<String> orderedInterventionIds = getOrderedInterventionIds(baselineInterventionId, mu);

    final List<List<Double>> data = getData(orderedInterventionIds, baselineInterventionId, baselineResults);

    RelativePerformance performance = getRelativePerformance(outcomeInclusion, mu, orderedInterventionIds, data);
    String criterion = outcomesById.get(outcomeInclusion.getOutcomeId()).getSemanticOutcomeUri().toString();
    String dataSource = dataSourcesByOutcomeId.get(criterion).getId();

    return new RelativePerformanceEntry(criterion, dataSource, performance);
  }

  private String getBaselineInterventionId(BenefitRiskNMAOutcomeInclusion outcomeInclusion) {
    AbstractIntervention baselineIntervention = getBaselineIntervention(outcomeInclusion);
    return baselineIntervention.getId().toString();
  }

  private AbstractIntervention getBaselineIntervention(BenefitRiskNMAOutcomeInclusion outcomeInclusion) {
    final AbstractBaselineDistribution baselineDistribution = getBaselineDistribution(outcomeInclusion);
    return includedInterventionsByName.get(baselineDistribution.getName());
  }

  private List<String> getOrderedInterventionIds(String baselineInterventionId, Map<String, Double> mu) {
    List<String> interventionIds = new ArrayList<>(mu.keySet());
    Integer baselineIndex = interventionIds.indexOf(baselineInterventionId);
    // place baseline at the front of the list
    Collections.swap(interventionIds, 0, baselineIndex);
    return interventionIds;
  }

  private RelativePerformance getRelativePerformance(BenefitRiskNMAOutcomeInclusion outcomeInclusion,
                                                     Map<String, Double> mu, List<String> interventionIds,
                                                     List<List<Double>> data) {
    CovarianceMatrix cov = new CovarianceMatrix(interventionIds, interventionIds, data);
    Relative relative = new Relative("dmnorm", mu, cov);
    RelativePerformanceParameters parameters = new RelativePerformanceParameters(outcomeInclusion.getBaseline(), relative);
    String modelLinkType = modelsById.get(outcomeInclusion.getModelId()).getLink();

    return new RelativePerformance(getModelPerformanceType(modelLinkType), parameters);
  }

  private Map<String, Double> getMu(String baselineInterventionId, MultiVariateDistribution distribution) {
    Map<String, Double> mu = distribution.getMu().entrySet().stream()
        .collect(Collectors.toMap(getTargetIntervention(), Map.Entry::getValue));

    mu = mu.entrySet().stream().filter(m -> isIncluded(m.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    //add baseline to mu
    mu.put(baselineInterventionId, 0.0);
    return mu;
  }

  private Function<Map.Entry<String, Double>, String> getTargetIntervention() {
    return e -> {
      String key = e.getKey();
      return key.substring(key.lastIndexOf('.') + 1);
    };
  }

  private List<List<Double>> getData(List<String> interventionIds, String baselineInterventionId, MultiVariateDistribution distribution) {
    Map<Pair<String, String>, Double> effectsByInterventionId = getEffectsByInterventionId(distribution, baselineInterventionId, interventionIds);

    List<List<Double>> data = new ArrayList<>(interventionIds.size());

    // setup data structure and init with zeroes
    for (int i = 0; i < interventionIds.size(); ++i) {
      List<Double> row = new ArrayList<>(interventionIds.size());
      for (int j = 0; j < interventionIds.size(); ++j) {
        row.add(0.0);
      }
      data.add(row);
    }

    interventionIds.forEach(rowName ->
        interventionIds
            .stream()
            .filter(colName -> !baselineInterventionId.equals(rowName) && !baselineInterventionId.equals(colName))
            .forEach(colName -> data
                .get(interventionIds.indexOf(rowName))
                .set(interventionIds.indexOf(colName), effectsByInterventionId.get(ImmutablePair.of(rowName, colName))))
    );
    return data;
  }

  private boolean isIncluded(String interventionId) {
    return includedInterventionsByName.entrySet().stream().anyMatch(entry -> entry.getValue().getId().toString().equals(interventionId));
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


  private MultiVariateDistribution getBaselineResults(BenefitRiskNMAOutcomeInclusion outcomeInclusion, String baselineInterventionId) {
    URI taskUrl = tasksByModelId.get(outcomeInclusion.getModelId()).getSelf();
    JsonNode taskResults = resultsByTaskUrl.get(taskUrl);

    Map<String, MultiVariateDistribution> distributionByInterventionId = null;
    try {
      distributionByInterventionId = objectMapper.readValue(
          taskResults.get("multivariateSummary").toString(),
          new TypeReference<Map<String, MultiVariateDistribution>>() {
          });
    } catch (IOException e) {
      e.printStackTrace();
    }
    assert distributionByInterventionId != null;
    return distributionByInterventionId.get(baselineInterventionId);
  }

  private AbstractBaselineDistribution getBaselineDistribution(BenefitRiskNMAOutcomeInclusion outcomeInclusion) {
    AbstractBaselineDistribution baselineDistribution = null;
    try {
      baselineDistribution = objectMapper.readValue(outcomeInclusion.getBaseline(), AbstractBaselineDistribution.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return baselineDistribution;
  }

  private String getModelPerformanceType(String modelLinkType) {
    String modelPerformanceType;
    if (Model.LINK_IDENTITY.equals(modelLinkType)) {
      modelPerformanceType = "relative-normal";
    } else if (Model.LIKELIHOOD_POISSON.equals(modelLinkType)) {
      modelPerformanceType = "relative-survival";
    } else {
      modelPerformanceType = "relative-" + modelLinkType + "-normal";
    }
    return modelPerformanceType;
  }
}
