package org.drugis.addis.trialverse.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.drugis.addis.patavitask.repository.impl.PataviTaskRepositoryImpl;
import org.drugis.addis.trialverse.service.ClinicalTrialsImportService;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.graph.exception.UpdateGraphException;
import org.drugis.trialverse.graph.repository.GraphWriteRepository;
import org.drugis.trialverse.util.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by connor on 12-5-16.
 */
@Service
public class ClinicalTrialsImportServiceImpl implements ClinicalTrialsImportService {

  private final static Logger logger = LoggerFactory.getLogger(PataviTaskRepositoryImpl.class);
  private ObjectMapper objectMapper = new ObjectMapper();
  private final static String importerServiceLocation = WebConstants.loadSystemEnv("CLINICALTRIALS_IMPORTER_URL");

  @Inject
  private HttpClient httpClient;

  @Inject
  private GraphWriteRepository graphWriteRepository;

  @Override
  public JsonNode fetchInfo(String nctId) throws ClinicalTrialsImportError {
    HttpGet httpGet = new HttpGet(importerServiceLocation + "/" + nctId);
    try (CloseableHttpResponse response =  (CloseableHttpResponse) httpClient.execute(httpGet)){
      int responceStatusCode = response.getStatusLine().getStatusCode();
      HttpEntity entity = response.getEntity();
      if(responceStatusCode == HttpStatus.SC_NOT_FOUND){
        logger.trace("import study not found for nctId: " + nctId);
        EntityUtils.consume(entity);
          return null;
      } else if (responceStatusCode == HttpStatus.SC_OK) {
        entity = response.getEntity();
        String responseAsString = EntityUtils.toString(entity);
        EntityUtils.consume(entity);
        return objectMapper.readTree(responseAsString);
      } else {
        EntityUtils.consume(entity);
        throw new ClinicalTrialsImportError("could fetch study information, resured status code = " + responceStatusCode);
      }
    } catch (Exception e) {
      throw new ClinicalTrialsImportError("could fetch study information, " + e.toString());
    }
  }

  @Override
  public Header importStudy(String commitTitle,
                            String commitDescription,
                            String datasetUuid,
                            String graphUuid, URI importUrl) throws ClinicalTrialsImportError {
    HttpGet httpGet = new HttpGet(importUrl);
    try (CloseableHttpResponse response =  (CloseableHttpResponse) httpClient.execute(httpGet)){
      InputStream content = response.getEntity().getContent();
      return graphWriteRepository.updateGraph(new URI(Namespaces.DATASET_NAMESPACE + datasetUuid), graphUuid, content, commitTitle, commitDescription);
    } catch (IOException e) {
      throw new ClinicalTrialsImportError("could get study data, " + e.toString());
    } catch (UpdateGraphException e) {
      throw new ClinicalTrialsImportError("could not update graph, " + e.toString());
    } catch (URISyntaxException e) {
      throw new ClinicalTrialsImportError("could not create URI, " + e.toString());
    }
  }
}
