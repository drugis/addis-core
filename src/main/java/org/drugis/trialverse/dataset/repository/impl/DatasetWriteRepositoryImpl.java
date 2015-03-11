package org.drugis.trialverse.dataset.repository.impl;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.DCTerms;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.protocol.HTTP;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetWriteRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
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
    public static final String TEXT_TURTLE_CONTENT_TYPE = "text/turtle";
    public static final String DATASET_PREFIX = "http://trials.drugis.org/datasets/";
    public static final String VERSION_HEADER = "X-EventSource-Version";

    @Inject
    private WebConstants webConstants;

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
    public URI createDataset(String title, String description, Account owner) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HOST, "example.com");
        httpHeaders.add(X_EVENT_SOURCE_CREATOR, "http://example.com/GreenGoblin");
        httpHeaders.add(X_EVENT_SOURCE_TITLE, Base64.encodeBase64String(INITIAL_COMMIT_MESSAGE.getBytes()));
        httpHeaders.add(HTTP.CONTENT_TYPE, TEXT_TURTLE_CONTENT_TYPE);

        Model baseDatasetModel = buildDatasetBaseModel(title, description, owner);
        String triples = modelToString(baseDatasetModel);

        HttpEntity<String> requestEntity = new HttpEntity<>(triples, httpHeaders);

        ResponseEntity<String> response =  restTemplate.postForEntity(webConstants.getTriplestoreBaseUri() + PATH, requestEntity, String.class);
        HttpHeaders responceHeaders = response.getHeaders();
        URI location = responceHeaders.getLocation();
        String version = responceHeaders.get(VERSION_HEADER).get(0);

        // store version
        VersionMapping mapping = new VersionMapping(DATASET_PREFIX + owner.getUsername(), owner.getUsername(), version);
        versionMappingRepository.createMapping(mapping);

        return location;
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

    private Model buildDatasetBaseModel(String title, String description, Account owner) {

        Model model = ModelFactory.createDefaultModel();

        model.createResource(DATASET_PREFIX + owner.getUsername())
                .addProperty(DCTerms.title, title)
                .addProperty(DCTerms.description, description);

        return model;
    }


}
