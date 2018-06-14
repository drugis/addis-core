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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PerformanceTableEntryBuilder {
  private final Map<Integer, Model> modelsById;
  private final Map<Integer, Outcome> outcomesById;
  private final Map<String, DataSourceEntry> dataSourcesByOutcomeId;
  private final Map<Integer, PataviTask> tasksByModelId;
  private final Map<URI, JsonNode> resultsByTaskUrl;
  private final Map<String, AbstractIntervention> includedInterventionsById;
  private final Map<String, AbstractIntervention> includedInterventionsByName;
  private ObjectMapper objectMapper = new ObjectMapper();

  public PerformanceTableEntryBuilder(Map<Integer,Model> modelsById, Map<Integer,Outcome> outcomesById, Map<String,DataSourceEntry> dataSourcesByOutcomeId, Map<Integer,PataviTask> tasksByModelId, Map<URI,JsonNode> resultsByTaskUrl, Map<String,AbstractIntervention> includedInterventionsById, Map<String,AbstractIntervention> includedInterventionsByName) {
    this.modelsById = modelsById;
    this.outcomesById = outcomesById;
    this.dataSourcesByOutcomeId = dataSourcesByOutcomeId;
    this.tasksByModelId = tasksByModelId;
    this.resultsByTaskUrl = resultsByTaskUrl;
    this.includedInterventionsById = includedInterventionsById;
    this.includedInterventionsByName = includedInterventionsByName;
  }

  @SuppressWarnings("SuspiciousNameCombination")
  public AbstractMeasurementEntry build(BenefitRiskNMAOutcomeInclusion outcomeInclusion) {
    AbstractBaselineDistribution tempBaseline = null;
    try {
      tempBaseline = objectMapper.readValue(outcomeInclusion.getBaseline(), AbstractBaselineDistribution.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    final AbstractBaselineDistribution baselineDistribution = tempBaseline;
    assert baselineDistribution != null;

    URI taskUrl = tasksByModelId.get(outcomeInclusion.getModelId()).getSelf();
    JsonNode taskResults = resultsByTaskUrl.get(taskUrl);

    Map<Integer, MultiVariateDistribution> distributionByInterventionId = null;
    try {
      distributionByInterventionId = objectMapper.readValue(
          taskResults.get("multivariateSummary").toString(),
          new TypeReference<Map<Integer, MultiVariateDistribution>>() {
          });
    } catch (IOException e) {
      e.printStackTrace();
    }
    assert distributionByInterventionId != null;

    AbstractIntervention baselineIntervention = includedInterventionsByName.get(baselineDistribution.getName());
    MultiVariateDistribution distribution = distributionByInterventionId.get(baselineIntervention.getId());

    Map<String, Double> mu = distribution.getMu().entrySet().stream()
        .collect(Collectors.toMap(
            e -> {
              String key = e.getKey();
              return key.substring(key.lastIndexOf('.') + 1);
            },
            Map.Entry::getValue));

    // filter mu
    mu = mu.entrySet().stream().filter(m -> includedInterventionsById.containsKey(m.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    //add baseline to mu
    String baselineInterventionId = baselineIntervention.getId().toString();
    mu.put(baselineInterventionId, 0.0);

    List<String> rowNames = new ArrayList<>(mu.keySet());

    // place baseline at the front of the list
    //noinspection ComparatorMethodParameterNotUsed
    rowNames.sort((rn1, rn2) -> rn1.equals(includedInterventionsByName.get(baselineDistribution.getName()).getId().toString()) ? -1 : 0);

    Map<Pair<String, String>, Double> dataMap = new HashMap<>();

    final Map<String, Map<String, Double>> sigma = distribution.getSigma();
    for (String interventionY : rowNames) {
      rowNames
          .stream()
          .filter(interventionX ->
              !interventionX.equals(baselineInterventionId) && !interventionY.equals(baselineInterventionId))
          .forEach(interventionX -> {
            Double value = sigma
                .get("d." + baselineInterventionId + '.' + interventionX)
                .get("d." + baselineInterventionId + '.' + interventionY);
            dataMap.put(new ImmutablePair<>(interventionX, interventionY), value);
            dataMap.put(new ImmutablePair<>(interventionY, interventionX), value);
          });
    }

    final List<List<Double>> data = new ArrayList<>(rowNames.size());

    // setup data structure and init with null values
    for (int i = 0; i < rowNames.size(); ++i) {
      List<Double> row = new ArrayList<>(rowNames.size());
      for (int j = 0; j < rowNames.size(); ++j) {
        row.add(0.0);
      }
      data.add(row);
    }

    rowNames.forEach(rowName ->
        rowNames
            .stream()
            .filter(colName -> !baselineInterventionId.equals(rowName) && !baselineInterventionId.equals(colName))
            .forEach(colName -> data
                .get(rowNames.indexOf(rowName))
                .set(rowNames.indexOf(colName), dataMap.get(ImmutablePair.of(rowName, colName))))
    );

    CovarianceMatrix cov = new CovarianceMatrix(rowNames, rowNames, data);
    Relative relative = new Relative("dmnorm", mu, cov);
    RelativePerformanceParameters parameters =
        new RelativePerformanceParameters(outcomeInclusion.getBaseline(), relative);
    String modelLinkType = modelsById.get(outcomeInclusion.getModelId()).getLink();

    String modelPerformanceType;
    if (Model.LINK_IDENTITY.equals(modelLinkType)) {
      modelPerformanceType = "relative-normal";
    } else if (Model.LIKELIHOOD_POISSON.equals(modelsById.get(outcomeInclusion.getModelId()).getLikelihood())) {
      modelPerformanceType = "relative-survival";
    } else {
      modelPerformanceType = "relative-" + modelLinkType + "-normal";
    }

    RelativePerformance performance = new RelativePerformance(modelPerformanceType, parameters);

    String criterion = outcomesById.get(outcomeInclusion.getOutcomeId()).getSemanticOutcomeUri().toString();
    String dataSource = dataSourcesByOutcomeId.get(criterion).getId();
    return new RelativePerformanceEntry(criterion, dataSource, performance);
  }
}
