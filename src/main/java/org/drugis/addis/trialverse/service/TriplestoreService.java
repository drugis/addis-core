package org.drugis.addis.trialverse.service;

import net.minidev.json.JSONArray;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.model.emun.StudyDataSection;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;

import java.util.Collection;
import java.util.List;

/**
 * Created by connor on 2/28/14.
 */
public interface TriplestoreService {
  public final static String TRIPLESTORE_BASE_URI = System.getenv("TRIPLESTORE_BASE_URI");

  public Collection<Namespace> queryNameSpaces();

  public Namespace getNamespace(String uid);

  public List<SemanticOutcome> getOutcomes(String namespaceUid, String version);

  public List<SemanticIntervention> getInterventions(String namespaceUid, String version);

  public List<Study> queryStudies(String namespaceUid, String version);

  public List<StudyWithDetails> queryStudydetails(String namespaceUid);
  
  public StudyWithDetails getStudydetails(String namespaceUid, String studyUid) throws ResourceDoesNotExistException;

  public JSONArray getStudyArms(String namespaceUid, String studyUid);

  public JSONArray getStudyEpochs(String namespaceUid, String studyUid);

  public List<TrialDataStudy> getTrialData(String namespaceUid, String version, String outcomeUri, List<String> interventionUris);

  public List<TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow> getSingleStudyMeasurements(String studyUid, String version, List<String> outcomeUids, List<String> interventionUids);

  public List<TreatmentActivity> getStudyTreatmentActivities(String namespaceUid, String studyUid);

  public List<StudyData> getStudyData(String namespaceUid, String studyUid, StudyDataSection studyDataSection);
}
