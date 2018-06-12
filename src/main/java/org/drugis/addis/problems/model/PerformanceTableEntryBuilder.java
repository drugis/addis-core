package org.drugis.addis.problems.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.drugis.addis.analyses.model.BenefitRiskNMAOutcomeInclusion;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.models.Model;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;

import java.net.URI;
import java.util.Map;

public class PerformanceTableEntryBuilder {
  private final Map<Integer, Model> modelsById;
  private final Map<Integer, Outcome> outcomesById;
  private final Map<String, DataSourceEntry> dataSourcesByOutcomeId;
  private final Map<Integer, PataviTask> tasksByModelId;
  private final Map<URI, JsonNode> resultsByTaskUrl;
  private final Map<String, AbstractIntervention> includedInterventionsById;
  private final Map<String, AbstractIntervention> includedInterventionsByName;

  public PerformanceTableEntryBuilder(Map<Integer,Model> modelsById, Map<Integer,Outcome> outcomesById, Map<String,DataSourceEntry> dataSourcesByOutcomeId, Map<Integer,PataviTask> tasksByModelId, Map<URI,JsonNode> resultsByTaskUrl, Map<String,AbstractIntervention> includedInterventionsById, Map<String,AbstractIntervention> includedInterventionsByName) {
    this.modelsById = modelsById;
    this.outcomesById = outcomesById;
    this.dataSourcesByOutcomeId = dataSourcesByOutcomeId;
    this.tasksByModelId = tasksByModelId;
    this.resultsByTaskUrl = resultsByTaskUrl;
    this.includedInterventionsById = includedInterventionsById;
    this.includedInterventionsByName = includedInterventionsByName;
  }

  public AbstractMeasurementEntry build(BenefitRiskNMAOutcomeInclusion outcomeInclusion) {
    return null; // TODO
  }
}
