package org.drugis.addis.trialverse.service;

import net.minidev.json.JSONArray;
import net.minidev.json.parser.ParseException;
import org.apache.commons.io.IOUtils;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.model.emun.CovariateOption;
import org.drugis.addis.trialverse.model.emun.StudyDataSection;
import org.drugis.addis.trialverse.model.mapping.VersionedUuidAndOwner;
import org.drugis.addis.trialverse.model.trialdata.CovariateStudyValue;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by connor on 2/28/14.
 */
public interface TriplestoreService {
  String TRIPLESTORE_BASE_URI = System.getenv("TRIPLESTORE_BASE_URI");

  static String loadResource(String filename) {
    try {
      Resource myData = new ClassPathResource(filename);
      InputStream stream = myData.getInputStream();
      return IOUtils.toString(stream, "UTF-8");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "";
  }

  Collection<Namespace> queryNameSpaces() throws ParseException;

  Namespace getNamespaceHead(VersionedUuidAndOwner uuidAndOwner);

  Namespace getNamespaceVersioned(VersionedUuidAndOwner datasetUri, String versionUri);

  List<SemanticVariable> getOutcomes(String namespaceUid, String version) throws ReadValueException;

  List<SemanticVariable> getPopulationCharacteristics(String versionedUuid, String version) throws ReadValueException;

  List<SemanticInterventionUriAndName> getInterventions(String namespaceUid, String version);

  List<Study> queryStudies(String namespaceUid, String version);

  StudyWithDetails getStudydetails(String namespaceUid, String studyUid) throws ResourceDoesNotExistException;

  JSONArray getStudyGroups(String namespaceUid, String studyUid);

  JSONArray getStudyEpochs(String namespaceUid, String studyUid);

  List<TrialDataStudy> getSingleStudyData(String namespaceUid, URI studyUri, String version, List<URI> outcomeUris, List<URI> interventionUids) throws ReadValueException;

  List<TreatmentActivity> getStudyTreatmentActivities(String namespaceUid, String studyUid);

  List<StudyData> getStudyData(String namespaceUid, String studyUid, StudyDataSection studyDataSection);

  Optional<AbstractIntervention> findMatchingIncludedIntervention(List<AbstractIntervention> includedInterventions, TrialDataArm arm);

  List<CovariateStudyValue> getStudyLevelCovariateValues(String namespaceUid, String version, List<CovariateOption> covariates) throws ReadValueException;

  List<TrialDataStudy> getNetworkData(String namespaceUid, String version, URI outcomeUri, List<URI> interventionUris, List<String> covariateKeys) throws ReadValueException;

  List<TrialDataStudy> getAllTrialData(String namespaceUid, String datasetVersion, List<URI> outcomeUris, List<URI> interventionUris) throws ReadValueException;

  void addMatchingInformation(List<AbstractIntervention> includedInterventions, List<TrialDataStudy> trialData);
}
