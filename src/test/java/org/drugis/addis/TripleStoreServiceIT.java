package org.drugis.addis;

import net.minidev.json.JSONArray;
import net.minidev.json.parser.ParseException;
import org.drugis.addis.config.TrialverseServiceIntegrationTestConfig;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.model.emun.StudyDataSection;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by connor on 16-4-15.
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TrialverseServiceIntegrationTestConfig.class})
public class TripleStoreServiceIT {

  @Inject
  TriplestoreService triplestoreService;
  private final String testNamespaceUid = "3e6a82b0-582e-4c5d-af93-f1a46a220035";
  private final String testDrug2 = "84260271-5b92-4b89-a714-da982077c383";
  private final String testDrug1 = "32398992-dcfb-4b23-9d49-7ffbf3c4c8ff";
  private final String testVersionHead1 = "http://localhost:8080/versions/85aad3a5-a9cc-46e0-ac60-c6b4adb83a4b";
  private final String testVersionHead2 = "http://localhost:8080/versions/dc660aaf-3b1e-4149-87cf-5b512e0b7283";

  private String nameSpaceUid = "e56ab1ac-9d36-4acf-baf6-0d47b618e817";
  private final String tak491019 = "eaea4081-8da1-4c5b-96e0-aa3077adebca"; //TAK491-019 / NCT00696436
  private final String tak491301 = "733c8344-66d4-4137-b0e5-80762812be2f"; //TAK491-301 / NCT00846365
  private final String tak491008 = "a795e3f0-325e-4172-973f-f25c34a5db96"; //TAK491-008 / NCT00696241
  private final String nonSAE = "2c7f5c18-e7fc-45e5-8e63-2c90c31afaea";
  private final String sbpMeanTroughSitting = "399b6814-8289-482b-87f7-ade143e42791";
  private final String azilsartan = "40d2e6b0-56cc-434b-9199-fde1b5b9a6be";
  private final String placebo = "5af90963-8999-4b6b-a71f-6a21872c649d";
  private final String version = "http://localhost:8080/versions/c81a90be-f0f1-402a-93e1-d6f3c6cf7796";
  private final String testVar2 = "f7ea96f9-029d-4d4d-8a78-ac4c6cc9b464";

  @Test
  public void testGetTreatmentActivitiesFixedDose() {
    List<TreatmentActivity> studyTreatmentActivities = triplestoreService.getStudyTreatmentActivities(nameSpaceUid, tak491019);
    assertEquals(6, studyTreatmentActivities.size());
    TreatmentActivity treatmentActivity = studyTreatmentActivities.get(0); // NB: may change on reimport
    assertEquals(1, treatmentActivity.getAdministeredDrugs().size());
  }

  @Test
  public void testGetTreatmentActivitiesFlexDose() {
    List<TreatmentActivity> studyTreatmentActivities = triplestoreService.getStudyTreatmentActivities(nameSpaceUid, tak491019);
    assertEquals(6, studyTreatmentActivities.size());
    TreatmentActivity treatmentActivity = studyTreatmentActivities.get(0);
    assertEquals(1, treatmentActivity.getAdministeredDrugs().size());
  }

  @Test
  public void testGetTreatmentActivitiesMultidrugArms() {
    List<TreatmentActivity> studyTreatmentActivities = triplestoreService.getStudyTreatmentActivities(nameSpaceUid, tak491301);
    assertEquals(4, studyTreatmentActivities.size());
    TreatmentActivity treatmentActivity = studyTreatmentActivities.get(0);
    assertEquals(2, treatmentActivity.getAdministeredDrugs().size());
  }

  @Test
  public void testGetPopulationCharacteristics() {
    List<StudyData> studyData = triplestoreService.getStudyData(nameSpaceUid, tak491008, StudyDataSection.BASE_LINE_CHARACTERISTICS);
    assertEquals(2, studyData.size());
  }

  @Test
  public void testGetOutcomes() {
    List<SemanticOutcome> outcomes = triplestoreService.getOutcomes(nameSpaceUid, version);
    assertEquals(95, outcomes.size());
  }

  @Test
  public void testGetOutcomesTestStudy() {
    List<SemanticOutcome> outcomes = triplestoreService.getOutcomes(testNamespaceUid, testVersionHead1);
    assertEquals(3, outcomes.size());

  }

  @Test
  public void testGetInterventions() {
    List<SemanticIntervention> interventions = triplestoreService.getInterventions(nameSpaceUid, version);
    assertEquals(7, interventions.size());
  }

  @Test
  public void testGetInterventionsTestStudy() {
    List<SemanticIntervention> interventions = triplestoreService.getInterventions(testNamespaceUid, testVersionHead1);
    assertEquals(2, interventions.size());
  }


  @Test
  public void testGetStudies() {
    List<Study> studies = triplestoreService.queryStudies(nameSpaceUid, version);
    assertEquals(5, studies.size());
    assertEquals(36, studies.get(1).getOutcomeUids().size()); // NB changes on re-import
    assertTrue(studies.get(1).getOutcomeUids().get(0).startsWith("http://trials.drugis.org/entities/"));
  }

  @Test
  public void testGetSingleStudyMeasurements() {
    List<String> outcomeUids = Arrays.asList(sbpMeanTroughSitting, nonSAE);
    List<String> interventionUids = Arrays.asList(azilsartan, placebo);
    List<TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow> singleStudyMeasurements
            = triplestoreService.getSingleStudyMeasurements(nameSpaceUid, tak491008, version, outcomeUids, interventionUids);
    assertEquals(8, singleStudyMeasurements.size());
    assertFalse(singleStudyMeasurements.get(0).getAlternativeUid().startsWith("ontology"));
  }

  @Test
  public void testGetSingleStudyMeasurementsteststudy() {
    String testStudy = "0a333700-89b0-49fd-a423-37a874ff1e6b";
    String var1 = "4cf45ac4-e287-425a-b838-7b2a2ce0d8b7";
    String var3 = "448daf84-d548-48f0-8660-8b848d9edaf2";
    List<String> outcomeUids = Arrays.asList(var1, testVar2, var3);

    String drug2 = testDrug2;
    List<String> interventionUids = Arrays.asList(testDrug1, drug2);

    List<TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow> singleStudyMeasurements
            = triplestoreService.getSingleStudyMeasurements(testNamespaceUid, testStudy, testVersionHead1, outcomeUids, interventionUids);

    assertEquals(4, singleStudyMeasurements.size());
    assertFalse(singleStudyMeasurements.get(0).getAlternativeUid().startsWith("ontology"));
  }

  @Test
  public void testGetTrialDataTestStudies() {
    List<String> testInterventionUids = Arrays.asList(testDrug1, testDrug2);
    List<TrialDataStudy> trialData = triplestoreService.getTrialData(testNamespaceUid, testVersionHead2, testVar2, testInterventionUids);
    assertEquals(3, trialData.size());
  }


  @Test
  public void testGetTrialData() {
    List<String> interventionUids = Arrays.asList(azilsartan, placebo);
    List<TrialDataStudy> trialData = triplestoreService.getTrialData(nameSpaceUid, version, nonSAE, interventionUids);
    assertEquals(4, trialData.size());
  }

  @Test
  public void testNamespaceGet() {
    Namespace namespace = triplestoreService.getNamespaceHead(nameSpaceUid);
    assertNotNull(namespace);
    assertEquals("EDARBI duration update", namespace.getName());
    assertEquals("from -P0D to PT0S", namespace.getDescription());
    assertEquals(new Integer(5), namespace.getNumberOfStudies());
    assertEquals(version, namespace.getVersion());
  }

  @Test
  public void testQueryNamespaces() throws ParseException {
    Collection<Namespace> namespaces = triplestoreService.queryNameSpaces();
    assertEquals(6, namespaces.size());
  }
  
  @Test
  public void getStudyDetailsTest() throws ResourceDoesNotExistException {
    StudyWithDetails studydetails = triplestoreService.getStudydetails(nameSpaceUid, tak491008);
    assertEquals("TAK491-008 / NCT00696241", studydetails.getName());
    assertEquals(new Integer(1275), studydetails.getStudySize());
    assertEquals("Olmesartan, Azilsartan, Placebo", studydetails.getInvestigationalDrugNames());
    assertEquals(new Integer(5), studydetails.getNumberOfArms());
  }

  @Test
  public void testGetEpochs() {
    JSONArray studyEpochs = triplestoreService.getStudyEpochs(nameSpaceUid, tak491008);
    assertEquals(2, studyEpochs.size());
  }

  @Test
  public void testGetArms() {
    JSONArray studyArms = triplestoreService.getStudyArms(nameSpaceUid, tak491008);
    assertEquals(5, studyArms.size());
  }

  @Test
  public void testGetStudiesWithDetails() {
    List<StudyWithDetails> studyWithDetailses = triplestoreService.queryStudydetailsHead(nameSpaceUid);
    assertEquals(5, studyWithDetailses.size());
  }

// @Test
// public  void testGetSingleStudyMeasurements() {
//   String namespaceUid = "namespaceUid";
//   String studyUid = "studUid";
//   String version = "verrsion";
//   List<String> outcomeUids = Arrays.asList();
//   List<String> alternativeUids = Arrays.asList();
//
//   List<TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow> rows = triplestoreService.getSingleStudyMeasurements(namespaceUid, studyUid, version, outcomeUids, alternativeUids);
//   assertNotNull(rows.get(0));
// }
}
