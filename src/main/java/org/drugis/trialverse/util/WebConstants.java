package org.drugis.trialverse.util;

import org.apache.jena.riot.WebContent;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.Charset;

/**
 * Created by connor on 2/12/14.
 */
@Component
public class WebConstants {
  private final static MediaType APPLICATION_JSON_UTF8 = new MediaType(
          MediaType.APPLICATION_JSON.getType(),
          MediaType.APPLICATION_JSON.getSubtype(),
          Charset.forName("utf8"));

  public static final String APPLICATION_JSON_UTF8_VALUE = "application/json; charset=UTF-8";
  public static final String APPLICATION_SPARQL_RESULTS_JSON = "application/sparql-results+json";
  public static final String JSON_LD = "application/ld+json";
  public static final String TURTLE = "text/turtle";

  public static final String API_KEY_PREFIX = "https://trialverse.org/apikeys/";
  public static final String VERSION_PATH = "versions/";
  public static final String QUERY_ENDPOINT = "/query";
  public static final String HISTORY_ENDPOINT = "/history";
  public static final String DATA_ENDPOINT = "/data";
  public static final String QUERY_PARAM_QUERY = "query";

  public static final String COPY_OF_QUERY_PARAM = "copyOf";
  public static final String GRAPH_QUERY_PARAM = "graph";
  public static final String QUERY_STRING_DEFAULT_GRAPH = "?default";
  public static final String X_EVENT_SOURCE_VERSION = "X-EventSource-Version";

  public static final String EVENT_SOURCE_TITLE_HEADER = "X-EventSource-Title";
  public static final String EVENT_SOURCE_DESCRIPTION_HEADER = "X-EventSource-Description";

  public static final String EVENT_SOURCE_CREATOR_HEADER = "X-EventSource-Creator";
  public static final String X_ACCEPT_EVENT_SOURCE_VERSION = "X-Accept-EventSource-Version";
  public static final String COMMIT_TITLE_PARAM = "commitTitle";

  public static final String COMMIT_DESCRIPTION_PARAM = "commitDescription";
  public static final String ACCEPT_HEADER = "Accept";
  public static final String ACCEPT_TURTLE_HEADER = "Accept=" + WebContent.contentTypeTurtle;
  public static final String ACCEPT_JSON_HEADER = "Accept=" + APPLICATION_JSON_UTF8_VALUE;

  public static final String VERSION_UUID = "versionUuid";
  private static final String TRIPLESTORE_BASE_URI = loadSystemEnvTripleStoreBaseURI();
  private static final String TRIPLESTORE_DATA_URI = TRIPLESTORE_BASE_URI + "/current";

  private static String loadSystemEnvTripleStoreBaseURI() {
    String tripleStoreBaseURI = System.getenv("TRIPLESTORE_BASE_URI");
    if (tripleStoreBaseURI == null || tripleStoreBaseURI.isEmpty()) {
      LoggerFactory
              .getLogger(WebConstants.class)
              .error("Cannot start server, no TRIPLESTORE_BASE_URI environment variable found");
      System.exit(-1);
    }
    return tripleStoreBaseURI;
  }

  public String getTriplestoreBaseUri() {
    return TRIPLESTORE_BASE_URI;
  }

  public String getTriplestoreDataUri() {
    return TRIPLESTORE_DATA_URI;
  }

  public static MediaType getApplicationJsonUtf8() {
    return APPLICATION_JSON_UTF8;
  }

  public static String getApplicationJsonUtf8Value() {
    return APPLICATION_JSON_UTF8_VALUE;
  }

  public URI buildVersionUri(String versionUuid) {
    return UriComponentsBuilder.fromHttpUrl(getTriplestoreBaseUri())
            .path(VERSION_PATH)
            .path(versionUuid)
            .build()
            .toUri();
  }
}

