package org.drugis.addis;

import org.drugis.addis.config.TrialverseServiceIntegrationTestConfig;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.model.emun.StudyDataSection;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by connor on 16-4-15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TrialverseServiceIntegrationTestConfig.class})
public class TripleStoreServiceIT {

  @Inject
  TriplestoreService triplestoreService;

  @Test
  public void testGetTreatmentActivitiesFixedDose() {
    String nameSpaceUid = "6292ccea-083c-4941-a3b6-2347f6165755"; // edarbi
    String studyUid = "30b36971-1074-4482-9252-74f6541b566b"; //TAK491-008 / NCT00696241
    List<TreatmentActivity> studyTreatmentActivities = triplestoreService.getStudyTreatmentActivities(nameSpaceUid, studyUid);
    assertEquals(6, studyTreatmentActivities.size());
    TreatmentActivity treatmentActivity = studyTreatmentActivities.get(1);
    assertEquals(1, treatmentActivity.getAdministeredDrugs().size());
  }

  @Test
  public void testGetTreatmentActivitiesFlexDose() {
    String nameSpaceUid = "6292ccea-083c-4941-a3b6-2347f6165755"; // edarbi
    String studyUid = "89b86b85-ea02-4a43-bc18-17dcca9f9c9a"; //TAK491-019 / NCT00696436
    List<TreatmentActivity> studyTreatmentActivities = triplestoreService.getStudyTreatmentActivities(nameSpaceUid, studyUid);
    assertEquals(6, studyTreatmentActivities.size());
    TreatmentActivity treatmentActivity = studyTreatmentActivities.get(1);
    assertEquals(1, treatmentActivity.getAdministeredDrugs().size());
  }

  @Test
  public void testGetTreatmentActivitiesMultidrugArms() {
    String nameSpaceUid = "6292ccea-083c-4941-a3b6-2347f6165755"; // edarbi
    String studyUid = "f0b7c1e7-b3e9-4a45-9acc-0e612d43c4e2"; //TAK491-301 / NCT00846365
    List<TreatmentActivity> studyTreatmentActivities = triplestoreService.getStudyTreatmentActivities(nameSpaceUid, studyUid);
    assertEquals(4, studyTreatmentActivities.size());
    TreatmentActivity treatmentActivity = studyTreatmentActivities.get(1);
    assertEquals(2, treatmentActivity.getAdministeredDrugs().size());
  }

  @Test
  public void testGetPopulationCharacteristics() {
    String nameSpaceUid = "6292ccea-083c-4941-a3b6-2347f6165755"; // edarbi
    String studyUid = "30b36971-1074-4482-9252-74f6541b566b"; //TAK491-008 / NCT00696241
    List<StudyData> studyData = triplestoreService.getStudyData(nameSpaceUid, studyUid, StudyDataSection.BASE_LINE_CHARACTERISTICS);
    assertEquals(2, studyData.size());
  }

  @Test
  public void testGetOutcomes() {
    String nameSpaceUid = "6292ccea-083c-4941-a3b6-2347f6165755"; // edarbi
    String version = "http://drugis.org/eventSourcing/event/15b24e23-f9fc-47d0-80ae-471df7830ba2";
    List<SemanticOutcome> outcomes = triplestoreService.getOutcomes(nameSpaceUid, version);
    assertEquals(95, outcomes.size());
  }

  @Test
  public void testGetInterventions() {
    String nameSpaceUid = "6292ccea-083c-4941-a3b6-2347f6165755"; // edarbi
    String version = "http://drugis.org/eventSourcing/event/15b24e23-f9fc-47d0-80ae-471df7830ba2";
    List<SemanticIntervention> interventions = triplestoreService.getInterventions(nameSpaceUid, version);
    assertEquals(7, interventions.size());
  }

  @Test
  public void testGetStudies() {
    String nameSpaceUid = "6292ccea-083c-4941-a3b6-2347f6165755"; // edarbi
    String version = "http://drugis.org/eventSourcing/event/15b24e23-f9fc-47d0-80ae-471df7830ba2";
    List<Study> studies = triplestoreService.queryStudies(nameSpaceUid, version);
    assertEquals(5, studies.size());
    assertEquals(59, studies.get(0).getOutcomeUids().size());
    assertTrue(studies.get(0).getOutcomeUids().get(0).startsWith("http://trials.drugis.org/entities/"));
  }

  @Test
  public void testGetSingleStudyMeasurements() {
    String studyUid = "30b36971-1074-4482-9252-74f6541b566b"; //TAK491-008 / NCT00696241
    String version = "http://drugis.org/eventSourcing/event/15b24e23-f9fc-47d0-80ae-471df7830ba2";

    String nonSAE = "832c2831-dab2-4707-8bf4-af0cf057ba5a";
    String SBPMeanTroughSitting = "151afdaf-ed92-4204-a389-a3e125e0f19d";
    List<String> outcomeUids = Arrays.asList(SBPMeanTroughSitting, nonSAE);
    String azilsartan = "f1b57b39-1887-4547-aaf3-7699ed37463b";
    String placebo = "dce4e0b8-57a4-438b-9001-742fcc1b9c8b";
    List<String> interventionUids = Arrays.asList(azilsartan, placebo);
    List<TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow> singleStudyMeasurements = triplestoreService.getSingleStudyMeasurements(studyUid, version, outcomeUids, interventionUids);
    assertEquals(8, singleStudyMeasurements.size());
    assertFalse(singleStudyMeasurements.get(0).getAlternativeUid().startsWith("ontology"));
  }

  @Test
  public void testGetTrialData() {
    String nameSpaceUid = "6292ccea-083c-4941-a3b6-2347f6165755"; // edarbi
    String version = "http://drugis.org/eventSourcing/event/15b24e23-f9fc-47d0-80ae-471df7830ba2";
    String nonSAE = "832c2831-dab2-4707-8bf4-af0cf057ba5a";
    String azilsartan = "f1b57b39-1887-4547-aaf3-7699ed37463b";
    String placebo = "dce4e0b8-57a4-438b-9001-742fcc1b9c8b";
    List<String> interventionUids = Arrays.asList(azilsartan, placebo);
    List<TrialDataStudy> trialData = triplestoreService.getTrialData(nameSpaceUid, version, nonSAE, interventionUids);
    assertEquals(5, trialData.size());
  }

}
