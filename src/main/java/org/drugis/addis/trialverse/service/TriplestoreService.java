package org.drugis.addis.trialverse.service;

import net.minidev.json.JSONArray;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;

import java.util.Collection;
import java.util.List;

/**
 * Created by connor on 2/28/14.
 */
public interface TriplestoreService {
  public Collection<Namespace> queryNameSpaces();

  public Namespace getNamespace(String uid);

  public List<SemanticOutcome> getOutcomes(String namespaceUid);

  public List<SemanticIntervention> getInterventions(String namespaceUid);

  public List<Study> queryStudies(String namespaceUid);

  public List<StudyWithDetails> queryStudydetails(String namespaceUid);

  public StudyWithDetails getStudydetails(String namespaceUid, String studyUid) throws ResourceDoesNotExistException;

  public JSONArray getStudyArms(String namespaceUid, String studyUid);

  public List<TreatmentActivity> getStudyDesign(String namespaceUid, String studyUid) throws ResourceDoesNotExistException;

  public List<TrialDataStudy> getTrialData(String namespaceUid, String outcomeUri, List<String> interventionUris);

  public List<TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow> getSingleStudyMeasurements(String studyUid, List<String> outcomeUids, List<String> interventionUids);
}
