package org.drugis.trialverse.dataset.repository.impl;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.jena.atlas.web.HttpException;
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

  private HttpResponse doRequest(String datasetContent, HttpEntityEnclosingRequestBase request) {
    HttpClient client = httpClientFactory.build();
    HttpResponse response = null;
    try {
      StringEntity entity = new StringEntity(datasetContent, "UTF-8");
      entity.setContentType("application/ld+json");
      request.setEntity(entity);
      request.setHeader("Accept", "application/ld+json");
      response = client.execute(request);
    } catch (IOException e) {
      logger.error(e.toString());
    }
    return response;
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
      // todo thow new blocking exception to signal the front-end something has gone very wrong
    }
    return datasetIdentifier;
  }

  @Override
  public HttpResponse updateDataset(String datasetUUID, String datasetContent) {
    HttpPost request = new HttpPost(createDatasetGraphUri(datasetUUID));
    HttpResponse response = doRequest(datasetContent, request);
    return response;
  }


}
