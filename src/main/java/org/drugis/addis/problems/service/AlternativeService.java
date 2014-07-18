package org.drugis.addis.problems.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.problems.model.AlternativeEntry;
import org.drugis.addis.problems.model.Arm;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by connor on 28/03/14.
 */
@Service
public class AlternativeService {

  @Inject
  private TriplestoreService triplestoreService;

  @Inject
  private TrialverseService trialverseService;

  private ObjectMapper mapper = new ObjectMapper();

  /**
   * Pair study arms with alternatives defined in the analysis.
   * Make the alternatives accessible by arm ID.
   *
   * @return A map keyed with arm IDS with the alternatives as values
   */
  public Map<Long, AlternativeEntry> createAlternatives(Project project, SingleStudyBenefitRiskAnalysis analysis) {

    Map<String, Intervention> interventionsByUri = new HashMap<>();
    for (Intervention intervention : analysis.getSelectedInterventions()) {
      interventionsByUri.put(intervention.getSemanticInterventionUri(), intervention);
    }

    System.out.println("DEBUG interventionsByUri : " + interventionsByUri);

    Map<Long, String> drugs = triplestoreService.getTrialverseDrugs(project.getNamespaceUid(), analysis.getStudyId().longValue(), interventionsByUri.keySet());
    List<ObjectNode> jsonArms = trialverseService.getArmsByDrugIds(analysis.getStudyId(), drugs.keySet());

    System.out.println("DEBUG drug ids : " + drugs);

    Map<Long, AlternativeEntry> alternativesCache = new HashMap<>();

    for (ObjectNode jsonArm : jsonArms) {
      Arm arm = mapper.convertValue(jsonArm, Arm.class);
      String drugUUID = drugs.get(arm.getDrugId());
      Intervention intervention = interventionsByUri.get(drugUUID);
      AlternativeEntry alternativeEntry = new AlternativeEntry(intervention.getSemanticInterventionUri(), intervention.getName());
      alternativesCache.put(arm.getId(), alternativeEntry);
    }

    return alternativesCache;
  }
}
