package org.drugis.addis.importer.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.util.EntityUtils;
import org.drugis.addis.patavitask.repository.impl.PataviTaskRepositoryImpl;
import org.drugis.addis.importer.ClinicalTrialsImportService;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.graph.exception.UpdateGraphException;
import org.drugis.trialverse.graph.repository.GraphWriteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

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
    try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpGet)) {
      int responseStatusCode = response.getStatusLine().getStatusCode();
      HttpEntity entity = response.getEntity();
      if (responseStatusCode == HttpStatus.SC_NOT_FOUND) {
        logger.trace("import study not found for nctId: " + nctId);
        EntityUtils.consume(entity);
        return null;
      } else if (responseStatusCode == HttpStatus.SC_OK) {
        entity = response.getEntity();
        String responseAsString = EntityUtils.toString(entity);
        EntityUtils.consume(entity);
        return objectMapper.readTree(responseAsString);
      } else {
        EntityUtils.consume(entity);
        throw new ClinicalTrialsImportError("could not fetch study information, status code = " + responseStatusCode);
      }
    } catch (Exception e) {
      throw new ClinicalTrialsImportError("could not fetch study information, " + e.toString());
    }
  }

  @Override
  public Header importStudy(
          String commitTitle,
          String commitDescription,
          String datasetUri,
          String graphUuid,
          String importStudyRef
  ) throws ClinicalTrialsImportError {
    HttpGet httpGet = new HttpGet(importerServiceLocation + "/" + importStudyRef + "/rdf");
    try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpGet)) {
      int responseStatusCode = response.getStatusLine().getStatusCode();
      if (responseStatusCode == HttpStatus.SC_OK) {
        InputStream content = response.getEntity().getContent();
        return graphWriteRepository.updateGraph(URI.create(datasetUri), graphUuid, content, commitTitle, commitDescription);
      } else {
        throw new ClinicalTrialsImportError("could not import study, errorCode: " + responseStatusCode + " reason:" + response.getStatusLine().getReasonPhrase());
      }
    } catch (IOException e) {
      throw new ClinicalTrialsImportError("could get study data, " + e.toString());
    } catch (UpdateGraphException e) {
      throw new ClinicalTrialsImportError("could not update graph, " + e.toString());
    }
  }

  @Override
  public Header importEudract(String versionedDatasetUrl, String graphUuid, ServletInputStream postedXml) {
    HttpPost httpPost = new HttpPost(importerServiceLocation + "/eudract");
    httpPost.setEntity(new InputStreamEntity(postedXml));
    httpPost.setHeader("Content-type", "application/xml");
    try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpPost)) {
      int responseStatusCode = response.getStatusLine().getStatusCode();
      if (responseStatusCode == HttpStatus.SC_OK) {
        InputStream content = response.getEntity().getContent();
        return graphWriteRepository.updateGraph(URI.create(versionedDatasetUrl), graphUuid,
                content, "Imported study from EudraCT XML", null);
      } else {
        throw new ClinicalTrialsImportError("could not import study, errorCode: " + responseStatusCode +
                " reason:" + response.getStatusLine().getReasonPhrase());
      }
    } catch (IOException | UpdateGraphException e) {
      e.printStackTrace();
      throw new ClinicalTrialsImportError("import eudract failed");
    }
  }
}
