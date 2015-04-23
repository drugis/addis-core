package org.drugis.addis;

import net.minidev.json.JSONArray;
import org.drugis.addis.config.TrialverseServiceIntegrationTestConfig;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.Project;
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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TrialverseServiceIntegrationTestConfig.class})
public class TripleStoreServiceIT {

  @Inject
  TriplestoreService triplestoreService;

  private String nameSpaceUid = "f833282f-9090-4b50-87ea-5f6a12dc6e19";

  @Test
  public void testGetTreatmentActivitiesFixedDose() {

    String studyUid = "30b36971-1074-4482-9252-74f6541b566b"; //TAK491-008 / NCT00696241
    List<TreatmentActivity> studyTreatmentActivities = triplestoreService.getStudyTreatmentActivities(nameSpaceUid, studyUid);
    assertEquals(6, studyTreatmentActivities.size());
    TreatmentActivity treatmentActivity = studyTreatmentActivities.get(1);
    assertEquals(1, treatmentActivity.getAdministeredDrugs().size());
  }

  @Test
  public void testGetTreatmentActivitiesFlexDose() {
    String nameSpaceUid = "f833282f-9090-4b50-87ea-5f6a12dc6e19"; // edarbi
    String studyUid = "89b86b85-ea02-4a43-bc18-17dcca9f9c9a"; //TAK491-019 / NCT00696436
    List<TreatmentActivity> studyTreatmentActivities = triplestoreService.getStudyTreatmentActivities(nameSpaceUid, studyUid);
    assertEquals(6, studyTreatmentActivities.size());
    TreatmentActivity treatmentActivity = studyTreatmentActivities.get(1);
    assertEquals(1, treatmentActivity.getAdministeredDrugs().size());
  }

  @Test
  public void testGetTreatmentActivitiesMultidrugArms() {
    String nameSpaceUid = "f833282f-9090-4b50-87ea-5f6a12dc6e19"; // edarbi
    String studyUid = "f0b7c1e7-b3e9-4a45-9acc-0e612d43c4e2"; //TAK491-301 / NCT00846365
    List<TreatmentActivity> studyTreatmentActivities = triplestoreService.getStudyTreatmentActivities(nameSpaceUid, studyUid);
    assertEquals(4, studyTreatmentActivities.size());
    TreatmentActivity treatmentActivity = studyTreatmentActivities.get(1);
    assertEquals(2, treatmentActivity.getAdministeredDrugs().size());
  }

  @Test
  public void testGetPopulationCharacteristics() {
    String nameSpaceUid = "f833282f-9090-4b50-87ea-5f6a12dc6e19"; // edarbi
    String studyUid = "30b36971-1074-4482-9252-74f6541b566b"; //TAK491-008 / NCT00696241
    List<StudyData> studyData = triplestoreService.getStudyData(nameSpaceUid, studyUid, StudyDataSection.BASE_LINE_CHARACTERISTICS);
    assertEquals(2, studyData.size());
  }

  @Test
  public void testGetOutcomes() {
    String nameSpaceUid = "f833282f-9090-4b50-87ea-5f6a12dc6e19"; // edarbi
    String version = "http://localhost:8080/versions/9a02c9e3-44b4-415b-b042-f449a1b5dcc0";
    List<SemanticOutcome> outcomes = triplestoreService.getOutcomes(nameSpaceUid, version);
    assertEquals(95, outcomes.size());
  }

  @Test
  public void testGetInterventions() {
    String nameSpaceUid = "f833282f-9090-4b50-87ea-5f6a12dc6e19"; // edarbi
    String version = "http://localhost:8080/versions/9a02c9e3-44b4-415b-b042-f449a1b5dcc0";
    List<SemanticIntervention> interventions = triplestoreService.getInterventions(nameSpaceUid, version);
    assertEquals(7, interventions.size());
  }

  @Test
  public void testGetStudies() {
    String nameSpaceUid = "f833282f-9090-4b50-87ea-5f6a12dc6e19"; // edarbi
    String version = "http://localhost:8080/versions/75f5834f-314e-44d7-beed-979dce4a1e4f";
    List<Study> studies = triplestoreService.queryStudies(nameSpaceUid, version);
    assertEquals(6, studies.size());
    assertEquals(59, studies.get(1).getOutcomeUids().size());
    assertTrue(studies.get(1).getOutcomeUids().get(0).startsWith("http://trials.drugis.org/entities/"));
  }

  @Test
  @Ignore // TODO needs fixing
  public void testGetSingleStudyMeasurements() {
    String studyUid = "30b36971-1074-4482-9252-74f6541b566b"; //TAK491-008 / NCT00696241
    String version = "http://localhost:8080/versions/9a02c9e3-44b4-415b-b042-f449a1b5dcc0";

    String nonSAE = "832c2831-dab2-4707-8bf4-af0cf057ba5a";
    String SBPMeanTroughSitting = "151afdaf-ed92-4204-a389-a3e125e0f19d";
    List<String> outcomeUids = Arrays.asList(SBPMeanTroughSitting, nonSAE);
    String azilsartan = "f1b57b39-1887-4547-aaf3-7699ed37463b";
    String placebo = "dce4e0b8-57a4-438b-9001-742fcc1b9c8b";
    List<String> interventionUids = Arrays.asList(azilsartan, placebo);
    List<TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow> singleStudyMeasurements = triplestoreService.getSingleStudyMeasurements(nameSpaceUid, studyUid, version, outcomeUids, interventionUids);
    assertEquals(8, singleStudyMeasurements.size());
    assertFalse(singleStudyMeasurements.get(0).getAlternativeUid().startsWith("ontology"));
  }

  @Test
  @Ignore // TODO needs fixing
  public void testGetSingleStudyMeasurementsteststudy() {
    String studyUid = "9b7f8a8d-f96b-4f3a-99d4-8b1d4433d649"; //test study
    String version = "http://localhost:8080/versions/de01d954-95ff-4523-8d02-bb08780deeca";

    String nonSAE = "832c2831-dab2-4707-8bf4-af0cf057ba5a";
    String spugen = "05303d8c-2609-4bda-adfc-8c5883454efd";
    List<String> outcomeUids = Arrays.asList(spugen, nonSAE);
    String azilsartan = "f1b57b39-1887-4547-aaf3-7699ed37463b";
    String ramipril = "330a5743-d827-4696-a2c2-43970517f58b";
    List<String> interventionUids = Arrays.asList(azilsartan, ramipril);
    List<TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow> singleStudyMeasurements = triplestoreService.getSingleStudyMeasurements(nameSpaceUid, studyUid, version, outcomeUids, interventionUids);
    assertEquals(8, singleStudyMeasurements.size());
    assertFalse(singleStudyMeasurements.get(0).getAlternativeUid().startsWith("ontology"));
  }

  @Test
  public void testGetTrialData() {
    String version = "http://localhost:8080/versions/9a02c9e3-44b4-415b-b042-f449a1b5dcc0";
    String nonSAE = "832c2831-dab2-4707-8bf4-af0cf057ba5a";
    String azilsartan = "f1b57b39-1887-4547-aaf3-7699ed37463b";
    String placebo = "dce4e0b8-57a4-438b-9001-742fcc1b9c8b";
    List<String> interventionUids = Arrays.asList(azilsartan, placebo);
    List<TrialDataStudy> trialData = triplestoreService.getTrialData(nameSpaceUid, version, nonSAE, interventionUids);
    assertEquals(4, trialData.size());
  }

  @Test
  public void testNamespaceGet() {
    Namespace namespace = triplestoreService.getNamespaceHead(nameSpaceUid);
    assertNotNull(namespace);
    assertEquals("Edarbi integration", namespace.getName());
    assertEquals("created for integration tests of trialverse",namespace.getDescription());
    assertEquals(new Integer(8), namespace.getNumberOfStudies());
    assertEquals("http://localhost:8080/versions/de01d954-95ff-4523-8d02-bb08780deeca", namespace.getVersion());
  }

  @Test
  public void testQueryNamespaces() {
    Collection<Namespace> namespaces = triplestoreService.queryNameSpaces();
    assertEquals(4, namespaces.size());
  }
  
  @Test
  public void getStudyDetailsTest() throws ResourceDoesNotExistException {
    String studyUid = "30b36971-1074-4482-9252-74f6541b566b"; //TAK491-008 / NCT00696241
    StudyWithDetails studydetails = triplestoreService.getStudydetails(nameSpaceUid, studyUid);
    assertEquals("TAK491-008 / NCT00696241", studydetails.getName());
    assertEquals(new Integer(1275), studydetails.getStudySize());
    assertEquals("Placebo, Olmesartan, Azilsartan", studydetails.getInvestigationalDrugNames());
    assertEquals(new Integer(5), studydetails.getNumberOfArms());
  }

  @Test
  public void testGetEpochs() {
    String studyUid = "30b36971-1074-4482-9252-74f6541b566b"; //TAK491-008 / NCT00696241
    JSONArray studyEpochs = triplestoreService.getStudyEpochs(nameSpaceUid, studyUid);
    assertEquals(2, studyEpochs.size());
  }

  @Test
  public void testGetArms() {
    String studyUid = "30b36971-1074-4482-9252-74f6541b566b"; //TAK491-008 / NCT00696241
    JSONArray studyArms = triplestoreService.getStudyArms(nameSpaceUid, studyUid);
    assertEquals(5, studyArms.size());
  }

  @Test
  public void testGetStudiesWithDetails() {
    List<StudyWithDetails> studyWithDetailses = triplestoreService.queryStudydetailsHead(nameSpaceUid);
    assertEquals(6, studyWithDetailses.size());
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
