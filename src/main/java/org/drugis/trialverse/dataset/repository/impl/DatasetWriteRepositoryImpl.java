package org.drugis.trialverse.dataset.repository.impl;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetWriteRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.exception.CreateDatasetException;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by connor on 04/11/14.
 */

@Repository
public class DatasetWriteRepositoryImpl implements DatasetWriteRepository {

    public static final String HOST = "Host";
    public static final String PATH = "/datasets";
    public static final String X_EVENT_SOURCE_CREATOR = "X-EventSource-Creator";
    public static final String INITIAL_COMMIT_MESSAGE = "Dataset created through Trialverse";
    public static final String X_EVENT_SOURCE_TITLE = "X-EventSource-Title";

    @Inject
    private WebConstants webConstants;

    @Inject
    private RestTemplate restTemplate;

    @Inject
    private JenaFactory jenaFactory;

    @Inject
    private VersionMappingRepository versionMappingRepository;


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

    @Override
    public URI createDataset(String title, String description, Account owner) throws URISyntaxException, CreateDatasetException {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(X_EVENT_SOURCE_CREATOR, "mailto:" + owner.getUsername());
        httpHeaders.add(X_EVENT_SOURCE_TITLE, Base64.encodeBase64String(INITIAL_COMMIT_MESSAGE.getBytes()));
        httpHeaders.add(HTTP.CONTENT_TYPE, RDFLanguages.TURTLE.getContentType().getContentType());

        String datasetUri =  jenaFactory.createDatasetURI();

        Model baseDatasetModel = buildDatasetBaseModel(title, description, datasetUri);
        String triples = modelToString(baseDatasetModel);
        HttpEntity<String> requestEntity = new HttpEntity<>(triples, httpHeaders);
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.postForEntity(webConstants.getTriplestoreBaseUri() + PATH, requestEntity, String.class);
        }catch (RestClientException e){
            logger.error(e.toString());
            throw new CreateDatasetException();
        }

        if(!HttpStatus.CREATED.equals(response.getStatusCode())) {
            logger.error("error , could not create dataset, tripleStore responce = " + response.getStatusCode().getReasonPhrase());
            throw new CreateDatasetException();
        }

        URI location = response.getHeaders().getLocation();
        //store link from uri to location
        versionMappingRepository.save(new VersionMapping(location.toString(), owner.getUsername(), datasetUri));

        return new URI(datasetUri);
    }

    @Override
    public HttpResponse updateDataset(String datasetUUID, InputStream datasetContent) {
        HttpPost request = new HttpPost(createDatasetGraphUri(datasetUUID));
//        HttpClient client = httpClientFactory.build();
        HttpResponse response = null;
//        try {
//            InputStreamEntity entity = new InputStreamEntity(datasetContent);
//            entity.setContentType(RDFLanguages.TURTLE.getContentType().getContentType());
//            request.setEntity(entity);
//            response = client.execute(request);
//            datasetContent.close();
//        } catch (IOException e) {
//            logger.error(e.toString());
//        } finally {
//            IOUtils.closeQuietly(datasetContent);
//        }
        return response;
    }

    private String modelToString(Model model) {
        StringWriter outputWriter = new StringWriter();
        model.write(outputWriter, "Turtle");
        return outputWriter.toString();
    }

    private Model buildDatasetBaseModel(String title, String description, String datasetUri) {

        Model model = ModelFactory.createDefaultModel();

        model.createResource(datasetUri)
                .addProperty(DCTerms.title, title)
                .addProperty(DCTerms.description, description);

        return model;
    }


}
