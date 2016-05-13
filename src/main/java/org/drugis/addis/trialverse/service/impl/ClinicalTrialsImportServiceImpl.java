package org.drugis.addis.trialverse.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.drugis.addis.patavitask.repository.impl.PataviTaskRepositoryImpl;
import org.drugis.addis.trialverse.service.ClinicalTrialsImportService;
import org.drugis.addis.util.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by connor on 12-5-16.
 */
@Service
public class ClinicalTrialsImportServiceImpl implements ClinicalTrialsImportService {

  private final static Logger logger = LoggerFactory.getLogger(PataviTaskRepositoryImpl.class);
  private ObjectMapper objectMapper = new ObjectMapper();
  private final static String importerServiceLocation = WebConstants.loadSystemEnv("CLINICALTRIALS_IMPORTER_URL");

  @Inject
  HttpClient httpClient;

  @Override
  public JsonNode fetchInfo(String nctId) throws ClinicalTrialsImportError {
    HttpGet httpGet = new HttpGet(importerServiceLocation + "/" + nctId);
    HttpEntity entity = null;
    try {
      HttpResponse response = httpClient.execute(httpGet);
      int responceStatusCode = response.getStatusLine().getStatusCode();
      entity = response.getEntity();
      if(responceStatusCode == HttpStatus.SC_NOT_FOUND){
        logger.trace("import study not found for nctId: " + nctId);
        EntityUtils.consume(entity);
          return null;
      } else {
        if (responceStatusCode == HttpStatus.SC_OK) {
          entity = response.getEntity();
          String responseAsString = EntityUtils.toString(entity);
          EntityUtils.consume(entity);
          return objectMapper.readTree(responseAsString);
        } else {
          EntityUtils.consume(entity);
          throw new ClinicalTrialsImportError("could fetch study information, resured status code = " + responceStatusCode);
        }
      }
    } catch (Exception e) {
      if(entity != null) {
        try {
          EntityUtils.consume(entity);
        } catch (IOException e1) {
          logger.error("could not close connection");
        }
      }
      throw new ClinicalTrialsImportError("could fetch study information, " + e.toString());
    }
  }
}
