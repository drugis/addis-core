package org.drugis.trialverse.dataset.repository.impl;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.dataset.repository.DatasetWriteRepository;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

/**
 * Created by connor on 04/11/14.
 */

@Repository
public class DatasetWriteRepositoryImpl implements DatasetWriteRepository {

  @Inject
  private WebConstants webConstants;

  @Inject
  private HttpClientFactory httpClientFactory;

  @Inject
  private JenaFactory jenaFactory;

  private final static Logger logger = LoggerFactory.getLogger(DatasetWriteRepositoryImpl.class);

  private String createDatasetGraphUri(String datasetUUID) {
    URIBuilder builder = null;
    try {
      builder = new URIBuilder(webConstants.getTriplestoreDataUri() + "/data");
      builder.addParameter("graph", Namespaces.DATASET_NAMESPACE + datasetUUID);
      return builder.build().toString();
    } catch (URISyntaxException e) {
      logger.error(e.toString());
    }
    return "";
  }

  private Model createDatasetModel(String datasetIdentifier, Account owner, String title, String description) {
    Model model = jenaFactory.createModel();
    model.setNsPrefix("ontology", "http://trials.drugis.org/ontology#");
    Resource datasetURI = model.createResource(datasetIdentifier);
    Resource datasetOntologyURI = model.createResource("http://trials.drugis.org/ontology#Dataset");
    if (description != null) {
      model.add(datasetURI, RDFS.comment, description);
    }

    return model
            .add(datasetURI, DC.creator, owner.getUsername())
            .add(datasetURI, RDFS.label, title)
            .add(datasetURI, RDF.type, datasetOntologyURI);
  }

  @Override
  public String createDataset(String title, String description, Account owner) {
    DatasetAccessor dataSetAccessor = jenaFactory.getDatasetAccessor();
    String datasetIdentifier = jenaFactory.createDatasetURI();
    Model model = createDatasetModel(datasetIdentifier, owner, title, description);

    try {
      dataSetAccessor.putModel(datasetIdentifier, model);
    } catch (HttpException e) {
      logger.error("Unable to create dataset, responceCode from jena: " + e.getResponseCode());
      throw e;
    }
    return datasetIdentifier;
  }

  @Override
  public HttpResponse updateDataset(String datasetUUID, InputStream datasetContent) {
    HttpPost request = new HttpPost(createDatasetGraphUri(datasetUUID));
    HttpClient client = httpClientFactory.build();
    HttpResponse response = null;
    try {
      InputStreamEntity entity = new InputStreamEntity(datasetContent);
      entity.setContentType(RDFLanguages.TURTLE.getContentType().getContentType());
      request.setEntity(entity);
      response = client.execute(request);
      datasetContent.close();
    } catch (IOException e) {
      logger.error(e.toString());
    } finally {
      IOUtils.closeQuietly(datasetContent);
    }
    return response;
  }


}
