package org.drugis.addis.trialverse.service;

import net.minidev.json.JSONArray;
import net.minidev.json.parser.ParseException;
import org.apache.commons.io.IOUtils;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.model.emun.CovariateOption;
import org.drugis.addis.trialverse.model.emun.StudyDataSection;
import org.drugis.addis.trialverse.model.mapping.TriplestoreUuidAndOwner;
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
import java.util.Set;

/**
 * Created by connor on 2/28/14.
 */
public interface TriplestoreService {
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

  Namespace getNamespaceHead(TriplestoreUuidAndOwner uuidAndOwner);

  Namespace getNamespaceVersioned(TriplestoreUuidAndOwner datasetUri, URI versionUri) throws IOException;

  String getHeadVersion(URI datasetUri);

  List<SemanticVariable> getOutcomes(String namespaceUid, URI version) throws ReadValueException, IOException;

  List<SemanticVariable> getPopulationCharacteristics(String versionedUuid, URI version) throws ReadValueException, IOException;

  List<SemanticInterventionUriAndName> getInterventions(String namespaceUid, URI version) throws IOException;

  List<Study> queryStudies(String namespaceUid, URI version) throws IOException;

  List<TrialDataStudy> getSingleStudyData(String namespaceUid, URI studyUri, URI version, Set<URI> outcomeUris, Set<URI> interventionUids) throws ReadValueException, IOException;

  Set<AbstractIntervention> findMatchingIncludedInterventions(Set<AbstractIntervention> includedInterventions, TrialDataArm arm);

  List<CovariateStudyValue> getStudyLevelCovariateValues(String namespaceUid, URI version, List<CovariateOption> covariates) throws ReadValueException, IOException;

  List<TrialDataStudy> getNetworkData(String namespaceUid, URI version, URI outcomeUri, Set<URI> interventionUris, Set<String> covariateKeys) throws ReadValueException, IOException;

  List<TrialDataStudy> getAllTrialData(String namespaceUid, URI datasetVersion, Set<URI> outcomeUris, Set<URI> interventionUris) throws ReadValueException, IOException;

  List<TrialDataStudy> addMatchingInformation(Set<AbstractIntervention> includedInterventions, List<TrialDataStudy> trialData);

  List<URI> getUnitUris(String trialverseDatasetUuid, URI headVersion) throws IOException;
}
