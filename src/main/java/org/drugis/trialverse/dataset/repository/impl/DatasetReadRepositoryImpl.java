package org.drugis.trialverse.dataset.repository.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.util.JenaGraphMessageConverter;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;

/**
 * Created by daan on 7-11-14.
 */
@Repository
public class DatasetReadRepositoryImpl implements DatasetReadRepository {

    private final static Logger logger = LoggerFactory.getLogger(DatasetReadRepositoryImpl.class);

    private final static String STUDIES_WITH_DETAILS = loadResource("constructStudiesWithDetails.sparql");
    private final static String IS_OWNER_QUERY = loadResource("askIsOwner.sparql");
    private final static String CONTAINS_STUDY_WITH_SHORTNAME = loadResource("askContainsStudyWithLabel.sparql");

    public final static String QUERY_AFFIX = "/current/query";
    public static final String DATA_END_POINT = "/data";
    public static final String QUERY_STRING_DEFAULT_GRAPH = "?default";
    private static final Node CLASS_VOID_DATASET = NodeFactory.createURI("http://rdfs.org/ns/void#Dataset");


    @Inject
    private HttpClientFactory httpClientFactory;

    @Inject
    private WebConstants webConstants;

    @Inject
    private JenaFactory jenaFactory;

    @Inject
    private VersionMappingRepository versionMappingRepository;

    private enum FUSEKI_OUTPUT_TYPES {
        TEXT, JSON;

        @Override
        public String toString() {
            switch (this) {
                case TEXT:
                    return "text";
                case JSON:
                    return "json";
                default:
                    throw new EnumConstantNotPresentException(FUSEKI_OUTPUT_TYPES.class, "nonexistent enum constant");
            }
        }
    }

    private static String loadResource(String filename) {
        try {
            Resource myData = new ClassPathResource(filename);
            InputStream stream = myData.getInputStream();
            return IOUtils.toString(stream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new LoadResourceException("could not load resource " + filename);
    }

    private HttpResponse doConstructQuery(String query) {
        return doRequest(query, FUSEKI_OUTPUT_TYPES.TEXT.toString(), RDFLanguages.TURTLE.getContentType().getContentType());
    }

    private HttpResponse doSelectQuery(String query) {
        return doRequest(query, FUSEKI_OUTPUT_TYPES.JSON.toString(), RDFLanguages.JSONLD.getContentType().getContentType());
    }

    private HttpResponse doAskQuery(String query) {
        return doRequest(query, FUSEKI_OUTPUT_TYPES.JSON.toString(), RDFLanguages.JSONLD.getContentType().getContentType());
    }


    private HttpResponse doRequest(String query, String outputType, String acceptType) {
        try {
            HttpClient client = httpClientFactory.build();
            URIBuilder builder = new URIBuilder(webConstants.getTriplestoreBaseUri() + QUERY_AFFIX);
            builder.setParameter("query", query);
            builder.setParameter("output", outputType);
            HttpGet request = new HttpGet(builder.build());
            request.setHeader("Accept", acceptType);
            return client.execute(request);
        } catch (URISyntaxException | IOException e) {
            logger.error(e.toString());
        }
        throw new QueryException("Could not execute query " + query);
    }

    @Override
    public Model queryDatasets(Account currentUserAccount) {
        List<VersionMapping> mappings = versionMappingRepository.findMappingsByUsername(currentUserAccount.getUsername());
        Graph graph = GraphFactory.createGraphMem();
        for(VersionMapping mapping : mappings) {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new JenaGraphMessageConverter());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(org.apache.http.HttpHeaders.ACCEPT, RDFLanguages.TURTLE.getContentType().getContentType());
            HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
            String uri = mapping.getDatasetLocation() + DATA_END_POINT + QUERY_STRING_DEFAULT_GRAPH;

            ResponseEntity<Graph> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, Graph.class);

            GraphUtil.addInto(graph,  responseEntity.getBody());
            graph.add(new Triple(NodeFactory.createURI(mapping.getTrialverseDataset()), RDF.Nodes.type, CLASS_VOID_DATASET));
        }

        return ModelFactory.createModelForGraph(graph);
    }

    @Override
    public Model getDataset(String datasetUUID) {
        DatasetAccessor datasetAccessor = jenaFactory.getDatasetAccessor();
        return datasetAccessor.getModel(Namespaces.DATASET_NAMESPACE + datasetUUID);
    }

    @Override
    public HttpResponse queryDatasetsWithDetail(String datasetUUID) {
        String query = StringUtils.replace(STUDIES_WITH_DETAILS, "$datasetUUID", datasetUUID);
        return doSelectQuery(query);
    }

    @Override
    public boolean isOwner(String datasetUUID, Principal principal) {
        boolean isOwner = false;
        String query = StringUtils.replace(IS_OWNER_QUERY, "$owner", "'" + principal.getName() + "'");
        query = StringUtils.replace(query, "$dataset", datasetUUID);
        HttpResponse response = doAskQuery(query);

        JSONParser jsonParser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response.getEntity().getContent());
            isOwner = (Boolean) jsonObject.get("boolean");
        } catch (ParseException | IOException e) {
            logger.error("could not parse result from check owner query");
            logger.error(e.toString());
        }
        return isOwner;
    }

    @Override
    public boolean containsStudyWithShortname(String datasetUUID, String shortName) {
        Boolean containsStudyWithShortname = false;
        String query = StringUtils.replace(CONTAINS_STUDY_WITH_SHORTNAME, "$dataset", datasetUUID);
        query = StringUtils.replace(query, "$shortName", "'" + shortName + "'");
        HttpResponse response = doAskQuery(query);
        try {
            containsStudyWithShortname = JsonPath.read(response.getEntity().getContent(), "$.boolean");
        } catch (IOException e) {
            logger.error(e.toString());
        }

        return containsStudyWithShortname;
    }

    private static class LoadResourceException extends RuntimeException {
        public LoadResourceException(String s) {
            super(s);
        }
    }
}
